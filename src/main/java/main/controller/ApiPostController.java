package main.controller;

import lombok.extern.slf4j.Slf4j;
import main.api.request.AddPostRequest;
import main.api.request.PostVoteRequest;
import main.api.response.ResponseApi;
import main.services.interfaces.PostRepositoryService;
import main.services.interfaces.PostVoteRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.text.ParseException;

@Slf4j
@RestController
@ComponentScan("service")
public class ApiPostController {

    @Autowired
    private PostRepositoryService postRepoService;
    @Autowired
    private PostVoteRepositoryService postVoteRepoService;

    public ApiPostController() {
    }

    @GetMapping(value = "/api/post", params = {"offset", "limit", "mode"})
    public ResponseEntity<ResponseApi> get(@RequestParam(value = "offset") int offset,  // Может иметь defaultValue = "10"
                                           @RequestParam(value = "limit") int limit,
                                           @RequestParam(value = "mode") String mode) {
        log.info("--- Получен GET запрос на /api/post со следующими параметрами: {" +
                "offset:" + offset + "," +
                "limit:" + limit + "," +
                "mode:" + mode
                + "}");
        return postRepoService.getPostsWithParams(offset, limit, mode);
    }

    @GetMapping(value = "/api/post/search", params = {"offset", "limit", "query"})
    public ResponseEntity<ResponseApi> searchPosts(@RequestParam(value = "offset") int offset,
                                                   @RequestParam(value = "limit") int limit,
                                                   @RequestParam(value = "query") String query) {
        log.info("--- Получен GET запрос на /api/post/search со следующими параметрами: {" +
                "offset:" + offset + "," +
                "limit:" + limit + "," +
                "query:" + query
                + "}");
        return postRepoService.searchPosts(offset, query, limit);
    }

    @GetMapping(value = "/api/post/{id}")
    public ResponseEntity<ResponseApi> getPost(@PathVariable int id, HttpServletRequest request) {
        log.info("--- Получен GET запрос на /api/post/{id} со следующими параметрами: {" +
                "id:" + id + "," +
                "sessionId:" + request.getSession().getId()
                + "}");
        return postRepoService.getPost(id, request.getSession());
    }

    @GetMapping(value = "/api/post/byDate", params = {"offset", "limit", "date"})
    public ResponseEntity<ResponseApi> getPostsByDate(@RequestParam(value = "offset") int offset,
                                                      @RequestParam(value = "limit") int limit,
                                                      @RequestParam(value = "date") String date) {
        log.info("--- Получен GET запрос на /api/post/byDate со следующими параметрами: {" +
                "offset:" + offset + "," +
                "limit:" + limit + "," +
                "date:" + date
                + "}");
        return postRepoService.getPostsByDate(date, offset, limit);
    }

    @GetMapping(value = "/api/post/byTag", params = {"tag", "offset", "limit"})
    public ResponseEntity<ResponseApi> get(@RequestParam(value = "limit") int limit,
                                           @RequestParam(value = "tag") String tag,
                                           @RequestParam(value = "offset") int offset) {
        log.info("--- Получен GET запрос на /api/post/byTag со следующими параметрами: {" +
                "offset:" + offset + "," +
                "limit:" + limit + "," +
                "tag:" + tag
                + "}");
        return postRepoService.getPostsByTag(limit, tag, offset);
    }

    @GetMapping(value = "/api/post/moderation", params = {"status", "offset", "limit"})
    public ResponseEntity<ResponseApi> getPostsForModeration(@RequestParam(value = "status") String status,
                                                             @RequestParam(value = "offset") int offset,
                                                             @RequestParam(value = "limit") int limit,
                                                             HttpServletRequest request) {
        log.info("--- Получен GET запрос на /api/post/moderation со следующими параметрами: {" +
                "offset:" + offset + "," +
                "limit:" + limit + "," +
                "status:" + status + "," +
                "sessionID:" + request.getSession().getId()
                + "}");
        return postRepoService.getPostsForModeration(status, offset, limit, request.getSession());
    }

    @GetMapping(value = "/api/post/my", params = {"status", "offset", "limit"})
    public ResponseEntity<ResponseApi> getMyPosts(@RequestParam(value = "status") String status,
                                                  @RequestParam(value = "offset") int offset,
                                                  @RequestParam(value = "limit") int limit,
                                                  HttpServletRequest request) {
        log.info("--- Получен GET запрос на /api/post/my со следующими параметрами: {" +
                "offset:" + offset + "," +
                "limit:" + limit + "," +
                "status:" + status + "," +
                "sessionID:" + request.getSession().getId()
                + "}");
        return postRepoService.getMyPosts(status, offset, limit, request.getSession());
    }

    @PostMapping(value = "/api/post")
    public ResponseEntity<ResponseApi> post(@RequestBody AddPostRequest addPostRequest,
                                            HttpServletRequest request) throws ParseException {
        log.info("--- Получен POST запрос на /api/post со следующими параметрами: {" +
                "sessionID:" + request.getSession().getId() + "," +
                "title:" + addPostRequest.getTitle() + "," +
                "text:" + addPostRequest.getText() + "," +
                "time:" + addPostRequest.getTime() + "," +
                "isActive:" + addPostRequest.getActive() + "," +
                "tags:" + addPostRequest.getTags()
                + "}");
        return postRepoService.addNewPost(addPostRequest, request.getSession());
    }

    @PutMapping(value = "/api/post/{id}")
    public ResponseEntity<ResponseApi> editPost(@PathVariable int id,
                                                @RequestBody AddPostRequest addPostRequest,
                                                HttpServletRequest request) {
        log.info("--- Получен PUT запрос на /api/post/{id} со следующими параметрами: {" +
                "id:" + id + "," +
                "sessionID:" + request.getSession().getId() + "," +
                "title:" + addPostRequest.getTitle() + "," +
                "text:" + addPostRequest.getText() + "," +
                "time:" + addPostRequest.getTime() + "," +
                "isActive:" + addPostRequest.getActive() + "," +
                "tags:" + addPostRequest.getTags()
                + "}");
        return postRepoService.editPost(id, addPostRequest, request.getSession());
    }

    @PostMapping(value = "/api/post/like")
    public ResponseEntity<ResponseApi> likePost(@RequestBody PostVoteRequest postVoteRequest, HttpServletRequest request) {
        log.info("--- Получен POST запрос на /api/post/like со следующими параметрами: {" +
                "sessionID:" + request.getSession().getId() + "," +
                "postId" + postVoteRequest.getPostId()
                + "}");
        return postVoteRepoService.likePost(postVoteRequest, request.getSession());
    }

    @PostMapping(value = "/api/post/dislike")
    public ResponseEntity<ResponseApi> dislikePost(@RequestBody PostVoteRequest postVoteRequest,
                                                   HttpServletRequest request) {
        log.info("--- Получен POST запрос на /api/post/dislike со следующими параметрами: {" +
                "sessionID:" + request.getSession().getId() + "," +
                "postId" + postVoteRequest.getPostId()
                + "}");
        return postVoteRepoService.dislikePost(postVoteRequest, request.getSession());
    }
}
