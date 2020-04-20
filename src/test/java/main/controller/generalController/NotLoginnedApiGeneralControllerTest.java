package main.controller.generalController;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.TestUtils;
import main.api.request.AddCommentRequest;
import main.api.request.EditProfileRequest;
import main.api.request.ModeratePostRequest;
import main.api.request.SetGlobalSettingsRequest;
import main.api.response.*;
import main.model.entities.Post;
import main.services.interfaces.*;
import org.hamcrest.Matchers;
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

import java.sql.Date;
import java.util.HashMap;
import java.util.List;

import static main.controller.generalController.LoginnedApiGeneralControllerTest.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-user-before.sql", "/create-posts-before.sql", "/create-tags-before.sql",
        "/create-tag2post-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/create-tag2post-after.sql", "/create-tags-after.sql", "/create-comments-after.sql",
        "/create-posts-after.sql", "/create-user-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class NotLoginnedApiGeneralControllerTest {

    public static final String API_CALENDAR_URL = "/api/calendar";
    public static final String API_SETTINGS_URL = "/api/settings";
    @Autowired
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

    @Value("${general_data.title1}")
    String title;
    @Value("${general_data.phone1}")
    String phone;
    @Value("${general_data.email1}")
    String email;
    @Value("${general_data.copyright_from1}")
    String copyrightFrom;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetGeneralData() throws Exception {
        String subtitle = "Рассказы разработчиков";
        String copyright = "Дмитрий Сергеев";
        ObjectMapper mapper = new ObjectMapper();
        GetGeneralDataResponse response = new GetGeneralDataResponse(
                title, subtitle, phone, email, copyright, copyrightFrom);
        mockMvc.perform(get("/api/init")).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.title", Matchers.is(response.getTitle())))
                .andExpect(jsonPath("$.subtitle", Matchers.is(subtitle)))
                .andExpect(jsonPath("$.phone", Matchers.is(response.getPhone())))
                .andExpect(jsonPath("$.email", Matchers.is(response.getEmail())))
                .andExpect(jsonPath("$.copyright", Matchers.is(response.getCopyright())))
                .andExpect(jsonPath("$.copyrightFrom", Matchers.is(response.getCopyrightFrom())));
    }

    @Test
    public void testAddComment_toPostUnauthorized() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        AddCommentRequest addCommentRequest = new AddCommentRequest(0, 10,
                "Lorem ipsum text here. Lots of text. Lorem ipsum text here. " +
                        "Lots of text. Lorem ipsum text here. Lots of text. ");
        mockMvc.perform(
                post("/api/comment").contentType(TestUtils.APPLICATION_JSON_UTF8).
                        content(mapper.writeValueAsBytes(addCommentRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        new BadRequestMessageResponse("Пользователь не авторизован"))));
    }

    @Test
    public void testGetTagsWithQuery_allIsOk() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<String> tags = List.of("Habr");
        List<Integer> tagCount = List.of(2);
        HashMap<String, Double> queryTagsMap = new HashMap<>();
        int mostFrequentTagCount = 3;
        for (int i = 0; i < tags.size(); i++) {
            Double weight = ((double) tagCount.get(i) / (double) mostFrequentTagCount);
            queryTagsMap.put(tags.get(i), weight);
        }
        GetTagsResponse expectedResponse = new GetTagsResponse(queryTagsMap);
        mockMvc.perform(
                get("/api/tag").param("query", "Habr")
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        expectedResponse)));
    }

    @Test
    public void testGetTagsEmptyQuery_allIsOk() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<String> tags = List.of("Habr", "Tag", "Shop", "Java", "Idea", "Питон");
        List<Integer> tagCount = List.of(2, 1, 1, 1, 3, 1);
        HashMap<String, Double> queryTagsMap = new HashMap<>();
        int mostFrequentTagCount = 3;
        for (int i = 0; i < tags.size(); i++) {
            Double weight = ((double) tagCount.get(i) / (double) mostFrequentTagCount);
            queryTagsMap.put(tags.get(i), weight);
        }
        GetTagsResponse expectedResponse = new GetTagsResponse(queryTagsMap);
        mockMvc.perform(
                get("/api/tag").param("query", "")
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        expectedResponse)));
    }

    @Test
    public void testGetTagsWithoutQuery_allIsOk() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<String> tags = List.of("Habr", "Tag", "Shop", "Java", "Idea", "Питон");
        List<Integer> tagCount = List.of(2, 1, 1, 1, 3, 1);
        HashMap<String, Double> queryTagsMap = new HashMap<>();
        int mostFrequentTagCount = 3;
        for (int i = 0; i < tags.size(); i++) {
            Double weight = ((double) tagCount.get(i) / (double) mostFrequentTagCount);
            queryTagsMap.put(tags.get(i), weight);
        }
        GetTagsResponse expectedResponse = new GetTagsResponse(queryTagsMap);
        mockMvc.perform(
                get("/api/tag")
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        expectedResponse)));
    }

    @Test
    public void testGetTagsWithQuery_wrongTag() throws Exception {
        mockMvc.perform(
                get("/api/tag").param("query", "someWrongTag")
                        .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().bytes("".getBytes()));
    }

    @Test
    public void testCountPostsByYear_allIsOk() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<Integer> allYears = List.of(2020);
        List<Post> postsByYear = List.of(
                postRepoService.getPostById(10),
                postRepoService.getPostById(13),
                postRepoService.getPostById(14),
                postRepoService.getPostById(15),
                postRepoService.getPostById(16),
                postRepoService.getPostById(17)
        );
        HashMap<Date, Integer> postsCountByDate = new HashMap<>();
        for (Post p : postsByYear) {
            Date postDate = Date.valueOf(p.getTime().toLocalDate());
            Integer postCount = postsCountByDate.getOrDefault(postDate, 0);
            postsCountByDate.put(postDate, postCount + 1);
        }
        GetPostsByCalendarResponse expectedResponse = new GetPostsByCalendarResponse(postsCountByDate, allYears);
        mockMvc.perform(get(API_CALENDAR_URL).param("year", "2020")
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        expectedResponse)));
    }

    @Test
    public void testCountPostsByYear_epmtyYear() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        List<Integer> allYears = List.of(2020);
        List<Post> postsByYear = List.of(
                postRepoService.getPostById(10),
                postRepoService.getPostById(13),
                postRepoService.getPostById(14),
                postRepoService.getPostById(15),
                postRepoService.getPostById(16),
                postRepoService.getPostById(17)
        );
        HashMap<Date, Integer> postsCountByDate = new HashMap<>();
        for (Post p : postsByYear) {
            Date postDate = Date.valueOf(p.getTime().toLocalDate());
            Integer postCount = postsCountByDate.getOrDefault(postDate, 0);
            postsCountByDate.put(postDate, postCount + 1);
        }
        GetPostsByCalendarResponse expectedResponse = new GetPostsByCalendarResponse(postsCountByDate, allYears);
        mockMvc.perform(get(API_CALENDAR_URL).param("year", "")
                .contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        expectedResponse)));
    }

    @Test
    public void testModeratePost() throws Exception {
        String message = "Пользователь не авторизован";
        ObjectMapper mapper = new ObjectMapper();
        ModeratePostRequest request = new ModeratePostRequest(12, "ACCEPT");
        mockMvc.perform(post(API_MODERATION_URL).contentType(TestUtils.APPLICATION_JSON_UTF8).
                content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        new BadRequestMessageResponse(message))));
    }

    @Test
    public void testEditProfile() throws Exception {
        String message = "Пользователь не авторизован";
        ObjectMapper mapper = new ObjectMapper();
        EditProfileRequest request = new EditProfileRequest(
                (byte) 0, "newName", "newEmail@mail.ru", "newPassword");
        mockMvc.perform(post(API_PROFILE_MY_URL).contentType(TestUtils.APPLICATION_JSON_UTF8).
                content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        new BadRequestMessageResponse(message))));
    }

    @Test
    public void testGetMyStatistics() throws Exception {
        String message = "Пользователь не авторизован";
        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(get(API_STATISTICS_MY_URL))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        new BadRequestMessageResponse(message))));
    }

    @Test
    @Sql(value = {"/create-user-before.sql", "/create-posts-before.sql", "/create-settings-before.sql",
            "/create-postvotes-before.sql"},
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/create-postvotes-after.sql", "/create-posts-after.sql", "/create-user-after.sql",
            "/create-settings-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testGetAllStatistics_userWithoutEnoughRights() throws Exception {
        String message = "Пользователь не авторизован, показ" +
                " статистики неавторизованным пользователям запрещен";
        ObjectMapper mapper = new ObjectMapper();
        mockMvc.perform(get(API_STATISTICS_ALL_URL))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        new BadRequestMessageResponse(message))));
    }

    @Test
    @Sql(value = {"/create-settings-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/create-settings-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testGetSettings_allIsOk() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        GetGlobalSettingsResponse expectedResponse =
                new GetGlobalSettingsResponse(globalSettingsRepoService.getAllGlobalSettingsSet());
        mockMvc.perform(get(API_SETTINGS_URL)).andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    @Test
    public void testSetSettings() throws Exception {
        SetGlobalSettingsRequest request =
                new SetGlobalSettingsRequest(true, false, true);
        ObjectMapper mapper = new ObjectMapper();
        BadRequestMessageResponse expectedResponse =
                new BadRequestMessageResponse("Пользователь не авторизован");
        mockMvc.perform(put(API_SETTINGS_URL).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request))).andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }
}