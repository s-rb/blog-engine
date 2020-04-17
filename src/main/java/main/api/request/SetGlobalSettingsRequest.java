package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import main.services.interfaces.GlobalSettingsRepositoryService;

public class SetGlobalSettingsRequest implements RequestApi {

    @JsonProperty(GlobalSettingsRepositoryService.MULTIUSER_MODE)
    private Boolean multiuserMode = null;
    @JsonProperty(GlobalSettingsRepositoryService.POST_PREMODERATION)
    private Boolean postPremoderation = null;
    @JsonProperty(GlobalSettingsRepositoryService.STATISTICS_IS_PUBLIC)
    private Boolean statisticsIsPublic = null;

    public SetGlobalSettingsRequest() {
    }

    public SetGlobalSettingsRequest(Boolean multiuserMode, Boolean postPremoderation, Boolean statisticsIsPublic) {
        this.multiuserMode = multiuserMode;
        this.postPremoderation = postPremoderation;
        this.statisticsIsPublic = statisticsIsPublic;
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
}