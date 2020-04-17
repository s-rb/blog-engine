package main.services.interfaces;

import main.api.request.PostVoteRequest;
import main.api.response.ResponseApi;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpSession;

public interface PostVoteRepositoryService {

    ResponseEntity<ResponseApi> likePost(PostVoteRequest postVoteRequest, HttpSession session);

    ResponseEntity<ResponseApi> dislikePost(PostVoteRequest postVoteRequest, HttpSession session);
}
