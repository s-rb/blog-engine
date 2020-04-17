package main.api.response;

import java.util.HashMap;
import java.util.Map;

public class FailEditProfileResponse implements ResponseApi {

    private boolean result;
    private Map<String, String> errors;

    public FailEditProfileResponse(boolean... args) {
        result = false;
        errors = new HashMap<>();
        boolean isEmailValid = args[0];
        boolean isNameValid = args[1];
        boolean sPassValid = args[2];
        boolean isPhotoValid = args.length == 4 ? args[3] : true;
        if (!isEmailValid) errors.put("email", "Этот e-mail уже зарегистрирован");
        if (!isNameValid) errors.put("name", "Имя указано неверно");
        if (!sPassValid) errors.put("password", "Пароль короче 6-ти символов");
        if (!isPhotoValid) errors.put("photo", "Фото слишком большое, нужно не более 5 Мб");
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