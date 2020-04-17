package main.services.interfaces;

import main.api.request.AddCommentRequest;
import main.api.response.ResponseApi;
import main.model.entities.PostComment;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpSession;

public interface PostCommentRepositoryService {

    ResponseEntity<ResponseApi> addComment(AddCommentRequest addCommentRequest, HttpSession session);

    PostComment getPostCommentById(int id);
}