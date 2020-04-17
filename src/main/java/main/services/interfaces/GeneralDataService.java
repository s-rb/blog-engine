package main.services.interfaces;

import main.api.response.ResponseApi;
import org.springframework.http.ResponseEntity;

public interface GeneralDataService {

    ResponseEntity<ResponseApi> getData();
}