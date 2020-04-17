package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ModeratePostRequest implements RequestApi {
    @JsonProperty("post_id")
    private Integer postId;
    private String decision;

    public ModeratePostRequest(Integer postId, String decision) {
        this.postId = postId;
        this.decision = decision;
    }

    public ModeratePostRequest() {
    }

    public Integer getPostId() {
        return postId;
    }

    public void setPostId(Integer postId) {
        this.postId = postId;
    }

    public String getDecision() {
        return decision;
    }

    public void setDecision(String decision) {
        this.decision = decision;
    }
}