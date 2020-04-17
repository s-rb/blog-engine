package main.model.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "captcha_codes")
public class CaptchaCode implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int id;

    @Column(nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime time;

    @Column(nullable = false, columnDefinition = "TINYTEXT")
    private String code;

    @Column(name = "secret_code", nullable = false, columnDefinition = "TINYTEXT")
    private String secretCode;

    public CaptchaCode(LocalDateTime time, String code, String secretCode) {
        this.time = time;
        this.code = code;
        this.secretCode = secretCode;
    }

    public CaptchaCode() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getSecretCode() {
        return secretCode;
    }

    public void setSecretCode(String secretCode) {
        this.secretCode = secretCode;
    }
}