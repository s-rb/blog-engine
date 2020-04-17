package main.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import main.TestUtils;
import main.api.request.ChangePasswordRequest;
import main.api.request.LoginRequest;
import main.api.request.RegisterRequest;
import main.api.request.RestorePassRequest;
import main.api.response.BadRequestMsgWithErrorsResponse;
import main.api.response.BooleanResponse;
import main.api.response.LoginResponse;
import main.model.entities.CaptchaCode;
import main.model.entities.User;
import main.services.interfaces.CaptchaRepositoryService;
import main.services.interfaces.UserRepositoryService;
import org.hamcrest.Matchers;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource("/application-test.properties")
@Sql(value = {"/create-user-before.sql", "/create-posts-before.sql", "/create-captchas-before.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(value = {"/create-posts-after.sql", "/create-user-after.sql", "/create-captchas-after.sql"},
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
public class AuthControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepositoryService userRepoService;
    @Autowired
    private CaptchaRepositoryService captchaRepoService;
    @Autowired
    private ApiAuthController apiAuthController;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testLogout() throws Exception {
        LoginRequest request = new LoginRequest("mail@mail.ru", "password");
        User user = userRepoService.getUser(21).getBody();
        ObjectMapper mapper = new ObjectMapper();
        int tempModerationCount = 1;
        BooleanResponse expectedResponse = new BooleanResponse(true);
        mockMvc.perform(
                post("/api/auth/login").contentType(TestUtils.APPLICATION_JSON_UTF8).
                        content(mapper.writeValueAsBytes(request)));
        MvcResult result = mockMvc.perform(get("/api/auth/logout")).andExpect(status().isOk()).andReturn();
        Assert.assertArrayEquals("Responses are different", mapper.writeValueAsBytes(expectedResponse),
                result.getResponse().getContentAsByteArray());
    }

    @Test
    public void testRegister_allIsOk() throws Exception {
        // Prepare
        captchaRepoService.generateCaptcha();
        CaptchaCode captchaCode = captchaRepoService.getAllCaptchas().stream().findFirst().get();
        RegisterRequest request = new RegisterRequest("someExample" + (int) (Math.random() * 100000) + "@mail.ru",
                "password", captchaCode.getCode(), captchaCode.getSecretCode(), "");
        // Execute
        mockMvc.perform(
                post("/api/auth/register").contentType(TestUtils.APPLICATION_JSON_UTF8).
                        content(TestUtils.convertObjectToJsonBytes(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void loginTest_allIsOk() throws Exception {
        LoginRequest request = new LoginRequest("mail@mail.ru", "password");
        User user = userRepoService.getUser(21).getBody();
        ObjectMapper mapper = new ObjectMapper();
        int tempModerationCount = 1;
        LoginResponse expectedResponse = new LoginResponse(user, tempModerationCount);
        MvcResult result = mockMvc.perform(
                post("/api/auth/login").contentType(TestUtils.APPLICATION_JSON_UTF8).
                        content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isOk()).andReturn();
        Assert.assertArrayEquals("Responses are different", mapper.writeValueAsBytes(expectedResponse),
                result.getResponse().getContentAsByteArray());
    }

    @Test
    public void loginTest_wrongPassword() throws Exception {
        LoginRequest request = new LoginRequest("mail@mail.ru", "somePassword");
        BadRequestMsgWithErrorsResponse expectedResponse = new BadRequestMsgWithErrorsResponse("Пароль введен не верно");
        ObjectMapper mapper = new ObjectMapper();
        MvcResult result = mockMvc.perform(
                post("/api/auth/login").contentType(TestUtils.APPLICATION_JSON_UTF8).
                        content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertArrayEquals("Responses are different", mapper.writeValueAsBytes(expectedResponse),
                result.getResponse().getContentAsByteArray());
    }

    @Test
    public void loginTest_wrongEmail() throws Exception {
        String email = "someKindOfEmail@mail.ru";
        LoginRequest request = new LoginRequest(email, "password");
        BadRequestMsgWithErrorsResponse expectedResponse =
                new BadRequestMsgWithErrorsResponse("Пользователь с таким E-mail: "
                        + email + " не существует");
        ObjectMapper mapper = new ObjectMapper();
        MvcResult result = mockMvc.perform(
                post("/api/auth/login").contentType(TestUtils.APPLICATION_JSON_UTF8).
                        content(mapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andReturn();
        Assert.assertArrayEquals("Responses are different", mapper.writeValueAsBytes(expectedResponse),
                result.getResponse().getContentAsByteArray());
    }

    @Test
    public void checkAuth_allIsOk() throws Exception {
        // Prepare
        LoginRequest request = new LoginRequest("mail@mail.ru", "password");
        ObjectMapper mapper = new ObjectMapper();
        // Execute
        MvcResult result = mockMvc.perform(get("/api/auth/check")
                .with(mockHttpServletRequest -> {
                    try {
                        return post("/api/auth/login").contentType(TestUtils.APPLICATION_JSON_UTF8).
                                content(mapper.writeValueAsBytes(request)).buildRequest(mockHttpServletRequest.getServletContext());
                    } catch (JsonProcessingException e) {
                        e.printStackTrace();
                    }
                    return mockHttpServletRequest;
                }))
                .andExpect(status().isOk()).andReturn();
        // Check
        User user = userRepoService.getUser(21).getBody();
        int tempModerationCount = 1;
        LoginResponse expectedResponse = new LoginResponse(user, tempModerationCount);
        Assert.assertArrayEquals("Responses are different", mapper.writeValueAsBytes(expectedResponse),
                result.getResponse().getContentAsByteArray());
    }

    @Test
    public void checkAuth_IsNotLoginned() throws Exception {
        // Prepare
        ObjectMapper mapper = new ObjectMapper();
        // Execute
        MvcResult result = mockMvc.perform(get("/api/auth/check"))
                .andExpect(status().isOk()).andReturn();
        // Check
        BooleanResponse expectedResponse =
                new BooleanResponse(false);
        Assert.assertArrayEquals("Responses are different", mapper.writeValueAsBytes(expectedResponse),
                result.getResponse().getContentAsByteArray());
    }

    @Test
    public void testRestorePassword_allIsOk() throws Exception {
        // Prepare
        RestorePassRequest request = new RestorePassRequest("restoreuser@mail.ru");
        ObjectMapper mapper = new ObjectMapper();
        // Execute
        MvcResult result = mockMvc.perform(
                post("/api/auth/restore").contentType(TestUtils.APPLICATION_JSON_UTF8).
                        content(TestUtils.convertObjectToJsonBytes(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON)).andReturn();
        BooleanResponse expectedResponse = new BooleanResponse(true);
        Assert.assertArrayEquals("Responses are different", mapper.writeValueAsBytes(expectedResponse),
                result.getResponse().getContentAsByteArray());
    }

    @Test
    public void testRestorePassword_emailIsNotValid() throws Exception {
        // Prepare
        String email = "someWrongString";
        RestorePassRequest request = new RestorePassRequest(email);
        ObjectMapper mapper = new ObjectMapper();
        // Execute
        mockMvc.perform(
                post("/api/auth/restore").contentType(TestUtils.APPLICATION_JSON_UTF8).
                        content(TestUtils.convertObjectToJsonBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content()
                        .json(mapper.writeValueAsString(
                                new BadRequestMsgWithErrorsResponse(
                                        "Ошибка! Вы указали неверный E-mail: " + email))));
    }

    @Test
    public void testRestorePassword_emailIsNotFound() throws Exception {
        // Prepare
        String email = "someEmailIsNotInList@mail.ru";
        RestorePassRequest request = new RestorePassRequest(email);
        ObjectMapper mapper = new ObjectMapper();
        // Execute
        mockMvc.perform(
                post("/api/auth/restore").contentType(TestUtils.APPLICATION_JSON_UTF8).
                        content(TestUtils.convertObjectToJsonBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content()
                        .json(mapper.writeValueAsString(
                                new BadRequestMsgWithErrorsResponse("Пользователь с таким E-mail: "
                                        + email + " не существует"))));
    }

    @Test
    public void testGenerateCaptcha() throws Exception {
        // Execute
        mockMvc.perform(
                get("/api/auth/captcha"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.secret", Matchers.notNullValue()))
                .andExpect(jsonPath("$.image", Matchers.notNullValue()));
    }

    @Test
    public void testChangePassword_allIsOk() throws Exception {
        // Prepare
        String newPassword = "someNewPassword";
        String hashedNewPassword = "DEFE6F8DAF04EBDE38277EEE0903C4ED";
        ChangePasswordRequest request = new ChangePasswordRequest(
                "restorecode", newPassword,
                "daney", "0pjkbqtfzehqo3p65pislx");;
        ObjectMapper mapper = new ObjectMapper();
        // Execute
        mockMvc.perform(
                post("/api/auth/password").contentType(TestUtils.APPLICATION_JSON_UTF8).
                        content(TestUtils.convertObjectToJsonBytes(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content()
                        .json(mapper.writeValueAsString(
                                new BooleanResponse(true))));
        User editedUser = userRepoService.getUser(23).getBody();
        Assert.assertNotNull(editedUser);
        Assert.assertNotNull(editedUser.getStoredHashPass());
        Assert.assertEquals(hashedNewPassword, editedUser.getStoredHashPass());
    }

    @Test
    public void testChangePassword_wrongCodePassCaptcha() throws Exception {
        // Prepare
        ChangePasswordRequest request = new ChangePasswordRequest(
                "someWrongCode", "aa",
                "someWrongCaptchaCode", "someWrongSecret");;
        ObjectMapper mapper = new ObjectMapper();
        // Execute
        mockMvc.perform(
                post("/api/auth/password").contentType(TestUtils.APPLICATION_JSON_UTF8).
                        content(TestUtils.convertObjectToJsonBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content()
                        .json(mapper.writeValueAsString(
                                new BadRequestMsgWithErrorsResponse(
                                        "Неверный код восстановления",
                                        "Пароль не соответствует требованиям",
                                        "Капча введена неверно"))));
    }

    @Test
    public void testChangePassword_blankInputs() throws Exception {
        // Prepare
        ChangePasswordRequest request = new ChangePasswordRequest(
                " ", " ",
                " ", " ");
        ObjectMapper mapper = new ObjectMapper();
        // Execute
        mockMvc.perform(
                post("/api/auth/password").contentType(TestUtils.APPLICATION_JSON_UTF8).
                        content(TestUtils.convertObjectToJsonBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content()
                        .json(mapper.writeValueAsString(
                                new BadRequestMsgWithErrorsResponse(
                                        "Не указан код восстановления",
                                        "Не введен пароль",
                                        "Не введен текст с картинки",
                                        "Не передан секретный код капчи"))));
    }

    @Test
    public void testChangePassword_emptyInputs() throws Exception {
        // Prepare
        ChangePasswordRequest request = new ChangePasswordRequest(
                null, null,
                null, null);
        ObjectMapper mapper = new ObjectMapper();
        // Execute
        mockMvc.perform(
                post("/api/auth/password").contentType(TestUtils.APPLICATION_JSON_UTF8).
                        content(TestUtils.convertObjectToJsonBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.content()
                        .json(mapper.writeValueAsString(
                                new BadRequestMsgWithErrorsResponse(
                                        "Не указан код восстановления",
                                        "Не введен пароль",
                                        "Не введен текст с картинки",
                                        "Не передан секретный код капчи"))));
    }
}