package main.services;

import lombok.extern.slf4j.Slf4j;
import main.api.response.GetGeneralDataResponse;
import main.api.response.ResponseApi;
import main.services.interfaces.GeneralDataService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GeneralDataServiceImpl implements GeneralDataService {

    @Value("${general_data.title}")
    private String title;
    @Value("${general_data.subtitle}")
    private String subtitle;
    @Value("${general_data.phone}")
    private String phone;
    @Value("${general_data.email}")
    private String email;
    @Value("${general_data.copyright}")
    private String copyright;
    @Value("${general_data.copyright_from}")
    private String copyrightFrom;

    @Override
    public ResponseEntity<ResponseApi> getData() {
        ResponseEntity<ResponseApi> response =
                new ResponseEntity<>(new GetGeneralDataResponse(
                        title, subtitle, phone, email, copyright, copyrightFrom),
                        HttpStatus.OK);
        log.info("--- Направляется ответ со следующими параметрами: {" +
                "HttpStatus:" + response.getStatusCode() + "," +
                response.getBody() + "}");
        return response;
    }
}