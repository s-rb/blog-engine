package main.controller.postController;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.api.request.AddPostRequest;
import main.api.request.LoginRequest;
import main.api.request.PostVoteRequest;
import main.api.request.RequestApi;
import main.api.response.*;
import main.model.entities.Post;
import main.model.entities.User;
import main.services.interfaces.*;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static main.controller.postController.LoginnedApiPostControllerTest.*;
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
public class NotLoginnedApiPostControllerTest {

    public static final String API_POST_URL = "/api/post";
    public static final String API_POST_SEARCH_URL = "/api/post/search";
    public static final String API_POST_BY_DATE_URL = "/api/post/byDate";
    public static final String API_POST_BY_TAG_URL = "/api/post/byTag";
    public static final String API_POST_MODERATION_URL = "/api/post/moderation";
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

    private LoginRequest loginRequest;
    private User user;
    private LoginResponse expectedLoginResponse;
    private ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetPostsWithParamsBest_allIsOk() throws Exception {
        testGetPostsWithParams(status().isOk(), 5, 0, "best",
                null, 10, 13, 14, 15, 16);
    }

    @Test
    public void testGetPostsWithParamsRecent_allIsOk() throws Exception {
        testGetPostsWithParams(status().isOk(), 5, 0, "recent",
                null, 13, 10, 14, 15, 16);
    }

    @Test
    public void testGetPostsWithParamsEarly_allIsOk() throws Exception {
        // popular, best, recent, early
        testGetPostsWithParams(status().isOk(), 5, 0, "early",
                null, 17, 16, 15, 14, 10);
    }

