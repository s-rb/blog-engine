package main.services.interfaces;

import main.api.request.SetGlobalSettingsRequest;
import main.model.entities.GlobalSettings;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpSession;
import java.util.HashSet;

public interface GlobalSettingsRepositoryService {

    String MULTIUSER_MODE = "MULTIUSER_MODE";
    String POST_PREMODERATION = "POST_PREMODERATION";
    String STATISTICS_IS_PUBLIC = "STATISTICS_IS_PUBLIC";
    String MULTIUSER_MODE_NAME = "Многопользовательский режим";
    String POST_PREMODERATION_NAME = "Премодерация постов";
    String STATISTICS_IS_PUBLIC_NAME = "Показывать всем статистику блога";

    ResponseEntity<?> getGlobalSettings();

    ResponseEntity<?> setGlobalSettings(SetGlobalSettingsRequest setGlobalSettingsRequest,
                                        HttpSession session);

    HashSet<GlobalSettings> getAllGlobalSettingsSet();
}
