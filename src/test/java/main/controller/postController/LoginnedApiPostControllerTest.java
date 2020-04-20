package main.controller.postController;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.TestUtils;
import main.api.request.AddPostRequest;
import main.api.request.LoginRequest;
import main.api.request.PostVoteRequest;
import main.api.request.RequestApi;
import main.api.response.*;
import main.model.entities.Post;
import main.model.entities.PostVote;
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
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.ResultMatcher;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.context.WebApplicationContext;

import java.util.*;
import java.util.stream.Collectors;

import static main.controller.postController.NotLoginnedApiPostControllerTest.API_POST_MODERATION_URL;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-alldata-before.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/create-alldata-after.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class LoginnedApiPostControllerTest {

    private static final String EXISTED_MODERATOR_EMAIL = "mail@mail.ru";
    private static final String EXISTED_MODERATOR_PASSWORD = "password";
    private static final int MODERATOR_USER_ID = 21;
    private static final int TEMP_MODERATION_COUNT = 1;
    public static final String API_POST_URL = "/api/post";
    public static final String API_POST_MY_URL = "/api/post/my";
    public static final String DUMMY_POST_TITLE = "SOME TITLE FOR A POST";
    public static final String API_POST_LIKE_URL = "/api/post/like";
    public static final String API_POST_DISLIKE_URL = "/api/post/dislike";
    private static final String DUMMY_TEXT = "Lorem ipsum text here. Lots of text. " +
            "Lorem ipsum text here. Lots of text. Lorem ipsum text here. Lots of text.";

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

    @Value("${post.announce.max_length}")
    private int announceLength;
    @Value("${post.body.min_length}")
    private int postBodyMinLength;
    @Value("${post.body.max_length}")
    private int postBodyMaxLength;
    @Value("${post.title.min_length}")
    private int postTitleMinLength;
    @Value("${post.title.max_length}")
    private int postTitleMaxLength;

    private LoginRequest loginRequest;
    private User user;
    private LoginResponse expectedLoginResponse;
    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetPost_allIsOk() throws Exception {
        performLogin(null, null, null, null);
        int postId = 10;
        ResponseApi expectedResponse =
                new GetPostResponse(postRepoService.getPostById(postId), announceLength);
        testPerformRequest("/api/post/" + postId, HttpMethod.GET, null,
                expectedResponse, status().isOk(), null);
    }

    @Test
    public void testGetPost_wrongId() throws Exception {
        performLogin(null, null, null, null);
        int postId = 1003;
        ResponseApi expectedResponse =
                new BadRequestMessageResponse("Пост не найден");
        testPerformRequest("/api/post/" + postId, HttpMethod.GET, null,
                expectedResponse, status().isBadRequest(), null);
    }

    @Test
    public void testGetPost_inactive_forAuthor() throws Exception {
        performLogin("new@mail.ru", "password", 1, 22);
        int postId = 18;
        ResponseApi expectedResponse =
                new GetPostResponse(postRepoService.getPostById(postId), announceLength);
        testPerformRequest("/api/post/" + postId, HttpMethod.GET, null,
                expectedResponse, status().isOk(), null);
    }

    @Test
    public void testGetPost_notModerated_forModerator() throws Exception {
        performLogin(null, null, null, null);
        int postId = 11;
        ResponseApi expectedResponse =
                new GetPostResponse(postRepoService.getPostById(postId), announceLength);
        testPerformRequest("/api/post/" + postId, HttpMethod.GET, null,
                expectedResponse, status().isOk(), null);
    }

    @Test
    public void getPostsForModeration_notForModerator() throws Exception {
        performLogin("new@mail.ru", "password", 1, 22);
        String status = "NEW";
        ResponseApi expectedResponse = new BadRequestMessageResponse("Требуются права модератора");
        testPerformRequest(API_POST_MODERATION_URL, HttpMethod.GET,
                null, expectedResponse, status().isBadRequest(),
                Map.of("status", status, "offset", "0", "limit", "5"));
    }

    @Test
    public void getPostsForModeration_wrongParams() throws Exception {
        performLogin(null, null, null, null);
        String status = "someWrongStatus";
        ResponseApi expectedResponse = new BadRequestMessageResponse(
                "Передан отрицательный параметр сдвига",
                "Ограничение количества отображаемых постов менее 1",
                "Статус модерации не распознан");
        testPerformRequest(API_POST_MODERATION_URL, HttpMethod.GET,
                null, expectedResponse, status().isBadRequest(),
                Map.of("status", status, "offset", "-1", "limit", "0"));
    }

    @Test
    public void getPostsForModeration_statusNew() throws Exception {
        performLogin(null, null, null, null);
        String status = "New";
        int count = 1;
        int[] postIds = {12};
        List<Post> postsToShow = Arrays.stream(postIds)
                .mapToObj(i -> postRepoService.getPostById(i))
                .collect(Collectors.toList());
        ResponseApi expectedResponse = new GetPostsForModerationResponse(count, (ArrayList<Post>) postsToShow, announceLength);
        testPerformRequest(API_POST_MODERATION_URL, HttpMethod.GET,
                null, expectedResponse, status().isOk(),
                Map.of("status", status, "offset", "0", "limit", "5"));
    }

    @Test
    public void getPostsForModeration_statusDeclined() throws Exception {
        performLogin(null, null, null, null);
        String status = "Declined";
        int count = 1;
        int[] postIds = {11};
        List<Post> postsToShow = Arrays.stream(postIds)
                .mapToObj(i -> postRepoService.getPostById(i))
                .collect(Collectors.toList());
        ResponseApi expectedResponse = new GetPostsForModerationResponse(count, (ArrayList<Post>) postsToShow, announceLength);
        testPerformRequest(API_POST_MODERATION_URL, HttpMethod.GET,
                null, expectedResponse, status().isOk(),
                Map.of("status", status, "offset", "0", "limit", "5"));
    }

    @Test
    public void getPostsForModeration_statusAccepted() throws Exception {
        performLogin(null, null, null, null);
        String status = "Accepted";
        int count = 4;
        int[] postIds = {10, 13, 15, 17};
        List<Post> postsToShow = Arrays.stream(postIds)
                .mapToObj(i -> postRepoService.getPostById(i))
                .collect(Collectors.toList());
        ResponseApi expectedResponse = new GetPostsForModerationResponse(count, (ArrayList<Post>) postsToShow, announceLength);
        testPerformRequest(API_POST_MODERATION_URL, HttpMethod.GET,
                null, expectedResponse, status().isOk(),
                Map.of("status", status, "offset", "0", "limit", "5"));
    }

    @Test
    public void testGetMyPostsInactive_allIsOk() throws Exception {
        performLogin("new@mail.ru", "password", 1, 22);
        String status = "inactive";
        int count = 1;
        int[] postIds = {18};
        List<Post> postsToShow = Arrays.stream(postIds).mapToObj(i -> postRepoService.getPostById(i))
                .collect(Collectors.toList());
        ResponseApi expectedResponse = new GetPostsResponse(count, (ArrayList<Post>) postsToShow, announceLength);
        testPerformRequest(API_POST_MY_URL, HttpMethod.GET, null, expectedResponse, status().isOk(),
                Map.of("status", status, "offset", "0", "limit", "5"));
    }

    @Test
    public void testGetMyPostsPending_allIsOk() throws Exception {
        performLogin(null, null, null, null);
        String status = "pending";
        int count = 1;
        int[] postIds = {12};
        List<Post> postsToShow = Arrays.stream(postIds).mapToObj(i -> postRepoService.getPostById(i))
                .collect(Collectors.toList());
        ResponseApi expectedResponse = new GetPostsResponse(count, (ArrayList<Post>) postsToShow, announceLength);
        testPerformRequest(API_POST_MY_URL, HttpMethod.GET, null, expectedResponse, status().isOk(),
                Map.of("status", status, "offset", "0", "limit", "5"));
    }

    @Test
    public void testGetMyPostsDeclined_allIsOk() throws Exception {
        performLogin(null, null, null, null);
        String status = "declined";
        int count = 1;
        int[] postIds = {11};
        List<Post> postsToShow = Arrays.stream(postIds).mapToObj(i -> postRepoService.getPostById(i))
                .collect(Collectors.toList());
        ResponseApi expectedResponse = new GetPostsResponse(count, (ArrayList<Post>) postsToShow, announceLength);
        testPerformRequest(API_POST_MY_URL, HttpMethod.GET, null, expectedResponse, status().isOk(),
                Map.of("status", status, "offset", "0", "limit", "5"));
    }

    @Test
    public void testGetMyPostsPublished_allIsOk() throws Exception {
        performLogin("new@mail.ru", "password", 1, 22);
        String status = "published";
        int count = 2;
        int[] postIds = {13, 17};
        List<Post> postsToShow = Arrays.stream(postIds).mapToObj(i -> postRepoService.getPostById(i))
                .collect(Collectors.toList());
        ResponseApi expectedResponse = new GetPostsResponse(count, (ArrayList<Post>) postsToShow, announceLength);
        testPerformRequest(API_POST_MY_URL, HttpMethod.GET, null, expectedResponse, status().isOk(),
                Map.of("status", status, "offset", "0", "limit", "5"));
    }

    @Test
    public void testGetMyPosts_wrongParams() throws Exception {
        performLogin(null, null, null, null);
        String status = "Some_Wrong_status";
        ResponseApi expectedResponse = new BadRequestMessageResponse(
                "Передан отрицательный параметр сдвига",
                "Ограничение количества отображаемых постов менее 1",
                "Статус модерации не распознан");
        testPerformRequest(API_POST_MY_URL, HttpMethod.GET,
                null, expectedResponse, status().isBadRequest(),
                Map.of("status", status, "offset", "-1", "limit", "0"));
    }

//    @Test
//    public void testAddPost_AllIsOk() throws Exception {
//        String time = "2020-04-20 14:40";
//        byte active = 1;
//        String title = DUMMY_POST_TITLE;
//        String text = DUMMY_TEXT;
//        List<String> tags = List.of("Best", "Java");
//        RequestApi addPostRequest = new AddPostRequest(time, active, title, text, tags);
//        ResponseApi expectedResponse = new BooleanResponse(true);
//        performLogin(null, null, null, null);
//        mapper = new ObjectMapper();
//        mockMvc.perform(post(API_POST_URL).contentType(MediaType.APPLICATION_JSON)
//                .content(mapper.writeValueAsBytes(addPostRequest))).andExpect(status().isOk())
//                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
//        Post newPost = postRepoService.getPostByTitle(title);
//        Assert.assertNotNull(newPost);
//        Assert.assertEquals(text, newPost.getText());
//        Assert.assertThat(tags.toArray(), Matchers.arrayContainingInAnyOrder(
//                newPost.getTagsToPostsSet().stream().map(i -> i.getTag().getName()).toArray()));
//    }

    @Test
    public void testAddPost_wrongParams() throws Exception {
        String time = "2020-04-20 14:40";
        byte active = 1;
        String title = "sw";
        String text = "wrong";
        List<String> tags = List.of("Spring", "Best");
        RequestApi addPostRequest = new AddPostRequest(time, active, title, text, tags);
        performLogin(null, null, null, null);
        ResponseApi expectedResponse = new BadRequestMessageResponse(
                "Текст не задан, слишком короткий (<" + postBodyMinLength
                        + " символов) или превышает максимальный размер (" + postBodyMaxLength
                        + " символов)",
                "Заголовок не задан, слишком короткий (<" + postTitleMinLength
                        + " символов) или превышает максимальный размер (" + postTitleMaxLength
                        + " символов)");
        testPerformRequest(API_POST_URL, HttpMethod.POST,
                addPostRequest, expectedResponse, status().isBadRequest(), null);
    }

    @Test
    public void testAddPost_notModerator_multiuserOff() throws Exception {
        String time = "2020-04-20 14:40";
        byte active = 1;
        String title = DUMMY_POST_TITLE;
        String text = DUMMY_TEXT;
        List<String> tags = List.of("Spring", "Best");
        RequestApi addPostRequest = new AddPostRequest(time, active, title, text, tags);
        performLogin("new@mail.ru", "password", 1, 22);
        ResponseApi expectedResponse = new BadRequestMessageResponse(
                "Для добавления поста требуется включить " +
                        "многопользовательский режим и/или требуются права модератора");
        testPerformRequest(API_POST_URL, HttpMethod.POST,
                addPostRequest, expectedResponse, status().isBadRequest(), null);
    }

    @Test
    public void testEditPost_allIsOk() throws Exception {
        int postId = 14;
        String time = "2020-04-20 14:45";
        byte active = 1;
        String title = "Some new title!!";
        String text = "Some additional text " + DUMMY_TEXT;
        List<String> tags = List.of("Spring2");
        RequestApi addPostRequest = new AddPostRequest(time, active, title, text, tags);
        ResponseApi expectedResponse = new BooleanResponse(true);
        performLogin(null, null, null, null);
        mapper = new ObjectMapper();
        mockMvc.perform(put(API_POST_URL + "/" + postId).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(addPostRequest))).andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
        Post newPost = postRepoService.getPostById(postId); // Для одиночного теста - PostID будет 1
        Assert.assertEquals(title, newPost.getTitle());
        Assert.assertEquals(text, newPost.getText());
        Assert.assertThat(newPost.getTagsToPostsSet().stream().map(i -> i.getTag().getName()).toArray(),
                Matchers.arrayContainingInAnyOrder(tags.toArray()));
    }

    @Test
    public void testEditPost_wrongPost() throws Exception {
        int postId = 1231;
        String time = "2020-04-20 14:45";
        byte active = 1;
        String title = "Some new title!!";
        String text = "Some additional text " + DUMMY_TEXT;
        List<String> tags = List.of("Spring2");
        RequestApi addPostRequest = new AddPostRequest(time, active, title, text, tags);
        ResponseApi expectedResponse = new BadRequestMessageResponse("Пост не найден");
        performLogin(null, null, null, null);
        testPerformRequest(API_POST_URL + "/" + postId, HttpMethod.PUT,
                addPostRequest, expectedResponse, status().isBadRequest(), null);
    }

    @Test
    public void testEditPost_wrongTextTitle() throws Exception {
        int postId = 14;
        String time = "2020-04-20 14:45";
        byte active = 1;
        String title = "ti";
        String text = "wrong";
        List<String> tags = List.of("Spring2");
        RequestApi addPostRequest = new AddPostRequest(time, active, title, text, tags);
        ResponseApi expectedResponse = new BadRequestMessageResponse(
                "Текст не задан, слишком короткий (<" + postBodyMinLength
                        + " символов) или превышает максимальный размер (" + postBodyMaxLength
                        + " символов)",
                "Заголовок не задан, слишком короткий (<" + postTitleMinLength
                        + " символов) или превышает максимальный размер (" + postTitleMaxLength
                        + " символов)");
        performLogin(null, null, null, null);
        testPerformRequest(API_POST_URL + "/" + postId, HttpMethod.PUT,
                addPostRequest, expectedResponse, status().isBadRequest(), null);
    }

    @Test
    public void testEditPost_notModerator() throws Exception {
        int postId = 14;
        String time = "2020-04-20 14:45";
        byte active = 1;
        String title = DUMMY_POST_TITLE;
        String text = DUMMY_TEXT;
        List<String> tags = List.of("Spring2");
        RequestApi addPostRequest = new AddPostRequest(time, active, title, text, tags);
        performLogin("new@mail.ru", "password", 1, 22);
        ResponseApi expectedResponse = new BadRequestMessageResponse(
                "Для редактирования поста требуется включить " +
                        "многопользовательский режим и быть его автором и/или требуются права модератора");
        testPerformRequest(API_POST_URL + "/" + postId, HttpMethod.PUT,
                addPostRequest, expectedResponse, status().isBadRequest(), null);
    }

    @Test
    public void testLikePost_allIsOk() throws Exception {
        testLikeDislike_allIsOk(true);
    }

    @Test
    public void testDislikePost_allIsOk() throws Exception {
        testLikeDislike_allIsOk(false);
    }

    @Test
    public void testLikeAgainSamePost() throws Exception {
        testLikeDislikeAgain_SamePost(true);
    }

    @Test
    public void testDislikeAgainSamePost() throws Exception {
        testLikeDislikeAgain_SamePost(false);
    }

    @Test
    public void testLikeNew_allIsOk() throws Exception {
        testLikeDislikeNew_allIsOk(true);
    }

    @Test
    public void testDislikeNew_allIsOk() throws Exception {
        testLikeDislikeNew_allIsOk(false);
    }

    private void testLikeDislikeNew_allIsOk(boolean isLike) throws Exception {
        int postId = 15;
        int currentUserId = 22;
        Post post = postRepoService.getPostById(postId);
        List<PostVote> currentUserCurrentPostVotes = post.getPostVotes().stream()
                .filter(p -> p.getUser().getId() == currentUserId).collect(Collectors.toList());
        Assert.assertEquals(0, currentUserCurrentPostVotes.size());

        testPerformLikeDislike(isLike, postId, currentUserId);

        post = postRepoService.getPostById(postId);
        currentUserCurrentPostVotes = post.getPostVotes().stream()
                .filter(p -> p.getUser().getId() == currentUserId).collect(Collectors.toList());
        Assert.assertEquals(1, currentUserCurrentPostVotes.size());
        PostVote like = currentUserCurrentPostVotes.get(0);
        Assert.assertEquals(isLike ? 1 : -1, like.getValue());
    }

    private void testLikeDislikeAgain_SamePost(boolean isLike) throws Exception {
        int postId = isLike ? 10 : 16;
        int currentUserId = 22;
        Post post = postRepoService.getPostById(postId);
        List<PostVote> currentUserCurrentPostVotes = post.getPostVotes().stream()
                .filter(p -> p.getUser().getId() == currentUserId).collect(Collectors.toList());
        Assert.assertEquals(1, currentUserCurrentPostVotes.size());
        PostVote like = currentUserCurrentPostVotes.get(0);
        Assert.assertEquals(isLike ? 1 : -1, like.getValue());

        RequestApi request = new PostVoteRequest(postId);
        performLogin("new@mail.ru", "password", 1, currentUserId);
        ResponseApi expectedResponse = new BadRequestMessageResponse("Повторный лайк/дизлайк");
        testPerformRequest(isLike ? API_POST_LIKE_URL : API_POST_DISLIKE_URL, HttpMethod.POST,
                request, expectedResponse, status().isBadRequest(), null);

        post = postRepoService.getPostById(postId);
        currentUserCurrentPostVotes = post.getPostVotes().stream()
                .filter(p -> p.getUser().getId() == currentUserId).collect(Collectors.toList());
        Assert.assertEquals(1, currentUserCurrentPostVotes.size());
        like = currentUserCurrentPostVotes.get(0);
        Assert.assertEquals(isLike ? 1 : -1, like.getValue());
    }

    private void testLikeDislike_allIsOk(boolean isLike) throws Exception {
        int postId = isLike ? 16 : 10;
        int currentUserId = 22;
        Post post = postRepoService.getPostById(postId);
        List<PostVote> currentUserCurrentPostVotes = post.getPostVotes().stream()
                .filter(p -> p.getUser().getId() == currentUserId).collect(Collectors.toList());
        Assert.assertEquals(1, currentUserCurrentPostVotes.size());
        PostVote like = currentUserCurrentPostVotes.get(0);
        Assert.assertEquals(isLike ? -1 : 1, like.getValue());

        testPerformLikeDislike(isLike, postId, currentUserId);

        post = postRepoService.getPostById(postId);
        currentUserCurrentPostVotes = post.getPostVotes().stream()
                .filter(p -> p.getUser().getId() == currentUserId).collect(Collectors.toList());
        Assert.assertEquals(1, currentUserCurrentPostVotes.size());
        like = currentUserCurrentPostVotes.get(0);
        Assert.assertEquals(isLike ? 1 : -1, like.getValue());
    }

    private void testPerformLikeDislike(boolean isLike, int postId, int currentUserId) throws Exception {
        RequestApi request = new PostVoteRequest(postId);
        performLogin("new@mail.ru", "password", 1, currentUserId);
        ResponseApi expectedResponse = new BooleanResponse(true);
        mapper = new ObjectMapper();
        mockMvc.perform(post(isLike ? API_POST_LIKE_URL : API_POST_DISLIKE_URL).contentType(MediaType.APPLICATION_JSON)
                .content(mapper.writeValueAsBytes(request))).andExpect(status().isOk())
                .andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
    }

    @Test
    public void testLike_WrongPost() throws Exception {
        testLikeDislikeWrongPost(true);
    }

    @Test
    public void testDislike_WrongPost() throws Exception {
        testLikeDislikeWrongPost(false);
    }

    private void testLikeDislikeWrongPost(boolean isLike) throws Exception {
        int postId = 1028;
        int currentUserId = 22;
        RequestApi request = new PostVoteRequest(postId);
        performLogin("new@mail.ru", "password", 1, currentUserId);
        ResponseApi expectedResponse = new BadRequestMessageResponse("Пост не найден");
        testPerformRequest(isLike ? API_POST_LIKE_URL : API_POST_DISLIKE_URL, HttpMethod.POST,
                request, expectedResponse, status().isBadRequest(), null);
    }


    private void testPerformRequest(String url, HttpMethod httpMethod, RequestApi request,
                                    ResponseApi expectedResponse, ResultMatcher expectedStatus,
                                    Map<String, String> params) throws Exception {
        mapper = new ObjectMapper();
        ResultActions result = null;
        MockHttpServletRequestBuilder requestBuilders = null;
        switch (httpMethod) {
            case GET:
                requestBuilders = get(url);
                break;
            case PUT:
                requestBuilders = put(url);
                break;
            case POST:
                requestBuilders = post(url);
                break;
        }
        if (requestBuilders == null) return;
        if ((params == null || params.isEmpty()) && request != null) {
            requestBuilders = requestBuilders.contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsBytes(request));
        } else if ((params != null && !params.isEmpty()) && request != null) {
            params.forEach(requestBuilders::param);
            requestBuilders.contentType(MediaType.APPLICATION_JSON)
                    .content(mapper.writeValueAsBytes(request));
        } else if (params != null && !params.isEmpty() && request == null) {
            MockHttpServletRequestBuilder finalRequestBuilders = requestBuilders;
            params.entrySet().forEach(i -> finalRequestBuilders.param(i.getKey(), i.getValue()));
        }
        result = mockMvc.perform(requestBuilders);
        result.andExpect(expectedStatus);
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(content().json(mapper.writeValueAsString(expectedResponse)));
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