package main.api.response;

public class BooleanResponse implements ResponseApi {

    private boolean result;

    public BooleanResponse() {
        this.result = false;
    }

    public BooleanResponse(boolean result) {
        this.result = result;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}