package main.api.response;

import java.util.Arrays;
import java.util.stream.Collectors;

public class BadRequestMsgWithErrorsResponse implements ResponseApi {

    private boolean result;
    private String message;

    public BadRequestMsgWithErrorsResponse(String... args) {
        result = false;
        message = Arrays.stream(args).filter(s -> s != null && !s.isBlank()).collect(Collectors.joining(". "));
//                String.join(". ", args);
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }
}
