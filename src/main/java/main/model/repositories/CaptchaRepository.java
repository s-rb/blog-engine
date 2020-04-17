package main.model.repositories;

import main.model.entities.CaptchaCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface CaptchaRepository extends JpaRepository<CaptchaCode, Integer> {

    @Transactional
    @Modifying
    @Query(value = "DELETE FROM captcha_codes c " +
            "WHERE DATE_ADD(c.time, INTERVAL ?1 MINUTE) < CURDATE()", nativeQuery = true)
    void deleteOldCaptchas(int captchaLiveTimeInMins);

    @Query(value = "SELECT * FROM captcha_codes c" +
            " WHERE c.secret_code = ?", nativeQuery = true)
    CaptchaCode getCaptchaBySecretCode(String secretCode);
}