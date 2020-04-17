package main.api.response;

public class BadRequestMsgResponse implements ResponseApi {

    private String message;

    public BadRequestMsgResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
