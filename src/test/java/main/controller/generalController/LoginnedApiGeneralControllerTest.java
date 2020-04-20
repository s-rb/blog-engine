package main.controller.generalController;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.TestUtils;
import main.api.request.*;
import main.api.response.*;
import main.model.ModerationStatus;
import main.model.entities.Post;
import main.model.entities.User;
import main.services.interfaces.*;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;
import java.util.Objects;

import static main.controller.generalController.NotLoginnedApiGeneralControllerTest.API_SETTINGS_URL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-user-before.sql", "/create-posts-before.sql",
        "/create-postvotes-before.sql", "/create-settings-before.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/create-settings-after.sql", "/create-postvotes-after.sql", "/create-comments-after.sql",
        "/create-posts-after.sql", "/create-user-after.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class LoginnedApiGeneralControllerTest {

    private static final String ADD_COMMENT_URL = "/api/comment";
    private static final String EXISTED_MODERATOR_EMAIL = "mail@mail.ru";
    private static final String EXISTED_MODERATOR_PASSWORD = "password";
    private static final int MODERATOR_USER_ID = 21;
    private static final int TEMP_MODERATION_COUNT = 1;
    public static final String API_MODERATION_URL = "/api/moderation";
    public static final String API_PROFILE_MY_URL = "/api/profile/my";
    public static final String API_STATISTICS_MY_URL = "/api/statistics/my";
    public static final String API_STATISTICS_ALL_URL = "/api/statistics/all";

    private MockMvc mockMvc;
    @Autowired
    private PostRepositoryService postRepoService;
    @Autowired
    private UserRepositoryService userRepoService;
    @Autowired
    private GlobalSettingsRepositoryService globalSettingsRepoService;
    @Autowired
    private TagRepositoryService tagRepoService;
    @Autowired
    private GeneralDataService generalDataService;
    @Autowired
    private PostCommentRepositoryService commentRepoService;
    @Autowired
    private WebApplicationContext wac;

    @Value("${post_comment.min_length}")
    private int minCommentLength;
    @Value("${post_comment.max_length}")
    private int maxCommentLength;

    private static final String DUMMY_TEXT = "Lorem ipsum text here. Lots of text. " +
            "Lorem ipsum text here. Lots of text. Lorem ipsum text here. Lots of text.";

    private LoginRequest loginRequest;
    private User user;
    private LoginResponse expectedLoginResponse;
    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testAddComment_toPost_AllIsOk() throws Exception {
        AddCommentRequest addCommentRequest = new AddCommentRequest(0, 10,
                DUMMY_TEXT);
        performLogin(null, null, null, null);
        mockMvc.perform(
                post(ADD_COMMENT_URL).contentType(TestUtils.APPLICATION_JSON_UTF8).
                        content(mapper.writeValueAsBytes(addCommentRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", Matchers.is(1)));
    }

    @Test
    public void testAddComment_toPost_NullParentPostAndComment() throws Exception {
        AddCommentRequest addCommentRequest = new AddCommentRequest(null, null,
                DUMMY_TEXT);
        performLogin(null, null, null, null);
        mockMvc.perform(
                post(ADD_COMMENT_URL).contentType(TestUtils.APPLICATION_JSON_UTF8).
                        content(mapper.writeValueAsBytes(addCommentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        new BadRequestMessageResponse(
                                "Не заданы родительский пост и комментарий"))));
    }

    @Test
    public void testAddComment_toPost_invalidText() throws Exception {
        String message = "Текст комментария не задан, слишком короткий (<" + minCommentLength
                + " символов) или превышает максимальный размер (" + maxCommentLength + " символов)";
        AddCommentRequest addCommentRequest = new AddCommentRequest(0, 10,
                "");
        performLogin(null, null, null, null);
        mockMvc.perform(post(ADD_COMMENT_URL).contentType(TestUtils.APPLICATION_JSON_UTF8).
                content(mapper.writeValueAsBytes(addCommentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        new BadRequestMessageResponse(message))));
    }

    @Test
    public void testAddComment_toPost_notFoundPostAndComment() throws Exception {
        String message = "Не найдены родительский пост и комментарий";
        AddCommentRequest addCommentRequest = new AddCommentRequest(1000, 1000,
                DUMMY_TEXT);
        performLogin(null, null, null, null);
        mockMvc.perform(post(ADD_COMMENT_URL).contentType(TestUtils.APPLICATION_JSON_UTF8).
                content(mapper.writeValueAsBytes(addCommentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        new BadRequestMessageResponse(message))));
    }

    @Test
    @Sql(value = {"/create-user-before.sql", "/create-posts-before.sql", "/create-settings-before.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/create-comments-after.sql", "/create-posts-after.sql", "/create-user-after.sql",
            "/create-settings-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testAddComment_toPost_userWithoutEnoughRights() throws Exception {
        String message = "Для добавления комментария требуется включить " +
                "многопользовательский режим и/или требуются права модератора";
        AddCommentRequest addCommentRequest = new AddCommentRequest(0, 10,
                DUMMY_TEXT);
        performLogin("new@mail.ru", "password", 1, 22);
        mockMvc.perform(post(ADD_COMMENT_URL).contentType(TestUtils.APPLICATION_JSON_UTF8).
                content(mapper.writeValueAsBytes(addCommentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        new BadRequestMessageResponse(message))));
    }

    @Test
    public void testModeratePost_AcceptAllIsOk() throws Exception {
        ModeratePostRequest request = new ModeratePostRequest(12, "ACCEPT");
        performLogin(null, null, null, null);
        mockMvc.perform(post(API_MODERATION_URL).contentType(TestUtils.APPLICATION_JSON_UTF8).
                content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(content().bytes("".getBytes()));
        Post post = postRepoService.getPostById(12);
        Assert.assertEquals(ModerationStatus.ACCEPTED, post.getModerationStatus());
    }

    @Test
    public void testModeratePost_DeclineAllIsOk() throws Exception {
        ModeratePostRequest request = new ModeratePostRequest(12, "DECLINE");
        performLogin(null, null, null, null);
        mockMvc.perform(post(API_MODERATION_URL).contentType(TestUtils.APPLICATION_JSON_UTF8).
                content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(content().bytes("".getBytes()));
        Post post = postRepoService.getPostById(12);
        Assert.assertEquals(ModerationStatus.DECLINED, post.getModerationStatus());
    }

    @Test
    public void testModeratePost_WrongDecision() throws Exception {
        String message = "Решение по модерации не распознано";
        ModeratePostRequest request = new ModeratePostRequest(12, "SOME_WRONG_DECISION");
        performLogin(null, null, null, null);
        mockMvc.perform(post(API_MODERATION_URL).contentType(TestUtils.APPLICATION_JSON_UTF8).
                content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        new BadRequestMessageResponse(message))));
    }

    @Test
    public void testModeratePost_IsNotModerator() throws Exception {
        String message = "Для модерации поста требуются права модератора";
        ModeratePostRequest request = new ModeratePostRequest(12, "ACCEPT");
        performLogin("new@mail.ru", "password", 1, 22);
        mockMvc.perform(post(API_MODERATION_URL).contentType(TestUtils.APPLICATION_JSON_UTF8).
                content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        new BadRequestMessageResponse(message))));
    }

    @Test
    public void testModeratePost_WrongPostId() throws Exception {
        String message = "Пост не найден";
        ModeratePostRequest request = new ModeratePostRequest(101, "ACCEPT");
        performLogin(null, null, null, null);
        mockMvc.perform(post(API_MODERATION_URL).contentType(TestUtils.APPLICATION_JSON_UTF8).
                content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        new BadRequestMessageResponse(message))));
    }

    @Test
    public void testEditProfile_allIsOk() throws Exception {
        EditProfileRequest request = new EditProfileRequest(
                (byte) 0, "newName", "newEmail@mail.ru", "newPassword");
        performLogin(null, null, null, null);
        ResponseApi expectedResponse = new BooleanResponse(true);
        mockMvc.perform(post(API_PROFILE_MY_URL).contentType(TestUtils.APPLICATION_JSON_UTF8).
                content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
        User user = userRepoService.getUser(21).getBody();
        Assert.assertNotNull(user);
        Assert.assertEquals("newName", user.getName());
        Assert.assertEquals("newEmail@mail.ru", user.getEmail());
        Assert.assertEquals("14A88B9D2F52C55B5FBCF9C5D9C11875", user.getStoredHashPass());
    }

    @Test
    public void testEditProfile_invalidParams() throws Exception {
        EditProfileRequest request = new EditProfileRequest(
                (byte) 0, "new", "someWrongEmail", "wr");
        performLogin(null, null, null, null);
        ResponseApi expectedResponse = new BadRequestMessageResponse(
                "Пользователь с таким именем уже существует",
                "Пароль не соответствует требованиям: введите не менее 6 символов",
                "Email не соответствует требованиям");
        mockMvc.perform(post(API_PROFILE_MY_URL).contentType(TestUtils.APPLICATION_JSON_UTF8).
                content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    @Test
    public void testEditProfile_invalidParamsNullPhoto() throws Exception {
        EditProfileWithPhotoRequest request = new EditProfileWithPhotoRequest(
                (byte) 0, "new", "someWrongEmail", "wr", null);
        performLogin(null, null, null, null);
        ResponseApi expectedResponse = new BadRequestMessageResponse(
                "Пользователь с таким именем уже существует",
                "Пароль не соответствует требованиям: введите не менее 6 символов",
                "Email не соответствует требованиям");
        mockMvc.perform(post(API_PROFILE_MY_URL).contentType(TestUtils.APPLICATION_JSON_UTF8).
                content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    @Test
    public void testGetMyStatistics_allIsOk() throws Exception {
        int postsCount = 4;
        int allLikesCount = 3;
        int allDislikeCount = 3;
        int viewsCount = 15;
        String firstPublicationDate = "2020-04-10 21:48";
        GetStatisticsResponse expectedResponse = new GetStatisticsResponse(postsCount, allLikesCount, allDislikeCount,
                viewsCount, firstPublicationDate);
        performLogin(null, null, null, null);
        mockMvc.perform(get(API_STATISTICS_MY_URL)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse.getMap())));
    }

    @Test
    public void testGetAllStatistics_allIsOk() throws Exception {
        int postsCount = 9;
        int allLikesCount = 6; // 5
        int allDislikeCount = 3; // 3
        int viewsCount = 37;
        String firstPublicationDate = "2020-04-09 21:48";
        GetStatisticsResponse expectedResponse = new GetStatisticsResponse(postsCount, allLikesCount, allDislikeCount,
                viewsCount, firstPublicationDate);
        performLogin(null, null, null, null);
        mockMvc.perform(get(API_STATISTICS_ALL_URL)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse.getMap())));
    }

    @Test
    public void testSetSettings_allIsOk() throws Exception {
        performLogin(null, null, null, null);
        SetGlobalSettingsRequest request =
                new SetGlobalSettingsRequest(true, false, true);
        ObjectMapper mapper = new ObjectMapper();
        GetGlobalSettingsResponse expectedResponse =
                new GetGlobalSettingsResponse(
                        Map.of(GlobalSettingsRepositoryService.MULTIUSER_MODE, true,
                                GlobalSettingsRepositoryService.STATISTICS_IS_PUBLIC, true,
                                GlobalSettingsRepositoryService.POST_PREMODERATION, false));
        mockMvc.perform(put(API_SETTINGS_URL).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request))).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    @Test
    public void testSetSettings_emptyParams() throws Exception {
        performLogin(null, null, null, null);
        SetGlobalSettingsRequest request =
                new SetGlobalSettingsRequest(null, null, null);
        ObjectMapper mapper = new ObjectMapper();
        BadRequestMessageResponse expectedResponse =
                new BadRequestMessageResponse("Не переданы параметры настроек");
        mockMvc.perform(put(API_SETTINGS_URL).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request))).andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    @Test
    public void testSetSettings_notModerator() throws Exception {
        performLogin("new@mail.ru", "password", 1, 22);
        SetGlobalSettingsRequest request =
                new SetGlobalSettingsRequest(true, false, true);
        ObjectMapper mapper = new ObjectMapper();
        BadRequestMessageResponse expectedResponse =
                new BadRequestMessageResponse("Для данного действия требуются права модератора");
        mockMvc.perform(put(API_SETTINGS_URL).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request))).andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    private void performLogin(String userEmail, String userPassword, Integer userModerationCount, Integer userId) throws Exception {
        String email = Objects.requireNonNullElse(userEmail, EXISTED_MODERATOR_EMAIL);
        String password = Objects.requireNonNullElse(userPassword, EXISTED_MODERATOR_PASSWORD);
        int moderationCount = Objects.requireNonNullElse(userModerationCount, TEMP_MODERATION_COUNT);
        int id = Objects.requireNonNullElse(userId, MODERATOR_USER_ID);
        mockMvc =
                webAppContextSetup(this.wac).apply(sharedHttpSession()).build();
        loginRequest = new LoginRequest(email, password);
        user = userRepoService.getUser(id).getBody();
        expectedLoginResponse = new LoginResponse(user, moderationCount);
        mapper = new ObjectMapper();
        mockMvc.perform(post("/api/auth/login").contentType(TestUtils.APPLICATION_JSON_UTF8).
                content(mapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().bytes(mapper.writeValueAsBytes(expectedLoginResponse)));
    }
}