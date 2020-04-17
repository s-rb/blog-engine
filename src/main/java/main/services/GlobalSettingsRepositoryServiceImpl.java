package main.services;

import lombok.extern.slf4j.Slf4j;
import main.api.request.SetGlobalSettingsRequest;
import main.api.response.BadRequestMsgWithErrorsResponse;
import main.api.response.GetGlobalSettingsResponse;
import main.api.response.ResponseApi;
import main.model.entities.GlobalSettings;
import main.model.entities.User;
import main.model.repositories.GlobalSettingsRepository;
import main.services.interfaces.GlobalSettingsRepositoryService;
import main.services.interfaces.UserRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Slf4j
@Service
public class GlobalSettingsRepositoryServiceImpl implements GlobalSettingsRepositoryService {

    @Value("${global_settings.default_values.multiuser_mode}")
    private String multiuserDefaultValue;
    @Value("${global_settings.default_values.statistics_is_public}")
    private String statisticsDefaultValue;
    @Value("${global_settings.default_values.post_premoderation}")
    private String postPremoderationDefaultValue;

    @Autowired
    private GlobalSettingsRepository globalSettingsRepository;
    @Autowired
    private UserRepositoryService userRepositoryService;

    @Override
    public ResponseEntity<?> getGlobalSettings() {
        GetGlobalSettingsResponse responseSettings = new GetGlobalSettingsResponse(getAllGlobalSettingsSet());
        ResponseEntity<ResponseApi> response = new ResponseEntity<>(responseSettings, HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;

    }

    @Override
    public ResponseEntity<?> setGlobalSettings(SetGlobalSettingsRequest setGlobalSettingsRequest,
                                               HttpSession session) {
        Boolean multiUserModeSetting = setGlobalSettingsRequest.getMultiuserMode();
        Boolean postPremoderationSetting = setGlobalSettingsRequest.getPostPremoderation();
        Boolean statisticsIsPublicSetting = setGlobalSettingsRequest.getStatisticsIsPublic();
        // Проверка: заданы ли какие-то параметры
        if (multiUserModeSetting == null && postPremoderationSetting == null && statisticsIsPublicSetting == null) {
            ResponseEntity<ResponseApi> response = new ResponseEntity<>(
                    new BadRequestMsgWithErrorsResponse("Не переданы параметры настроек"),
                    HttpStatus.BAD_REQUEST);
            log.warn("--- Не заданы параметры настроек");
            return response;
        }
        // Проверка: есть ли пользователь и права на внесение изменений в настройки
        User user = userRepositoryService.getUserBySession(session);
        if (user == null) {
            log.warn("--- Не найден пользователь по номеру сессии: " + session.getId());
            return new ResponseEntity<ResponseApi>(
                    new BadRequestMsgWithErrorsResponse("Пользователь не авторизован"),
                    HttpStatus.BAD_REQUEST);
        }
        if (!user.isModerator()) {
            log.info("--- Для данного действия пользователю " + user.getId() + ":"
                    + user.getName() + " требуются права модератора");
            return new ResponseEntity<ResponseApi>(
                    new BadRequestMsgWithErrorsResponse("Для данного действия требуются права модератора"),
                    HttpStatus.BAD_REQUEST);
        }
        // Устанавливаем новые настройки и получаем результат
        HashSet<GlobalSettings> settings = getAllGlobalSettingsSet();
        Map<String, Boolean> resultMap = new HashMap<>();
        setNewSettingsAndAddToMap(settings, resultMap, multiUserModeSetting,
                postPremoderationSetting, statisticsIsPublicSetting);

        ResponseEntity<ResponseApi> response = new ResponseEntity<>(
                new GetGlobalSettingsResponse(resultMap), HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }

    private void setNewSettingsAndAddToMap(HashSet<GlobalSettings> settings, Map<String, Boolean> resultMap,
                                           Boolean multiUserModeSetting, Boolean postPremoderationSetting,
                                           Boolean statisticsIsPublicSetting) {
        for (GlobalSettings g : settings) {
            switch (g.getCode().toUpperCase()) {
                case MULTIUSER_MODE: {
                    resultMap.put(MULTIUSER_MODE, multiUserModeSetting != null
                            ? yesOrNoToBoolean(setGSetting(g, multiUserModeSetting).getValue())
                            : yesOrNoToBoolean(g.getValue()));
                    break;
                }
                case POST_PREMODERATION: {
                    resultMap.put(POST_PREMODERATION, postPremoderationSetting != null
                            ? yesOrNoToBoolean(setGSetting(g, postPremoderationSetting).getValue())
                            : yesOrNoToBoolean(g.getValue()));
                    break;
                }
                case STATISTICS_IS_PUBLIC: {
                    resultMap.put(STATISTICS_IS_PUBLIC, statisticsIsPublicSetting != null
                            ? yesOrNoToBoolean(setGSetting(g, statisticsIsPublicSetting).getValue())
                            : yesOrNoToBoolean(g.getValue()));
                    break;
                }
            }
        }
    }

    @Override
    public HashSet<GlobalSettings> getAllGlobalSettingsSet() {
        HashSet<GlobalSettings> gsSet = new HashSet<>(globalSettingsRepository.findAll());
        if (gsSet.isEmpty()) {
            setDefaultGlobalSettings(gsSet);
        } else {
            boolean hasMultiuserMode = false;
            boolean hasPostPremoderation = false;
            boolean hasStatisticsIsPublic = false;
            for (GlobalSettings g : gsSet) {
                String globalSettingsCode = g.getCode().toUpperCase();
                switch (globalSettingsCode) {
                    case (MULTIUSER_MODE):
                        hasMultiuserMode = true;
                        break;
                    case (POST_PREMODERATION):
                        hasPostPremoderation = true;
                        break;
                    case (STATISTICS_IS_PUBLIC):
                        hasStatisticsIsPublic = true;
                        break;
                }
            }
            setDefaultsToMissedSettings(gsSet, hasMultiuserMode, hasPostPremoderation, hasStatisticsIsPublic);
        }
        log.info("--- Возвращен список настроек: " + gsSet.toString());
        return gsSet;
    }

    private void setDefaultsToMissedSettings(HashSet<GlobalSettings> gsSet, boolean hasMultiuserMode,
                                             boolean hasPostPremoderation, boolean hasStatisticsIsPublic) {
        if (!hasMultiuserMode) gsSet.add(addNewGlobalSetting(
                MULTIUSER_MODE, MULTIUSER_MODE_NAME, multiuserDefaultValue));
        if (!hasPostPremoderation) gsSet.add(addNewGlobalSetting(
                POST_PREMODERATION, POST_PREMODERATION_NAME, postPremoderationDefaultValue));
        if (!hasStatisticsIsPublic) gsSet.add(addNewGlobalSetting(
                STATISTICS_IS_PUBLIC, STATISTICS_IS_PUBLIC_NAME, statisticsDefaultValue));
    }

    private void setDefaultGlobalSettings(HashSet<GlobalSettings> gsSet) {
        log.info("--- Отсутствуют данные по глобальным настройкам. Устанавливаются значения по-умолчанию");
        gsSet.add(addNewGlobalSetting(MULTIUSER_MODE, MULTIUSER_MODE_NAME, multiuserDefaultValue));
        gsSet.add(addNewGlobalSetting(POST_PREMODERATION, POST_PREMODERATION_NAME, postPremoderationDefaultValue));
        gsSet.add(addNewGlobalSetting(STATISTICS_IS_PUBLIC, STATISTICS_IS_PUBLIC_NAME, statisticsDefaultValue));
    }

    private GlobalSettings setGSetting(GlobalSettings g, boolean setting) {
        g.setValue(convertBooleanToYesOrNo(setting));
        g = globalSettingsRepository.save(g);
        log.info("--- Установлена настройка " + g.getCode() + " со значением " + g.getValue());
        return g;
    }

    private GlobalSettings addNewGlobalSetting(String code, String name, String value) {
        GlobalSettings g = new GlobalSettings(code, name, value);
        g = globalSettingsRepository.save(g);
        log.info("--- Установлена глобальная настройка: " + g.getCode() + " : " + g.getValue());
        return g;
    }

    private String convertBooleanToYesOrNo(boolean bool) {
        if (bool) {
            return "YES";
        } else return "NO";
    }

    private Boolean yesOrNoToBoolean(String yesOrNo) {
        if (yesOrNo.toUpperCase().equals("YES")) {
            return true;
        } else if (yesOrNo.toUpperCase().equals("NO")) {
            return false;
        } else return null;
    }
}
