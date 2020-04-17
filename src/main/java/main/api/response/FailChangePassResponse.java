package main.api.response;

import java.util.HashMap;
import java.util.Map;

public class FailChangePassResponse implements ResponseApi {

    private boolean result;
    private Map<String, String> errors;

    public FailChangePassResponse(boolean isCodeValid, boolean isPassValid, boolean isCaptchaCodeValid) {
        errors = new HashMap<>();
        if (!isCodeValid) errors.put("code", "Ссылка для восстановления пароля устарела.\n" +
                "<a href=\"/auth/restore\">Запросить ссылку снова</a>");
        if (!isPassValid) errors.put("password", "Пароль короче 6-ти символов");
        if (!isCaptchaCodeValid) errors.put("captcha", "Код с картинки введён неверно");
        result = false;
    }

    @Override
    public String toString() {
        return "ResponseFailChangePass{" +
                "result=" + result +
                ", errors=" + errors +
                '}';
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
