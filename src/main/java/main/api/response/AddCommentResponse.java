package main.api.response;

import main.model.entities.PostComment;

public class AddCommentResponse implements ResponseApi {

    private int id;

    public AddCommentResponse(PostComment postComment) {
        id = postComment.getId();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                '}';
    }
}
