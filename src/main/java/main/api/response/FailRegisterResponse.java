package main.api.response;

import java.util.HashMap;
import java.util.Map;

public class FailRegisterResponse implements ResponseApi {

    private boolean result;
    private Map<String, String> errors;

    public FailRegisterResponse(boolean isNameValid, boolean isPassValid, boolean isCaptchaCodeValid, boolean isEmailValid) {
        errors = new HashMap<>();
        if (!isNameValid) errors.put("name", "Имя указано неверно");
        if (!isPassValid) errors.put("password", "Пароль короче 6-ти символов");
        if (!isCaptchaCodeValid) errors.put("captcha", "Код с картинки введён неверно");
        if (!isEmailValid) errors.put("email", "Этот e-mail уже зарегистрирован");
        result = false;
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