    @Test
    @Sql(value = {"/create-alldata-before.sql"}, executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(value = {"/create-alldata-after.sql"}, executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
    public void testGetPostsWithParamsPopular_allIsOk() throws Exception {
        testGetPostsWithParams(status().isOk(), 5, 0, "popular",
                null, 13, 10, 14, 16, 17);
    }

    @Test
    public void testGetPostsWithParams_WrongParams() throws Exception {
        testGetPostsWithParams(status().isBadRequest(), 0, -1, "wrongMode",
                new BadRequestMessageResponse("Передан отрицательный параметр сдвига",
                        "Ограничение количества отображаемых постов менее 1",
                        "Режим отображения не распознан"),
                13, 10, 14, 16, 17);
    }

    @Test
    public void testSearchPosts_allIsOk() throws Exception {
        int count = 4;
        int[] postIds = {10, 13, 15, 17};
        List<Post> postsToShow = Arrays.stream(postIds)
                .mapToObj(i -> postRepoService.getPostById(i))
                .collect(Collectors.toList());
        ResponseApi expectedResponse = new GetPostsResponse(count, (ArrayList<Post>) postsToShow, announceLength);
        testPerformRequest(API_POST_SEARCH_URL, HttpMethod.GET, null, expectedResponse, status().isOk(),
                Map.of("query", "Поиск1", "offset", "0", "limit", "5"));
    }

    @Test
    public void testSearchPosts_wrongParams() throws Exception {
        ResponseApi expectedResponse = new BadRequestMessageResponse(
                "Передан отрицательный параметр сдвига",
                "Ограничение количества отображаемых постов менее 1");
        testPerformRequest(API_POST_SEARCH_URL, HttpMethod.GET, null, expectedResponse, status().isBadRequest(),
                Map.of("query", "Поиск1", "offset", "-1", "limit", "0"));
    }

    private void testGetPostsWithParams(ResultMatcher status, int limit, int offset, String mode,
                                        BadRequestMessageResponse response, int... postIds) throws Exception {
        mapper = new ObjectMapper();
        MockMvc mockMvc = webAppContextSetup(this.wac).apply(sharedHttpSession()).build();
        List<Post> postsToShow = Arrays.stream(postIds)
                .mapToObj(i -> postRepoService.getPostById(i))
                .collect(Collectors.toList());
        int count = 6;
        ResponseApi expectedResponse = response != null ? response :
                new GetPostsResponse(count, (ArrayList<Post>) postsToShow, announceLength);
        mockMvc.perform(get(API_POST_URL).param("offset", String.valueOf(offset))
                .param("limit", String.valueOf(limit))
                .param("mode", mode)).andExpect(status)
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(content().json(mapper.writeValueAsString(
                        expectedResponse)));
    }

    @Test
    public void testGetPost_notAuthorized_allIsOk() throws Exception {
        int postId = 10;
        ResponseApi expectedResponse =
                new GetPostResponse(postRepoService.getPostById(postId), announceLength);
        testPerformRequest(API_POST_URL + "/" + postId, HttpMethod.GET, null,
                expectedResponse, status().isOk(), null);
    }

    @Test
    public void testGetPost_notAuthorized_inactivePost() throws Exception {
        int postId = 18;
        ResponseApi expectedResponse =
                new BadRequestMessageResponse("Публикация скрыта");
        testPerformRequest(API_POST_URL + "/" + postId, HttpMethod.GET, null,
                expectedResponse, status().isBadRequest(), null);
    }

    @Test
    public void testGetPost_notAuthorized_notModerated() throws Exception {
        int postId = 12;
        ResponseApi expectedResponse =
                new BadRequestMessageResponse("Требуется модерация");
        testPerformRequest(API_POST_URL + "/" + postId, HttpMethod.GET, null,
                expectedResponse, status().isBadRequest(), null);
    }

    @Test
    public void testGetPostByDate_allIsOk() throws Exception {
        int count = 1;
        String date = "2020-04-16";
        int[] postIds = {13};
        List<Post> postsToShow = Arrays.stream(postIds)
                .mapToObj(i -> postRepoService.getPostById(i))
                .collect(Collectors.toList());
        ResponseApi expectedResponse = new GetPostsResponse(count, (ArrayList<Post>) postsToShow, announceLength);
        testPerformRequest(API_POST_BY_DATE_URL, HttpMethod.GET, null, expectedResponse, status().isOk(),
                Map.of("date", date, "offset", "0", "limit", "5"));
    }

    @Test
    public void testGetPostsByDate_wrongParams() throws Exception {
        String date = "someUnparseableDate";
        ResponseApi expectedResponse = new BadRequestMessageResponse(
                "Передан отрицательный параметр сдвига",
                "Ограничение количества отображаемых постов менее 1",
                "Дата не задана");
        testPerformRequest(API_POST_BY_DATE_URL, HttpMethod.GET, null, expectedResponse,
                status().isBadRequest(), Map.of("date", date, "offset", "-1", "limit", "0"));

    }

    @Test
    public void testGetPostsByTag_allIsOk() throws Exception {
        int count = 2;
        String tag = "Habr";
        int[] postIds = {10, 13};
        List<Post> postsToShow = Arrays.stream(postIds)
                .mapToObj(i -> postRepoService.getPostById(i))
                .collect(Collectors.toList());
        ResponseApi expectedResponse = new GetPostsResponse(count, (ArrayList<Post>) postsToShow, announceLength);
        testPerformRequest(API_POST_BY_TAG_URL, HttpMethod.GET, null, expectedResponse, status().isOk(),
                Map.of("tag", tag, "offset", "0", "limit", "5"));
    }

    @Test
    public void testGetPostsByTag_wrongParams() throws Exception {
        String tag = " ";
        ResponseApi expectedResponse = new BadRequestMessageResponse(
                "Передан отрицательный параметр сдвига",
                "Ограничение количества отображаемых постов менее 1",
                "Тег не задан");
        testPerformRequest(API_POST_BY_TAG_URL, HttpMethod.GET, null, expectedResponse, status().isBadRequest(),
                Map.of("tag", tag, "offset", "-1", "limit", "0"));
    }

    @Test
    public void getPostsForModeration() throws Exception {
        String status = "NEW";
        ResponseApi expectedResponse = new BadRequestMessageResponse("Пользователь не авторизован");
        testPerformRequest(API_POST_MODERATION_URL, HttpMethod.GET,
                null, expectedResponse, status().isBadRequest(),
                Map.of("status", status, "offset", "0", "limit", "5"));
    }

    @Test
    public void testGetMyPosts_wrongParams() throws Exception {
        String status = "declined";
        ResponseApi expectedResponse = new BadRequestMessageResponse("Пользователь не авторизован");
        testPerformRequest(API_POST_MY_URL, HttpMethod.GET,
                null, expectedResponse, status().isBadRequest(),
                Map.of("status", status, "offset", "0", "limit", "5"));
    }

    @Test
    public void testAddPost() throws Exception {
        String time = "2020-04-20 14:40";
        byte active = 1;
        String title = DUMMY_POST_TITLE;
        String text = DUMMY_TEXT;
        List<String> tags = List.of("Spring", "Best");
        RequestApi addPostRequest = new AddPostRequest(time, active, title, text, tags);
        ResponseApi expectedResponse = new BadRequestMessageResponse("Пользователь не авторизован");
        testPerformRequest(API_POST_URL, HttpMethod.POST,
                addPostRequest, expectedResponse, status().isBadRequest(), null);
    }

    @Test
    public void testEditPost() throws Exception {
        int postId = 14;
        String time = "2020-04-20 14:45";
        byte active = 1;
        String title = DUMMY_POST_TITLE;
        String text = DUMMY_TEXT;
        List<String> tags = List.of("Spring2");
        RequestApi addPostRequest = new AddPostRequest(time, active, title, text, tags);
        ResponseApi expectedResponse = new BadRequestMessageResponse("Пользователь не авторизован");
        testPerformRequest(API_POST_URL + "/" + postId, HttpMethod.PUT,
                addPostRequest, expectedResponse, status().isBadRequest(), null);
    }

    @Test
    public void testLikePost() throws Exception {
        testLikeDislike(true);
    }

    @Test
    public void testDislikePost() throws Exception {
        testLikeDislike(false);
    }

    private void testLikeDislike(boolean isLike) throws Exception {
        int postId = isLike ? 16 : 10;
        RequestApi request = new PostVoteRequest(postId);
        ResponseApi expectedResponse = new BadRequestMessageResponse("Пользователь не авторизован");
        testPerformRequest(isLike ? API_POST_LIKE_URL : API_POST_DISLIKE_URL, HttpMethod.POST,
                request, expectedResponse, status().isBadRequest(), null);
    }

    private void testPerformRequest(String url, HttpMethod httpMethod, RequestApi request,
                                    ResponseApi expectedResponse, ResultMatcher expectedStatus,
                                    Map<String, String> params) throws Exception {
        mockMvc =
                webAppContextSetup(this.wac).apply(sharedHttpSession()).build();
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
}