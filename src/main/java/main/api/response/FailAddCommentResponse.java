package main.api.response;

public class FailAddCommentResponse implements ResponseApi {

    private boolean result;
    private String message;

    public FailAddCommentResponse(String message) {
        result = false;
        this.message = message;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
