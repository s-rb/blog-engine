package main.services.interfaces;

import main.api.response.ResponseApi;
import main.model.entities.CaptchaCode;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;

public interface CaptchaRepositoryService {

    ResponseEntity<ResponseApi> generateCaptcha();

    ArrayList<CaptchaCode> getAllCaptchas();
}
