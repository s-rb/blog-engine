package main.api.request;

public class RestorePassRequest implements RequestApi {

    private String email;

    public RestorePassRequest(String email) {
        this.email = email;
    }

    public RestorePassRequest() {}

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}