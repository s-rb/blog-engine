package main.controller;

import lombok.extern.slf4j.Slf4j;
import main.api.request.ChangePasswordRequest;
import main.api.request.RegisterRequest;
import main.api.request.LoginRequest;
import main.api.request.RestorePassRequest;
import main.api.response.ResponseApi;
import main.services.interfaces.CaptchaRepositoryService;
import main.services.interfaces.UserRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestController
@ComponentScan("service")
@RequestMapping(value = "/api/auth/")
public class ApiAuthController {

    @Autowired
    private UserRepositoryService userRepoService;
    @Autowired
    private CaptchaRepositoryService captchaRepoService;

    public ApiAuthController() {
    }

    @GetMapping(value = "logout")
    public ResponseEntity<ResponseApi> logout(HttpServletRequest request) {
        log.info("--- Получен GET запрос на /api/auth/logout : ID сессии" + request.getSession().getId());
        return userRepoService.logout(request.getSession());
    }

    @PostMapping(value = "register")
    public ResponseEntity<ResponseApi> register(@RequestBody RegisterRequest registerRequest) {
        log.info("--- Получен POST запрос на /api/auth/register со следующими параметрам: {" +
                "Email: " + registerRequest.getEmail() + ", " +
                "Captcha: " + registerRequest.getCaptcha() + ", " +
                "CaptchaSecret: " + registerRequest.getCaptchaSecret() + ", " +
                "}");
        return userRepoService.register(registerRequest);
    }

    @PostMapping(value = "login")
    public ResponseEntity<ResponseApi> login(@RequestBody LoginRequest loginRequest, HttpServletRequest request) {
        log.info("--- Получен POST запрос на /api/auth/login со следующими параметрами: {" +
                "Email: " + loginRequest.getEmail() + ", " +
                "}");
        return userRepoService.login(loginRequest, request.getSession());
    }

    @GetMapping(value = "check")
    public ResponseEntity<ResponseApi> checkAuth(HttpServletRequest request) {
        log.info("--- Получен GET запрос на /api/auth/login : ID сессии" + request.getSession().getId());
        return userRepoService.checkAuth(request.getSession());
    }

    @PostMapping(value = "restore")
    public ResponseEntity<ResponseApi> restorePassword(@RequestBody RestorePassRequest restorePassRequest) {
        log.info("--- Получен POST запрос на /api/auth/restore со следующими параметрами: {" +
                "Email:" + restorePassRequest.getEmail() + "}");
        return userRepoService.restorePassword(restorePassRequest);
    }

    @PostMapping(value = "password")
    public ResponseEntity<ResponseApi> changePassword(@RequestBody ChangePasswordRequest changePasswordRequest) {
        log.info("--- Получен POST запрос на /api/auth/password со следующими параметрами: {" +
                "Code:" + changePasswordRequest.getCode() + "," +
                "Captcha:" + changePasswordRequest.getCaptcha() + "," +
                "CaptchaSecret:" + changePasswordRequest.getCaptchaSecret()
                + "}");
        return userRepoService.changePassword(changePasswordRequest);
    }

    @GetMapping(value = "captcha")
    public ResponseEntity<ResponseApi> generateCaptcha() {
        log.info("--- Получен GET запрос на /api/auth/captcha");
        return captchaRepoService.generateCaptcha();
    }
}