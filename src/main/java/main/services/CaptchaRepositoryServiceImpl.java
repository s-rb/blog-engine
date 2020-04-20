package main.services;

import com.github.cage.Cage;
import com.github.cage.IGenerator;
import com.github.cage.ObjectRoulette;
import com.github.cage.image.EffectConfig;
import com.github.cage.image.Painter;
import com.github.cage.image.RgbColorGenerator;
import com.github.cage.token.RandomTokenGenerator;
import lombok.extern.slf4j.Slf4j;
import main.api.response.GetCaptchaResponse;
import main.api.response.ResponseApi;
import main.model.entities.CaptchaCode;
import main.model.repositories.CaptchaRepository;
import main.services.interfaces.CaptchaRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Random;

@Slf4j
@Service
public class CaptchaRepositoryServiceImpl implements CaptchaRepositoryService {

    private static final char[] SYMBOLS_FOR_GENERATOR = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    @Value("${captcha.delete_timeout}")
    private int oldCaptchaDeleteTimeInMin;
    @Value("${captcha.random_secret_key_length}")
    private int randomSecretKeyLength;
    @Value("${captcha.image.text.length}")
    private int captchaPictureTextLength;
    @Value("${captcha.image.format}")
    private String captchaFormat;
    @Value("${captcha.image.format_string}")
    private String captchaFormatString;
    @Value("${captcha.image.text.font.random_font1}")
    private String captchaImageRandomFont1;
    @Value("${captcha.image.text.font.random_font2}")
    private String captchaImageRandomFont2;
    @Value("${captcha.image.text.font.random_font3}")
    private String captchaImageRandomFont3;
    @Value("${captcha.image.width}")
    private int captchaImageWidth;
    @Value("${captcha.image.height}")
    private int captchaImageHeight;

    @Autowired
    private CaptchaRepository captchaRepository;

    @Override
    public ResponseEntity<ResponseApi> generateCaptcha() {
        log.info("--- Удаление устаревших капч (время устаревания " + oldCaptchaDeleteTimeInMin + " мин)");
        LocalDateTime captchaDeleteBeforeTime = LocalDateTime.now().minusMinutes(oldCaptchaDeleteTimeInMin);
        captchaRepository.deleteOldCaptchas(captchaDeleteBeforeTime);
        log.info("--- Устаревшие капчи удалены");

        String secretCode = generateRandomString();
        Cage cage = getCage();
        String token = cage.getTokenGenerator().next();
        if (token.length() > captchaPictureTextLength) { // Ограничиваем размер картинки капчи
            token = token.substring(0, captchaPictureTextLength);
        }
        log.info("--- Сгенерировано случайное изображение капчи {" + token + "}");

        byte[] encodedBytes = Base64.getEncoder().encode(cage.draw(token));
        String captchaImageBase64String = captchaFormatString + ", " + new String(encodedBytes, StandardCharsets.UTF_8);
        log.info("--- Закодировано в BASE64 изображение капчи {" + captchaImageBase64String + "}");

        CaptchaCode newCaptcha = captchaRepository.save(new CaptchaCode(LocalDateTime.now(), token, secretCode));
        log.info("--- Созданная капча сохранена в БД с ID:" + newCaptcha.getId());

        ResponseEntity<ResponseApi> response = new ResponseEntity<>(
                new GetCaptchaResponse(secretCode, captchaImageBase64String), HttpStatus.OK);
        log.info("--- Направляется ответ со следующими параметрами: {" +
                "HttpStatus:" + response.getStatusCode() + "," +
                response.getBody() + "}");
        return response;
    }

    public ArrayList<CaptchaCode> getAllCaptchas() {
        return new ArrayList<>(captchaRepository.findAll());
    }

    private String generateRandomString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < randomSecretKeyLength; i++) {
            builder.append(SYMBOLS_FOR_GENERATOR[(int) (Math.random() * (SYMBOLS_FOR_GENERATOR.length - 1))]);
        }
        String randomString = builder.toString();
        log.info("--- Сгенерирован случайный код {" + randomString + "}");
        return randomString;
    }

    private Cage getCage() {
        Random rnd = new Random();
        Painter painter = new Painter(
                captchaImageWidth, captchaImageHeight, Color.WHITE, Painter.Quality.MAX, new EffectConfig(), rnd);
        int defFontHeight = painter.getHeight() / 2;
        Cage cage = new Cage(
                painter,
                (IGenerator<Font>) new ObjectRoulette<>(rnd, new Font[]{
                        new Font(captchaImageRandomFont1, Font.PLAIN, defFontHeight),
                        new Font(captchaImageRandomFont2, Font.PLAIN, defFontHeight),
                        new Font(captchaImageRandomFont3, Font.BOLD, defFontHeight)}),
                (IGenerator<Color>) new RgbColorGenerator(rnd),
                captchaFormat,
                Cage.DEFAULT_COMPRESS_RATIO,
                (IGenerator<String>) new RandomTokenGenerator(rnd),
                rnd);
        log.info("--- Создана случайная капча {" +
                "width:" + captchaImageWidth + "," +
                "height:" + captchaImageHeight + "," +
                "format" + captchaFormat
                + "}");
        return cage;
    }
}