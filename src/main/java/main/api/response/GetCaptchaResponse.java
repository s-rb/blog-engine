package main.api.response;

public class GetCaptchaResponse implements ResponseApi {

    private String secret;
    private String image;

    public GetCaptchaResponse(String secretCode, String imageBase64) {
        secret = secretCode;
        image = imageBase64;
    }

    public String getSecret() {
        return secret;
    }

    public void setSecret(String secret) {
        this.secret = secret;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "ResponseCaptcha{" +
                "secret='" + secret + '\'' +
                ", image='" + image + '\'' +
                '}';
    }
}
