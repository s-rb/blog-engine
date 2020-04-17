package main.services;

import lombok.extern.slf4j.Slf4j;
import main.api.request.PostVoteRequest;
import main.api.response.BadRequestMsgWithErrorsResponse;
import main.api.response.BooleanResponse;
import main.api.response.ResponseApi;
import main.model.entities.Post;
import main.model.entities.PostVote;
import main.model.entities.User;
import main.model.repositories.PostVoteRepository;
import main.services.interfaces.PostRepositoryService;
import main.services.interfaces.PostVoteRepositoryService;
import main.services.interfaces.UserRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;

@Slf4j
@Service
public class PostVoteRepositoryServiceImpl implements PostVoteRepositoryService {

    @Autowired
    private PostVoteRepository postVoteRepository;
    @Autowired
    private UserRepositoryService userRepositoryService;
    @Autowired
    private PostRepositoryService postRepositoryService;

    @Override
    public ResponseEntity<ResponseApi> likePost(PostVoteRequest postVoteRequest, HttpSession session) {
        return setPostVote(postVoteRequest, session, true);
    }

    @Override
    public ResponseEntity<ResponseApi> dislikePost(PostVoteRequest postVoteRequest, HttpSession session) {
        return setPostVote(postVoteRequest, session, false);
    }

    private ResponseEntity<ResponseApi> setPostVote(PostVoteRequest postVoteRequest,
                                                    HttpSession session, boolean isLike) {
        int postId = postVoteRequest.getPostId();

        User user = userRepositoryService.getUserBySession(session);
        if (user == null) {
            log.warn("--- Не найден пользователь по номеру сессии: " + session.getId());
            return new ResponseEntity<>(
                    new BadRequestMsgWithErrorsResponse("Пользователь не авторизован"),
                    HttpStatus.BAD_REQUEST);
        }
        ResponseEntity<ResponseApi> response =
                new ResponseEntity<>(new BooleanResponse(false), HttpStatus.BAD_REQUEST);
        Post currentPost = postRepositoryService.getPostById(postId);
        if (currentPost == null) {
            log.warn("--- Не найден пост с id: " + postId);
            return new ResponseEntity<>(
                    new BadRequestMsgWithErrorsResponse("Пост не найден"),
                    HttpStatus.BAD_REQUEST);
        }
        // проверяем лайкал/дизлайкал ли юзер ранее этот пост
        PostVote beforeLike = postVoteRepository.getPostVoteByUserIdAndPostId(postId, user.getId());
        if (beforeLike == null)
        { // Не было лайков и диз
            PostVote newPostVote = postVoteRepository
                    .save(new PostVote(user, currentPost, LocalDateTime.now(), (byte) (isLike ? 1 : -1)));
            log.info("--- Создан postVote поста с id:" + postId + ", пользователем с id:" + user.getId() +
                    ", postVoteID:" + newPostVote.getId() + ", postVoteValue: " + newPostVote.getValue());
            response = new ResponseEntity<>(new BooleanResponse(true), HttpStatus.OK);
        }
        else if (beforeLike.getValue() == (isLike ? 1 : -1))
        { // Ранее был лайк
            log.info("--- Повторный лайк/дизлайк поста с id:" + postId + ", пользователем с id:" + user.getId());
            response = new ResponseEntity<>(
                    new BadRequestMsgWithErrorsResponse("Повторный лайк/дизлайк"),
                    HttpStatus.BAD_REQUEST);
        }
        else if (beforeLike.getValue() == (isLike ? -1 : 1))
        { // ранее был противоположный postVote, удаляем
            postVoteRepository.delete(beforeLike);
            log.info("--- Удален лайк/дизлайк поста с id:" + postId + ", пользователем с id:" + user.getId());
            // Для создания нового лайка/дизлайка, а не только удаления (для синхр. с фронтом, а не док.API):
            PostVote newPostVote = postVoteRepository
                    .save(new PostVote(user, currentPost, LocalDateTime.now(), (byte) (isLike ? 1 : -1)));
            log.info("--- Создан postVote поста с id:" + postId + ", пользователем с id:" + user.getId() +
                    ", postVoteID:" + newPostVote.getId() + ", postVoteValue: " + newPostVote.getValue());
            response = new ResponseEntity<>(new BooleanResponse(true), HttpStatus.OK);
        }
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }
}