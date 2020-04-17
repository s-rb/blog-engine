package main.api.response;

import java.util.HashMap;
import java.util.Map;

public class FailAddPostResponse implements ResponseApi {

    private boolean result;
    private Map<String, String> errors;

    public FailAddPostResponse(boolean isTextValid, boolean isTitleValid) {
        result = false;
        errors = new HashMap<>();
        if (!isTextValid) {
            errors.put("text", "Текст публикации слишком короткий");
        }
        if (!isTitleValid) {
            errors.put("title", "Заголовок не установлен");
        }
    }

    public boolean isResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public Map<String, String> getErrors() {
        return errors;
    }

    public void setErrors(Map<String, String> errors) {
        this.errors = errors;
    }
}
