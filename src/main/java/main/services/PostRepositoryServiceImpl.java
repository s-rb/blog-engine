package main.services;

import lombok.extern.slf4j.Slf4j;
import main.api.request.AddPostRequest;
import main.api.request.ModeratePostRequest;
import main.api.response.*;
import main.model.ModerationStatus;
import main.model.entities.*;
import main.model.repositories.PostRepository;
import main.model.repositories.TagRepository;
import main.services.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

@Slf4j
@Service
public class PostRepositoryServiceImpl implements PostRepositoryService {
    public static final String PARSE_TIME_PATTERN1 = "yyyy-MM-dd HH:mm";
    public static final String PARSE_TIME_PATTERN2 = "yyyy-MM-dd'T'HH:mm";
    public static final String PARSE_DATE_PATTERN = "yyyy-MM-dd";
    @Value("${post.image.root_folder}")
    private String imagesRootFolder;
    @Value("${post.body.min_length}")
    private int postBodyMinLength;
    @Value("${post.body.max_length}")
    private int postBodyMaxLength;
    @Value("${post.title.min_length}")
    private int postTitleMinLength;
    @Value("${post.title.max_length}")
    private int postTitleMaxLength;
    @Value("${post.image.upload_folder}")
    private String imagesUploadFolder;
    @Value("${post.image.format}")
    private String imagesFormat;
    @Value("${post.default_limit_per_page}")
    private int defaultPostsLimitPerPage;
    @Value("${post.announce.max_length}")
    private int announceLength;
    @Value("${user.password.hashing_algorithm}")
    private String hashingAlgorithm;

    @Autowired
    private PostRepository postRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private TagRepositoryService tagRepositoryService;
    @Autowired
    private UserRepositoryService userRepositoryService;
    @Autowired
    private TagToPostRepositoryService tagToPostRepositoryService;
    @Autowired
    private FileSystemService fileSystemService;
    @Autowired
    private GlobalSettingsRepositoryService globalSettingsRepositoryService;

