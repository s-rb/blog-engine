package main.services;

import lombok.extern.slf4j.Slf4j;
import main.api.request.AddCommentRequest;
import main.api.response.AddCommentResponse;
import main.api.response.BadRequestMessageResponse;
import main.api.response.ResponseApi;
import main.model.entities.GlobalSettings;
import main.model.entities.Post;
import main.model.entities.PostComment;
import main.model.entities.User;
import main.model.repositories.PostCommentRepository;
import main.services.interfaces.GlobalSettingsRepositoryService;
import main.services.interfaces.PostCommentRepositoryService;
import main.services.interfaces.PostRepositoryService;
import main.services.interfaces.UserRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpSession;
import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
public class PostCommentRepositoryServiceImpl implements PostCommentRepositoryService {

    @Value("${post_comment.min_length}")
    private int minCommentLength;
    @Value("${post_comment.max_length}")
    private int maxCommentLength;

    @Autowired
    private PostCommentRepository postCommentRepository;
    @Autowired
    private UserRepositoryService userRepoService;
    @Autowired
    private PostRepositoryService postRepositoryService;
    @Autowired
    private GlobalSettingsRepositoryService globalSettingsRepositoryService;

    @Override
    public ResponseEntity<ResponseApi> addComment(AddCommentRequest addCommentRequest, HttpSession session) {
        Integer parentId = addCommentRequest.getParentId();
        Integer postId = addCommentRequest.getPostId();
        if (parentId == null && postId == null) {
            log.warn("--- Не заданы родительского поста/комментария");
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Не заданы родительский пост и комментарий"),
                    HttpStatus.BAD_REQUEST);
        }

        String text = addCommentRequest.getText();
        if (!isTextValid(text)) {
            String message = "Текст комментария не задан, слишком короткий (<" + minCommentLength
                    + " символов) или превышает максимальный размер (" + maxCommentLength + " символов)";
            log.warn("--- " + message);
            return new ResponseEntity<>(
                    new BadRequestMessageResponse(message), HttpStatus.BAD_REQUEST);
        }
        PostComment parentComment = null;
        Post parentPost = null;
        if (parentId != null) {
            parentComment = getPostCommentById(parentId);
        }
        if (postId != null) {
            parentPost = postRepositoryService.getPostById(postId);
        }
        if (parentComment == null && parentPost == null) {
            log.warn("--- Не удалось найти пост/комментарий с указанными id: [" +
                    "postId:" + postId + "," + "parentCommentID:" + postId + "]");
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Не найдены родительский пост и комментарий"),
                    HttpStatus.BAD_REQUEST);
        }

        User user = userRepoService.getUserBySession(session);
        if (user == null) {
            log.warn("--- Не найден пользователь по номеру сессии: " + session.getId());
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пользователь не авторизован"),
                    HttpStatus.BAD_REQUEST);
        }
        if (!user.isModerator() && !isMultiuserMode())
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Для добавления комментария требуется включить " +
                            "многопользовательский режим и/или требуются права модератора"),
                    HttpStatus.BAD_REQUEST);

        PostComment newComment = postCommentRepository.save(
                new PostComment(parentComment, user, parentPost, LocalDateTime.now(), text));
        log.info("--- Создан новый комментарий с id:" + newComment.getId());
        ResponseEntity<ResponseApi> response =
                new ResponseEntity<>(new AddCommentResponse(newComment), HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }

    @Override
    public PostComment getPostCommentById(int id) {
        Optional<PostComment> optionalComment = postCommentRepository.findById(id);
        return optionalComment.orElse(null);
    }

    private boolean isTextValid(String text) {
        if (text == null || text.equals("")) return false;
        String cleanText = HtmlParserServiceImpl.getTextStringFromHtml(text);
        assert cleanText != null;
        return cleanText.length() <= maxCommentLength && cleanText.length() >= minCommentLength;
    }

    private boolean isMultiuserMode() {
        return isCurrentSettingOn(GlobalSettingsRepositoryService.MULTIUSER_MODE);
    }

    private boolean isCurrentSettingOn(String globalSettingCode) {
        Optional<GlobalSettings> optionalPostPremoder = globalSettingsRepositoryService.getAllGlobalSettingsSet()
                .stream().filter(g -> g.getCode()
                        .equalsIgnoreCase(globalSettingCode)).findFirst();
        return optionalPostPremoder.isPresent()
                && optionalPostPremoder.get().getValue().equalsIgnoreCase("YES");
    }
}
