package main.services.interfaces;

import main.api.request.AddPostRequest;
import main.api.request.ModeratePostRequest;
import main.api.response.ResponseApi;
import main.model.entities.Post;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.text.ParseException;

public interface PostRepositoryService {

    ResponseEntity<ResponseApi> getPostsWithParams(int offset, int limit, String mode);

    ResponseEntity<ResponseApi> searchPosts(int offset, String query, int limit);

    ResponseEntity<ResponseApi> getPostsByDate(String date, int offset, int limit);

    ResponseEntity<ResponseApi> getPostsByTag(int limit, String tag, int offset);

    ResponseEntity<ResponseApi> getPostsForModeration(String status, int offset, int limit, HttpSession session);

    ResponseEntity<ResponseApi> getMyPosts(String status, int offset, int limit, HttpSession session);

    ResponseEntity<ResponseApi> addNewPost(AddPostRequest addPostRequest,
                                           HttpSession session) throws ParseException;

    ResponseEntity<?> uploadImage(MultipartFile image, HttpSession session) throws IOException;

    ResponseEntity<ResponseApi> editPost(int id, AddPostRequest addPostRequest,
                                         HttpSession session);

    ResponseEntity<ResponseApi> moderatePost(ModeratePostRequest moderatePostRequest, HttpSession session);

    ResponseEntity<ResponseApi> countPostsByYear(Integer year);

    Post getPostById(int postId);

    ResponseEntity<ResponseApi> getPost(int id, HttpSession session);

    Post getPostByTitle(String title);
}