    public ResponseEntity<ResponseApi> getPostForUnloginnedUser(Post post) {
        if (!post.isActive() || !post.getModerationStatus().equals(ModerationStatus.ACCEPTED)
                || !post.getTime().isBefore(LocalDateTime.now())) {
            log.warn("--- Данные поста не соответствуют настройкам отображения: " + post.toString());
            return new ResponseEntity<>(new BadRequestMessageResponse(
                    !post.isActive() ? "Публикация скрыта" : "",
                    !post.getModerationStatus().equals(ModerationStatus.ACCEPTED) ? "Требуется модерация" : "",
                    !post.getTime().isBefore(LocalDateTime.now())
                            ? "Публикация отложена (дата публикации еще не настала" : ""),
                    HttpStatus.BAD_REQUEST);
        }
        ResponseApi responseApi = new GetPostResponse(post, announceLength);
        post.setViewCount(post.getViewCount() + 1);
        postRepository.save(post);
        log.info("--- Получен пост с id:" + post.getId() + ", количество просмотров увеличено на 1");
        ResponseEntity<ResponseApi> response = new ResponseEntity<ResponseApi>(responseApi, HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }

    @Override
    public ResponseEntity<ResponseApi> getPost(int id, HttpSession session) {
        Post post = getPostById(id);
        if (post == null) {
            log.warn("--- Не удалось найти пост с id:" + id);
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пост не найден"),
                    HttpStatus.BAD_REQUEST);
        }
        User user = userRepositoryService.getUserBySession(session);
        if (user == null || (post.getUser() != user && !user.isModerator()))
            return getPostForUnloginnedUser(post);
        // Если автор или модератор, то просматривает в любом случае, однако просмотры не увеличиваются
        ResponseApi responseApi = new GetPostResponse(post, announceLength);
        ResponseEntity<ResponseApi> response = new ResponseEntity<ResponseApi>(responseApi, HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }

    @Override
    public Post getPostByTitle(String title) {
        return postRepository.getPostByTitle(title);
    }

    public Post getPostById(int id) {
        Optional<Post> optionalPost = postRepository.findById(id);
        return optionalPost.orElse(null);
    }

    public ResponseEntity<ResponseApi> getPostsWithParams(int offset, int limit, String mode) {
        boolean isModeValid = mode.equals("recent") || mode.equals("popular")
                || mode.equals("best") || mode.equals("early");
        if (offset < 0 || limit < 1 || !isModeValid) {
            log.warn("--- Неверный сдвиг, лимит или режим отображения постов: " +
                    "offset:" + offset + "," + "limit:" + limit + "," + "mode:" + mode);
            return new ResponseEntity<>(new BadRequestMessageResponse(
                    offset < 0 ? "Передан отрицательный параметр сдвига" : "",
                    limit < 1 ? "Ограничение количества отображаемых постов менее 1" : "",
                    !isModeValid ? "Режим отображения не распознан" : ""),
                    HttpStatus.BAD_REQUEST);
        }
        List<Post> postsToShow = new ArrayList<>();
        int count = postRepository.countAllPostsAtSite();
        switch (mode.toLowerCase()) {
            case ("recent"):
                postsToShow = postRepository.getRecentPosts(offset, limit);
                break;
            case ("popular"):
                postsToShow = postRepository.getPopularPosts(offset, limit);
                break;
            case ("best"):
                postsToShow = postRepository.getBestPosts(offset, limit);
                break;
            case ("early"):
                postsToShow = postRepository.getEarlyPosts(offset, limit);
                break;
        }
        log.info("--- Для отображения получены посты с параметрами: " +
                "offset:" + offset + "," + "limit:" + limit + "," + "mode:" + mode);
        ResponseApi responseApi = new GetPostsResponse(count, (ArrayList<Post>) postsToShow, announceLength);
        ResponseEntity<ResponseApi> response = new ResponseEntity<>(responseApi, HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }

    public ResponseEntity<ResponseApi> searchPosts(int offset, String query, int limit) {
        if (offset < 0 || limit < 1) {
            log.warn("--- Неверный сдвиг или лимит постов: " + "offset:" + offset + "," + "limit:" + limit);
            return new ResponseEntity<>(new BadRequestMessageResponse(
                    offset < 0 ? "Передан отрицательный параметр сдвига" : "",
                    limit < 1 ? "Ограничение количества отображаемых постов менее 1" : ""),
                    HttpStatus.BAD_REQUEST);
        }
        List<Post> postsToShow = postRepository.searchPosts(offset, limit, query);
        log.info("--- Для отображения получены посты с параметрами: " +
                "offset:" + offset + "," + "limit:" + limit + "," + "query:" + query);
        int count = postRepository.countSearchedPosts(query);
        ResponseApi responseApi = new GetPostsResponse(count, (ArrayList<Post>) postsToShow, announceLength);
        ResponseEntity<ResponseApi> response = new ResponseEntity<>(responseApi, HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }

    public ResponseEntity<ResponseApi> getPostsByDate(String dateString, int offset, int limit) {
        LocalDate date = parseLocalDate(dateString);
        if (offset < 0 || limit < 1 || date == null) {
            log.warn("--- Неверные параметры: " + "offset:" + offset + "," + "limit:" + limit + "," +
                    "date:" + date);
            return new ResponseEntity<>(new BadRequestMessageResponse(
                    offset < 0 ? "Передан отрицательный параметр сдвига" : "",
                    limit < 1 ? "Ограничение количества отображаемых постов менее 1" : "",
                    date == null ? "Дата не задана" : ""),
                    HttpStatus.BAD_REQUEST);
        }
        List<Post> postsToShow = postRepository.getPostsByDate(dateString, limit, offset);
        int count = postRepository.countPostsByDate(dateString);
        log.info("--- Для отображения получены посты с параметрами: " +
                "offset:" + offset + "," + "limit:" + limit + "," + "date:" + dateString);
        ResponseApi responseApi = new GetPostsResponse(count, (ArrayList<Post>) postsToShow, announceLength);
        ResponseEntity<ResponseApi> response = new ResponseEntity<>(responseApi, HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }

    public ResponseEntity<ResponseApi> getPostsByTag(int limit, String tag, int offset) {
        if (offset < 0 || limit < 1 || tag == null || tag.equals("") || tag.isBlank()) {
            log.warn("--- Неверные параметры: " + "offset:" + offset + "," +
                    "limit:" + limit + "," + "tag:" + tag);
            return new ResponseEntity<>(new BadRequestMessageResponse(
                    offset < 0 ? "Передан отрицательный параметр сдвига" : "",
                    limit < 1 ? "Ограничение количества отображаемых постов менее 1" : "",
                    (tag == null || tag.equals("") || tag.isBlank()) ? "Тег не задан" : ""),
                    HttpStatus.BAD_REQUEST);
        }
        List<Post> postsToShow = postRepository.getPostsByTag(tag, limit, offset);
        int count = postRepository.countPostsByTag(tag);
        log.info("--- Для отображения получены посты с параметрами: " +
                "offset:" + offset + "," + "limit:" + limit + "," + "tag:" + tag);
        ResponseApi responseApi = new GetPostsResponse(count, (ArrayList<Post>) postsToShow, announceLength);
        ResponseEntity<ResponseApi> response = new ResponseEntity<>(responseApi, HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }

    public ResponseEntity<ResponseApi> getPostsForModeration(String status, int offset, int limit,
                                                             HttpSession session) {
        boolean isStatusValid = status.equalsIgnoreCase("new")
                || status.equalsIgnoreCase("declined")
                || status.equalsIgnoreCase("accepted");
        if (offset < 0 || limit < 1 || !isStatusValid) {
            log.warn("--- Неверные параметры: " + "offset:" + offset + "," +
                    "limit:" + limit + "," + "status:" + status);
            return new ResponseEntity<>(new BadRequestMessageResponse(
                    offset < 0 ? "Передан отрицательный параметр сдвига" : "",
                    limit < 1 ? "Ограничение количества отображаемых постов менее 1" : "",
                    !isStatusValid ? "Статус модерации не распознан" : ""),
                    HttpStatus.BAD_REQUEST);
        }

        User user = userRepositoryService.getUserBySession(session);
        if (user == null) {
            log.warn("--- Не найден пользователь по номеру сессии: " + session.getId());
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пользователь не авторизован"),
                    HttpStatus.BAD_REQUEST);
        }

        if (!user.isModerator()) {
            log.info("--- Для данного действия пользователю " + user.getId() + ":" + user.getName() + " требуются права модератора");
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Требуются права модератора"),
                    HttpStatus.BAD_REQUEST);
        }

        ArrayList<Post> postsToShow = new ArrayList<>();
        int count = 0;
        switch (status.toLowerCase()) {
            case ("new"):
                postsToShow = (ArrayList<Post>) postRepository.getPostsForModeration(limit, offset);
                count = postRepository.countPostsForModeration();
                break;
            case ("declined"):
                postsToShow = (ArrayList<Post>) postRepository
                        .getPostsModeratedByMe("DECLINED", limit, offset, user.getId());
                count = postRepository.countPostsModeratedByMe("DECLINED", user.getId());
                break;
            case ("accepted"):
                postsToShow = (ArrayList<Post>) postRepository
                        .getPostsModeratedByMe("ACCEPTED", limit, offset, user.getId());
                count = postRepository.countPostsModeratedByMe("ACCEPTED", user.getId());
                break;
        }
        log.info("--- Для отображения получены посты с параметрами: " +
                "offset:" + offset + "," + "limit:" + limit + "," + "status:" + status);
        ResponseApi responseApi = new GetPostsForModerationResponse(count, postsToShow, announceLength);
        ResponseEntity<ResponseApi> response = new ResponseEntity<>(responseApi, HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }

    public ResponseEntity<ResponseApi> getMyPosts(String status, int offset, int limit, HttpSession session) {
        boolean isStatusValid = status.equalsIgnoreCase("inactive")
                || status.equalsIgnoreCase("pending")
                || status.equalsIgnoreCase("declined")
                || status.equalsIgnoreCase("published");
        if (offset < 0 || limit < 1 || !isStatusValid) {
            log.warn("--- Неверные параметры: " +
                    "offset:" + offset + "," + "limit:" + limit + "," + "status:" + status);
            return new ResponseEntity<>(new BadRequestMessageResponse(
                    offset < 0 ? "Передан отрицательный параметр сдвига" : "",
                    limit < 1 ? "Ограничение количества отображаемых постов менее 1" : "",
                    !isStatusValid ? "Статус модерации не распознан" : ""),
                    HttpStatus.BAD_REQUEST);
        }

        User user = userRepositoryService.getUserBySession(session);
        if (user == null) {
            log.warn("--- Не найден пользователь по номеру сессии: " + session.getId());
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пользователь не авторизован"),
                    HttpStatus.BAD_REQUEST);
        }

        ArrayList<Post> postsToShow = new ArrayList<>();
        int count = 0;
        switch (status.toLowerCase()) {
            case ("inactive"):
                postsToShow = (ArrayList<Post>) postRepository.getMyNotActivePosts(user.getId(), limit, offset);
                count = postRepository.countMyNotActivePosts(user.getId());
                break;
            case ("pending"):
                postsToShow = (ArrayList<Post>) postRepository.getMyActivePosts("NEW", limit, offset, user.getId());
                count = postRepository.countMyActivePosts("NEW", user.getId());
                break;
            case ("declined"):
                postsToShow = (ArrayList<Post>) postRepository.getMyActivePosts("DECLINED", limit, offset, user.getId());
                count = postRepository.countMyActivePosts("DECLINED", user.getId());
                break;
            case ("published"):
                postsToShow = (ArrayList<Post>) postRepository.getMyActivePosts("ACCEPTED", limit, offset, user.getId());
                count = postRepository.countMyActivePosts("ACCEPTED", user.getId());
                break;
        }
        log.info("--- Для отображения получены посты с параметрами: " +
                "offset:" + offset + "," + "limit:" + limit + "," + "status:" + status);
        ResponseApi responseApi = new GetPostsResponse(count, postsToShow, announceLength);
        ResponseEntity<ResponseApi> response = new ResponseEntity<>(responseApi, HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }

    public ResponseEntity<ResponseApi> addNewPost(AddPostRequest addPostRequest, HttpSession session) {
        String timeString = addPostRequest.getTime();
        byte active = addPostRequest.getActive();
        String title = addPostRequest.getTitle();
        String text = addPostRequest.getText();
        List<String> postTags = addPostRequest.getTags();
        LocalDateTime time = parseLocalDateTime(timeString);
        time = time == null || time.isBefore(LocalDateTime.now()) ? LocalDateTime.now() : time;
        boolean isTextValidFlag = isTextValid(text);
        boolean isTitleValidFlag = isTitleValid(title);
        if (!isTextValidFlag || !isTitleValidFlag) {
            log.warn("--- Неверные параметры: " + "text:" + text + "," + "title:" + title);
            return new ResponseEntity<>(new BadRequestMessageResponse(
                    !isTextValidFlag ? "Текст не задан, слишком короткий (<" + postBodyMinLength
                            + " символов) или превышает максимальный размер (" + postBodyMaxLength
                            + " символов)" : "",
                    !isTitleValidFlag ? "Заголовок не задан, слишком короткий (<" + postTitleMinLength
                            + " символов) или превышает максимальный размер (" + postTitleMaxLength
                            + " символов)" : ""),
                    HttpStatus.BAD_REQUEST);
        }

        User user = userRepositoryService.getUserBySession(session);
        if (user == null) {
            log.warn("--- Не найден пользователь по номеру сессии: " + session.getId());
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пользователь не авторизован"),
                    HttpStatus.BAD_REQUEST);
        }

        boolean isActive = false;
        if (active == 1) {
            isActive = true;
        }

        if (!user.isModerator() && !isMultiuserMode())
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Для добавления поста требуется включить " +
                            "многопользовательский режим и/или требуются права модератора"),
                    HttpStatus.BAD_REQUEST);

        Post currentPost = postRepository.save(new Post(isActive, isPremoderation() ? ModerationStatus.NEW
                : ModerationStatus.ACCEPTED, user, time, title, text));
        log.info("--- Создан новый пост с id:" + currentPost.getId());
        for (String currentTagName : postTags) {
            if (!currentTagName.isBlank()) {
                Tag currentTag = tagRepository.getTagByTagName(currentTagName) != null ? // Создаем теги, только если их еще не было
                        tagRepository.getTagByTagName(currentTagName) :
                        tagRepositoryService.addTag(new Tag(currentTagName));
                tagToPostRepositoryService.addTagToPost(new TagToPost(currentTag, currentPost));
                log.info("--- К созданному посту с id:" + currentPost.getId() + " добавлен тэг:" + currentTagName);
            }
        }
        ResponseEntity<ResponseApi> response = new ResponseEntity<>(new BooleanResponse(true), HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }

    public ResponseEntity<?> uploadImage(MultipartFile image, HttpSession session) {
        if (image == null) {
            log.warn("--- Отсутствует изображение для загрузки");
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Отсутствует изображение для загрузки"),
                    HttpStatus.BAD_REQUEST);
        }
        User user = userRepositoryService.getUserBySession(session);
        if (user == null) {
            log.warn("--- Не найден пользователь по номеру сессии: " + session.getId());
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пользователь не авторизован"),
                    HttpStatus.BAD_REQUEST);
        }

        if (!Files.exists(Path.of(imagesRootFolder))) {
            fileSystemService.createDirectoriesByPath(imagesRootFolder);
        }
        String fileDestPath;
        String directoryPath = getRandomDirectoryToUpload();
        if (fileSystemService.createDirectoriesByPath(directoryPath)) {
            String imageName = getRandomImageName();
            fileDestPath = directoryPath + "/" + imageName;
            while (Files.exists(Path.of(fileDestPath))) {
                imageName = getRandomImageName();
                fileDestPath = directoryPath + "/" + imageName;
            }
            fileSystemService.copyMultiPartFileToPath(image, Paths.get(directoryPath, imageName));
        } else {
            log.warn("--- Не удалось создать папку: " + directoryPath);
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Не удалось создать папку для загрузки"),
                    HttpStatus.BAD_REQUEST);
        }
        ResponseEntity<String> response = new ResponseEntity<>(fileDestPath, HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }

