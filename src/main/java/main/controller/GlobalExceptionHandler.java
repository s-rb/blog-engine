package main.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MultipartException.class)
    public String handleUploadedFileOversizeError(MultipartException e, RedirectAttributes redirectAttributes) {
        log.error("--- Загружаемый файл превышает максимальный возможный для загрузки размер", e);
        redirectAttributes.addFlashAttribute("message",
                "Загружаемый файл превышает максимальный возможный для загрузки размер");
        return "redirect:/";
    }
}