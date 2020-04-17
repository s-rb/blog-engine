package main.controller;

import lombok.extern.slf4j.Slf4j;
import main.api.request.*;
import main.api.response.ResponseApi;
import main.services.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@Slf4j
@RestController
@ComponentScan("service")
public class ApiGeneralController {

    @Autowired
    private PostRepositoryService postRepoService;
    @Autowired
    private UserRepositoryService userRepoService;
    @Autowired
    private GlobalSettingsRepositoryService globalSettingsRepoService;
    @Autowired
    private TagRepositoryService tagRepoService;
    @Autowired
    private GeneralDataService generalDataService;
    @Autowired
    private PostCommentRepositoryService commentRepoService;

    public ApiGeneralController() {
    }

    @GetMapping(value = "/api/init")
    public ResponseEntity<ResponseApi> getData() {
        log.info("--- Получен GET запрос на /api/init");
        return generalDataService.getData();
    }

    @PostMapping(value = "/api/image")
    public ResponseEntity<?> uploadImage(@RequestParam MultipartFile image,
                                         HttpServletRequest request) {
        log.info("--- Получен POST запрос на /api/image со следующими параметрами: {" +
                "SessionID:" + request.getSession().getId() + "," +
                "ImageFileName:" + image.getOriginalFilename() + "," +
                "ImageFileSize:" + image.getSize()
                + "}");
        ResponseEntity<?> responseEntity = null;
        try {
            responseEntity = postRepoService.uploadImage(image, request.getSession());
        } catch (IOException e) {
            e.printStackTrace();
            log.error("--- При загрузке файла (POST на /api/image выброшено исключение", e);
        }
        return responseEntity;
    }

    @PostMapping(value = "/api/comment")
    public ResponseEntity<ResponseApi> addComment(@RequestBody AddCommentRequest addCommentRequest,
                                                  HttpServletRequest request) {
        log.info("--- Получен POST запрос на /api/comment со следующими параметрами: {" +
                "SessionID:" + request.getSession().getId() + "," +
                "ParentCommentID:" + addCommentRequest.getParentId() + "," +
                "PostID:" + addCommentRequest.getPostId() + "," +
                "Text:" + addCommentRequest.getText()
                + "}");
        return commentRepoService.addComment(addCommentRequest, request.getSession());
    }

    @GetMapping(value = "/api/tag", params = {"query"})
    public ResponseEntity<ResponseApi> getTags(@RequestParam(value = "query") String query) {
        log.info("--- Получен GET запрос на /api/tag со следующими параметрами: {" +
                "Query:" + query
                + "}");
        return tagRepoService.getTags(query);
    }

    @GetMapping(value = "/api/tag")
    public ResponseEntity<ResponseApi> getTagsWithoutQuery() {
        log.info("--- Получен GET запрос на /api/tag");
        return tagRepoService.getTagsWithoutQuery();
    }

    @PostMapping(value = "/api/moderation")
    public ResponseEntity<ResponseApi> moderatePost(@RequestBody ModeratePostRequest moderatePostRequest,
                                                    HttpServletRequest request) {
        log.info("--- Получен POST запрос на /api/moderation со следующими параметрами: {" +
                "SessionID:" + request.getSession().getId() + "," +
                "Decision:" + moderatePostRequest.getDecision() + "," +
                "PostID:" + moderatePostRequest.getPostId()
                + "}");
        return postRepoService.moderatePost(moderatePostRequest, request.getSession());
    }

    @GetMapping(value = "/api/calendar", params = {"year"})
    public ResponseEntity<ResponseApi> countPostByYear(@RequestParam(value = "year") Integer year) {
        log.info("--- Получен GET запрос на /api/calendar со следующими параметрами: {" +
                "Year:" + year
                + "}");
        return postRepoService.countPostsByYear(year);
    }

    @PostMapping(value = "/api/profile/my")
    public ResponseEntity<ResponseApi> editProfile(@RequestBody EditProfileRequest editProfileRequest,
                                                   HttpServletRequest request) {
        log.info("--- Получен POST запрос на /api/profile/my со следующими параметрами: {" +
                "SessionID:" + request.getSession().getId() + "," +
                "Email:" + editProfileRequest.getEmail() + "," +
                "Name:" + editProfileRequest.getName() + "," +
                "Password:" + editProfileRequest.getPassword() + "," +
                "RemovePhoto:" + editProfileRequest.getRemovePhoto()
                + "}");
        return userRepoService.editProfile(editProfileRequest, request.getSession());
    }

    @PostMapping(value = "/api/profile/my", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseApi> editProfileWithPhoto(@ModelAttribute EditProfileWithPhotoRequest editProfileRequest,
                                                            HttpServletRequest request) {
        log.info("--- Получен POST запрос на /api/profile/my со следующими параметрами: {" +
                "SessionID:" + request.getSession().getId() + "," +
                "Email:" + editProfileRequest.getEmail() + "," +
                "Name:" + editProfileRequest.getName() + "," +
                "Password:" + editProfileRequest.getPassword() + "," +
                "RemovePhoto:" + editProfileRequest.getRemovePhoto() + "," +
                "PhotoFileName:" + editProfileRequest.getPhoto().getOriginalFilename() + "," +
                "PhotoFileSize:" + editProfileRequest.getPhoto().getSize()
                + "}");
        return userRepoService.editProfile(editProfileRequest, request.getSession());
    }

    @GetMapping(value = "/api/statistics/my")
    public ResponseEntity<?> getMyStatistics(HttpServletRequest request) {
        log.info("--- Получен GET запрос на /api/statistics/my со следующими параметрами: {" +
                "SessionID:" + request.getSession().getId()
                + "}");
        return userRepoService.getMyStatistics(request.getSession());
    }

    @GetMapping(value = "/api/statistics/all")
    public ResponseEntity<?> getAllStatistics(HttpServletRequest request) {
        log.info("--- Получен GET запрос на /api/statistics/all со следующими параметрами: {" +
                "SessionID:" + request.getSession().getId()
                + "}");
        return userRepoService.getAllStatistics(request.getSession());
    }

    @GetMapping(value = "/api/settings")
    public ResponseEntity<?> getGlobalSettings() {
        log.info("--- Получен GET запрос на /api/settings");
        return globalSettingsRepoService.getGlobalSettings();
    }

    @PutMapping(value = "/api/settings")
    public ResponseEntity<?> setGlobalSettings(@RequestBody SetGlobalSettingsRequest setGlobalSettingsRequest,
                                               HttpServletRequest request) {
        log.info("--- Получен PUT запрос на /api/settings со следующими параметрами: {" +
                "SessionID:" + request.getSession().getId() + "," +
                "MultiuserMode:" + setGlobalSettingsRequest.getMultiuserMode() + "," +
                "PostPremoderation:" + setGlobalSettingsRequest.getPostPremoderation() + "," +
                "StatisticsIsPublic:" + setGlobalSettingsRequest.getStatisticsIsPublic()
                + "}");
        return globalSettingsRepoService.setGlobalSettings(setGlobalSettingsRequest,
                request.getSession());
    }
}
