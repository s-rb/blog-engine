package main.controller;

import lombok.extern.slf4j.Slf4j;
import main.api.response.BadRequestMessageResponse;
import main.api.response.ResponseApi;
import org.apache.tomcat.util.http.fileupload.impl.SizeLimitExceededException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartException;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @Value("${user.image.max_size}")
    private int maxPhotoSizeInBytes;

    @ExceptionHandler({MultipartException.class, java.lang.IllegalStateException.class,
            org.springframework.web.multipart.MaxUploadSizeExceededException.class,
            SizeLimitExceededException.class})
    public @ResponseBody
    ResponseEntity<ResponseApi> handleUploadedFileOversizeError(MultipartException e
    ) {
        log.error("--- Загружаемый файл превышает максимальный возможный для загрузки размер", e);
        ResponseEntity<ResponseApi> response = new ResponseEntity<>(
                new BadRequestMessageResponse(
                        "Размер фото не соответствует ограничению "
                                + (int) (maxPhotoSizeInBytes / 1024) + " кБ"),
                HttpStatus.BAD_REQUEST);
        return response;
    }
}