    public ResponseEntity<ResponseApi> editPost(int id, AddPostRequest addPostRequest, HttpSession session) {
        String timeString = addPostRequest.getTime();
        byte active = addPostRequest.getActive();
        String title = addPostRequest.getTitle();
        String text = addPostRequest.getText();
        List<String> tags = addPostRequest.getTags();
        LocalDateTime time = parseLocalDateTime(timeString);
        Post currentPost = getPostById(id);
        if (currentPost == null) {
            log.warn("--- Не найдет пост с id:" + id);
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пост не найден"),
                    HttpStatus.BAD_REQUEST);
        }
        if (time == null || time.isBefore(LocalDateTime.now())) time = LocalDateTime.now();
        boolean isTextValidFlag = isTextValid(text);
        boolean isTitleValidFlag = isTitleValid(title);
        if (!isTextValidFlag || !isTitleValidFlag) {
            log.warn("--- Неверные параметры: " +
                    "text:" + text + "," + "title:" + title);
            return new ResponseEntity<>(new BadRequestMessageResponse(
                    !isTextValidFlag ? "Текст не задан, слишком короткий (<" + postBodyMinLength
                            + " символов) или превышает максимальный размер (" + postBodyMaxLength
                            + " символов)" : "",
                    !isTitleValidFlag ? "Заголовок не задан, слишком короткий (<" + postTitleMinLength
                            + " символов) или превышает максимальный размер (" + postTitleMaxLength
                            + " символов)" : ""),
                    HttpStatus.BAD_REQUEST);
        }

