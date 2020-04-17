package main.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PostVoteRequest implements RequestApi {
    @JsonProperty("post_id")
    private int postId;

    public PostVoteRequest() {
    }

    public PostVoteRequest(int postId) {
        this.postId = postId;
    }

    public int getPostId() {
        return postId;
    }

    public void setPostId(int postId) {
        this.postId = postId;
    }
}