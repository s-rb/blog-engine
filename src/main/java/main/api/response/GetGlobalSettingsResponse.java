package main.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.model.entities.GlobalSettings;
import main.services.interfaces.GlobalSettingsRepositoryService;

import java.util.Map;
import java.util.Set;

public class GetGlobalSettingsResponse implements ResponseApi {

    @JsonProperty(GlobalSettingsRepositoryService.MULTIUSER_MODE)
    private Boolean multiuserMode;
    @JsonProperty(GlobalSettingsRepositoryService.POST_PREMODERATION)
    private Boolean postPremoderation;
    @JsonProperty(GlobalSettingsRepositoryService.STATISTICS_IS_PUBLIC)
    private Boolean statisticsIsPublic;

    public GetGlobalSettingsResponse(Map<String, Boolean> settings) {
        for (String key : settings.keySet()) {
            switch (key) {
                case (GlobalSettingsRepositoryService.MULTIUSER_MODE):
                    multiuserMode = settings.get(GlobalSettingsRepositoryService.MULTIUSER_MODE);
                    break;
                case (GlobalSettingsRepositoryService.POST_PREMODERATION):
                    postPremoderation = settings.get(GlobalSettingsRepositoryService.POST_PREMODERATION);
                    break;
                case (GlobalSettingsRepositoryService.STATISTICS_IS_PUBLIC):
                    statisticsIsPublic = settings.get(GlobalSettingsRepositoryService.STATISTICS_IS_PUBLIC);
                    break;
            }
        }
    }

    public GetGlobalSettingsResponse(Set<GlobalSettings> settings) {
        for (GlobalSettings g : settings) {
            String settingName = g.getCode().toUpperCase();
            switch (settingName) {
                case GlobalSettingsRepositoryService.MULTIUSER_MODE: {
                    multiuserMode = yesOrNoToBoolean(g.getValue());
                    break;
                }
                case GlobalSettingsRepositoryService.POST_PREMODERATION: {
                    postPremoderation = yesOrNoToBoolean(g.getValue());
                    break;
                }
                case GlobalSettingsRepositoryService.STATISTICS_IS_PUBLIC: {
                    statisticsIsPublic = yesOrNoToBoolean(g.getValue());
                    break;
                }
            }
        }
    }

    public Boolean getMultiuserMode() {
        return multiuserMode;
    }

    public void setMultiuserMode(Boolean multiuserMode) {
        this.multiuserMode = multiuserMode;
    }

    public Boolean getPostPremoderation() {
        return postPremoderation;
    }

    public void setPostPremoderation(Boolean postPremoderation) {
        this.postPremoderation = postPremoderation;
    }

    public Boolean getStatisticsIsPublic() {
        return statisticsIsPublic;
    }

    public void setStatisticsIsPublic(Boolean statisticsIsPublic) {
        this.statisticsIsPublic = statisticsIsPublic;
    }

    private String booleanToYesOrNo(boolean bool) {
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

    @Override
    public String toString() {
        return "Settings{" +
                "multiuserMode=" + multiuserMode +
                ", postPremoderation=" + postPremoderation +
                ", statisticsIsPublic=" + statisticsIsPublic +
                '}';
    }
}