        User user = userRepositoryService.getUserBySession(session);
        if (user == null) {
            log.warn("--- Не найден пользователь по номеру сессии: " + session.getId());
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пользователь не авторизован"),
                    HttpStatus.BAD_REQUEST);
        }

        if (!user.isModerator() && !(isMultiuserMode() && currentPost.getUser() == user))
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Для редактирования поста требуется включить " +
                            "многопользовательский режим и быть его автором и/или требуются права модератора"),
                    HttpStatus.BAD_REQUEST);
        currentPost.setActive(active == 1);
        currentPost.setTime(time);
        currentPost.setTitle(title);
        currentPost.setText(text);
        currentPost.setModerationStatus(isPremoderation() ? ModerationStatus.NEW : ModerationStatus.ACCEPTED);
        postRepository.save(currentPost); // Пост пересохранили, меняем теги и связи с тегами, удаляя старые и добавляя новые
        currentPost.getTagsToPostsSet().forEach(tag2post -> {
            currentPost.getTagsToPostsSet().remove(tag2post);
            tagToPostRepositoryService.deleteTagToPost(tag2post);
        });
        log.info("--- Удалены теги к посту с id:" + currentPost.getId());
        for (String currentTagName : tags) {
            if (!currentTagName.isBlank()) {
                Tag currentTag = tagRepository.getTagByTagName(currentTagName) != null ? // Создаем теги, только если их еще не было
                        tagRepository.getTagByTagName(currentTagName) :
                        tagRepositoryService.addTag(new Tag(currentTagName));
                tagToPostRepositoryService.addTagToPost(new TagToPost(currentTag, currentPost));
                log.info("--- К посту с id:" + currentPost.getId() + " добавлен тэг {" + currentTagName + "}");
            }
        }
        ResponseEntity<ResponseApi> response = new ResponseEntity<>(new BooleanResponse(true), HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }

    public ResponseEntity<ResponseApi> moderatePost(ModeratePostRequest moderatePostRequest, HttpSession session) {
        int postId = moderatePostRequest.getPostId();
        String decision = moderatePostRequest.getDecision();
        decision = decision.toUpperCase().trim();
        if (!decision.equals("DECLINE") && !decision.equals("ACCEPT")) {
            log.warn("--- Неверный параметр: decision:" + decision);
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Решение по модерации не распознано"),
                    HttpStatus.BAD_REQUEST);
        }
        // Если пользователь залогинен
        User user = userRepositoryService.getUserBySession(session);
        if (user == null) {
            log.warn("--- Не найден пользователь по номеру сессии: " + session.getId());
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пользователь не авторизован"),
                    HttpStatus.BAD_REQUEST);
        }

        if (!user.isModerator()) {
            log.info("--- Для данного действия пользователю " + user.getId() + ":" + user.getName() + " требуются права модератора");
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Для модерации поста требуются права модератора"),
                    HttpStatus.BAD_REQUEST);
        }

        Post post = getPostById(postId);
        if (post == null) {
            log.warn("--- Не найден пост с id: " + postId);
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пост не найден"),
                    HttpStatus.BAD_REQUEST);
        }

        post.setModeratorId(user.getId());
        post.setModerationStatus(decision.equals("DECLINE") ? ModerationStatus.DECLINED
                : ModerationStatus.ACCEPTED);
        postRepository.save(post);
        ResponseEntity<ResponseApi> response = ResponseEntity.status(HttpStatus.OK).body(null);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }

    public ResponseEntity<ResponseApi> countPostsByYear(Integer queriedYear) {
        int year = queriedYear == null ? LocalDateTime.now().getYear() : queriedYear;
        List<Post> postsByYear = postRepository.getPostsByYear(year);
        log.info("--- Получен список постов за " + year + " год");
        HashMap<Date, Integer> postsCountByDate = new HashMap<>();
        for (Post p : postsByYear) {
            Date postDate = Date.valueOf(p.getTime().toLocalDate());
            Integer postCount = postsCountByDate.getOrDefault(postDate, 0);
            postsCountByDate.put(postDate, postCount + 1);
        }
        List<Integer> allYears = postRepository.getYearsWithAnyPosts();
        log.info("--- Получен список всех лет, за которые есть посты: " + Arrays.toString(allYears.toArray()));
        ResponseEntity<ResponseApi> response =
                new ResponseEntity<ResponseApi>(new GetPostsByCalendarResponse(postsCountByDate, allYears), HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() + "," + response.getBody() + "}");
        return response;
    }

    private String getRandomDirectoryToUpload() {
        String randomHash = String.valueOf(String.valueOf(Math.pow(Math.random(), 100 * Math.random())).hashCode());
        String firstFolder = randomHash.substring(0, randomHash.length() / 3);
        String secondFolder = randomHash.substring(
                firstFolder.length(), (firstFolder.length() + randomHash.length() / 3));
        String thirdFolder = randomHash.substring((firstFolder.length() + secondFolder.length()));
        StringBuilder builder = new StringBuilder(imagesRootFolder)
                .append("/").append(imagesUploadFolder)
                .append("/").append(firstFolder)
                .append("/").append(secondFolder)
                .append("/").append(thirdFolder);
        String result = builder.toString();
        log.info("--- Получена случайная папка для загрузки: " + result);
        return result;
    }

    private String getRandomImageName() {
        String randomHash = getHashedString(String.valueOf(Math.pow(Math.random(), 100 * Math.random())));
        String res = randomHash + "." + imagesFormat; // имя файла задаем хэшем
        log.info("--- Получено имя файла: " + res);
        return res;
    }

    private boolean isTextValid(String text) {
        if (text == null || text.equals("")) return false;
        String cleanText = HtmlParserServiceImpl.getTextStringFromHtml(text);
        assert cleanText != null;
        return cleanText.length() <= postBodyMaxLength && cleanText.length() >= postBodyMinLength;
    }

    private boolean isTitleValid(String title) {
        return title != null && !title.equals("") && title.length() <= postTitleMaxLength
                && title.length() >= postTitleMinLength;
    }

    private String getHashedString(String stringToHash) {
        try {
            MessageDigest md = MessageDigest.getInstance(hashingAlgorithm);
            md.update(stringToHash.getBytes());
            byte[] digest = md.digest();
            String result = DatatypeConverter.printHexBinary(digest).toUpperCase();
            log.info("--- Успешно получена хэш-строка по алгоритму: "
                    + hashingAlgorithm + " из строки {" + stringToHash + "}");
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            log.warn("--- Не удалось получить хэш-строку по алгоритму: "
                    + hashingAlgorithm + " из строки {" + stringToHash + "}");
            return null;
        }
    }

    private LocalDateTime parseLocalDateTime(String timeToParse) {
        LocalDateTime time = null;
        try {
            time = LocalDateTime.ofInstant(new SimpleDateFormat(PARSE_TIME_PATTERN1)
                    .parse(timeToParse).toInstant(), ZoneId.systemDefault());
        } catch (ParseException e) {
            log.error("--- Не удалось спарсить время из строки {" + timeToParse +
                    "} с паттерном {" + PARSE_TIME_PATTERN1 + "}", e);
            try {
                time = LocalDateTime.ofInstant(new SimpleDateFormat(PARSE_TIME_PATTERN2)
                        .parse(timeToParse).toInstant(), ZoneId.systemDefault());
            } catch (ParseException ex) {
                log.error("--- Не удалось спарсить время из строки {" + timeToParse +
                        "} с паттерном {" + PARSE_TIME_PATTERN2 + "}", ex);
                ex.printStackTrace();
                e.printStackTrace();
            }
        }
        log.info("--- Получено время {" + time + "} из строки {" + timeToParse + "}");
        return time;
    }

    private LocalDate parseLocalDate(String dateToParse) {
        LocalDate date = null;
        try {
            date = LocalDate.ofInstant(new SimpleDateFormat(PARSE_DATE_PATTERN)
                    .parse(dateToParse).toInstant(), ZoneId.systemDefault());
        } catch (ParseException e) {
            e.printStackTrace();
            log.error("--- Не удалось спарсить дату из строки {" + dateToParse + "}", e);
        }
        log.info("--- Получена дата {" + date + "} из строки {" + dateToParse + "}");
        return date;
    }

    private boolean isPremoderation() { // Проверка - требуется ли премодерации постов при создании
        return isCurrentSettingOn(GlobalSettingsRepositoryService.POST_PREMODERATION);
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