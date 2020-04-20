package main.controller.authController;

import com.fasterxml.jackson.databind.ObjectMapper;
import main.TestUtils;
import main.api.request.LoginRequest;
import main.api.response.BooleanResponse;
import main.api.response.LoginResponse;
import main.controller.ApiAuthController;
import main.model.entities.User;
import main.services.interfaces.CaptchaRepositoryService;
import main.services.interfaces.UserRepositoryService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.webAppContextSetup;
import static org.springframework.test.web.servlet.setup.SharedHttpSessionConfigurer.sharedHttpSession;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-user-before.sql", "/create-posts-before.sql", "/create-captchas-before.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/create-posts-after.sql", "/create-user-after.sql", "/create-captchas-after.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class LoginnedAuthControllerTest {

    private MockMvc mockMvc;
    @Autowired
    private UserRepositoryService userRepoService;
    @Autowired
    private CaptchaRepositoryService captchaRepoService;
    @Autowired
    private ApiAuthController apiAuthController;
    @Autowired
    private WebApplicationContext wac;

    LoginRequest loginRequest;
    User user;
    int tempModerationCount = 1;
    LoginResponse expectedLoginResponse;
    ObjectMapper mapper;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLogout() throws Exception {
        performLogin();
        BooleanResponse expectedResponse = new BooleanResponse(true);
        mockMvc.perform(get("/api/auth/logout"))
                .andExpect(status().isOk()).andExpect(content()
                .json(mapper.writeValueAsString(expectedResponse)));
    }

    @Test
    public void checkAuth_allIsOk() throws Exception {
        performLogin();
        LoginResponse expectedResponse = new LoginResponse(user, tempModerationCount);
        mockMvc.perform(get("/api/auth/check"))
                .andExpect(status().isOk()).andExpect(content()
                .json(mapper.writeValueAsString(expectedResponse)));
    }

    private void performLogin() throws Exception {
        mockMvc = webAppContextSetup(this.wac).apply(sharedHttpSession()).build();
        loginRequest = new LoginRequest("mail@mail.ru", "password");
        user = userRepoService.getUser(21).getBody();
        expectedLoginResponse = new LoginResponse(user, tempModerationCount);
        mapper = new ObjectMapper();
        mockMvc.perform(post("/api/auth/login").contentType(TestUtils.APPLICATION_JSON_UTF8).
                content(mapper.writeValueAsBytes(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(content().bytes(mapper.writeValueAsBytes(expectedLoginResponse)));
    }
}