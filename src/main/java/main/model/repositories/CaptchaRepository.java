package main.model.repositories;

import main.model.entities.CaptchaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Repository
public interface CaptchaRepository extends JpaRepository<CaptchaCode, Integer> {

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM captcha_codes " +
            "WHERE time < ?", nativeQuery = true)
    void deleteOldCaptchas(LocalDateTime captchaDeletedBeforeTime);

    @Query(value = "SELECT * FROM captcha_codes c" +
            " WHERE c.secret_code = ?", nativeQuery = true)
    CaptchaCode getCaptchaBySecretCode(String secretCode);
}