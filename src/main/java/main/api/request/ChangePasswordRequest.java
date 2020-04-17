package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChangePasswordRequest implements RequestApi {

    private String code;
    private String password;
    private String captcha;
    @JsonProperty("captcha_secret")
    private String captchaSecret;

    public ChangePasswordRequest() {
    }

    public ChangePasswordRequest(String code, String password, String captcha, String captchaSecret) {
        this.code = code;
        this.password = password;
        this.captcha = captcha;
        this.captchaSecret = captchaSecret;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

    public String getCaptchaSecret() {
        return captchaSecret;
    }

    public void setCaptchaSecret(String captchaSecret) {
        this.captchaSecret = captchaSecret;
    }
}