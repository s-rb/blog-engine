package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.ToString;

import java.io.Serializable;

@ToString
public class RegisterRequest implements RequestApi, Serializable {

    @JsonProperty("e_mail")
    private String email;
    private String password;
    private String captcha;
    private String name;
    @JsonProperty("captcha_secret")
    private String captchaSecret;

    public RegisterRequest() {
    }

    public RegisterRequest(String email, String password, String captcha, String captchaSecret, String name) {
        this.email = email;
        this.password = password;
        this.captcha = captcha;
        this.captchaSecret = captchaSecret;
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}