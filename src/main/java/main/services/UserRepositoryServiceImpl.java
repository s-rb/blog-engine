package main.services;

import lombok.extern.slf4j.Slf4j;
import main.api.request.*;
import main.api.response.*;
import main.model.ModerationStatus;
import main.model.entities.*;
import main.model.repositories.CaptchaRepository;
import main.model.repositories.PostRepository;
import main.model.repositories.PostVoteRepository;
import main.model.repositories.UserRepository;
import main.services.interfaces.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpSession;
import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserRepositoryServiceImpl implements UserRepositoryService {

    private static final char[] SYMBOLS_FOR_GENERATOR = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

    @Value("${user.password.restore_key_length}")
    private int keySize;
    @Value("${user.image.root_folder}")
    private String rootPathToUploadAvatars;
    @Value("${user.password.validation_regex}")
    private String passwordValidationRegex;
    @Value("${user.email.validation_regex}")
    private String emailValidationRegex;
    @Value("${user.image.upload_folder}")
    private String uploadFolder;
    @Value("${user.image.avatars_folder}")
    private String avatarsFolder;
    @Value("${user.image.format}")
    private String imageFormat;
    @Value("${user.image.max_size}")
    private int maxPhotoSizeInBytes;
    @Value("${user.image.upload_timeout_ms}")
    private int timeoutToUploadPhotoMS;
    @Value("${user.timeout_edit_profile}")
    private int timeoutToFinishEditProfileMS;
    @Value("${user.password.restore_pass_message_string}")
    private String restorePassMessageString;
    @Value("${user.password.restore_message_subject}")
    private String restoreMessageSubject;
    @Value("${user.password.hashing_algorithm}")
    private String hashingAlgorithm;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CaptchaRepositoryService captchaRepositoryService;
    @Autowired
    private GlobalSettingsRepositoryService globalSettingsRepositoryService;
    @Autowired
    private PostVoteRepositoryService postVoteRepositoryService;
    @Autowired
    private PostRepositoryService postRepositoryService;
    @Autowired
    private JavaMailSender emailSender;
    @Autowired
    private CaptchaRepository captchaRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PostVoteRepository postVoteRepository;
    @Autowired
    private FileSystemService fileSystemService;

    private Map<String, Integer> sessionIdToUserId = new HashMap<>(); // Храним сессию и ID пользователя, по заданию не в БД

    @Override
    public ResponseEntity<User> getUser(int id) {
        Optional<User> optionalUser = userRepository.findById(id);
        return optionalUser.map(user -> new ResponseEntity<>(user, HttpStatus.OK))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(null));
    }

    @Override
    public ResponseEntity<ResponseApi> login(LoginRequest loginRequest, HttpSession session) {
        String email = loginRequest.getEmail();
        String password = loginRequest.getPassword();
        User user = userRepository.getUserByEmail(email);
        if (user == null) {
            log.warn("--- Ошибка! Пользователь с таким E-mail: " + email + " не существует");
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пользователь с таким E-mail: "
                            + email + " не существует"),
                    HttpStatus.BAD_REQUEST);
        }
        String hashedPassToCheck = getHashedString(password);
        if (user.getStoredHashPass().equals(hashedPassToCheck)) { // пароль совпал по хэшу
            sessionIdToUserId.put(session.getId().toString(), user.getId()); // Запоминаем пользователя и сессию
            log.info("--- Добавлена сессия пользователя с email:" + email + ", session:" + session.getId());
            return new ResponseEntity<>(new LoginResponse(user, postRepository.countPostsForModeration()), HttpStatus.OK);
        } else {
            log.warn("--- Ошибка! Пароль введен неверно");
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пароль введен не верно"),
                    HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    public ResponseEntity<ResponseApi> checkAuth(HttpSession session) {
        if (!sessionIdToUserId.containsKey(session.getId().toString())) {
            log.warn("--- Ошибка! Отсутствует сессия");
            return new ResponseEntity<>(new BooleanResponse(false), HttpStatus.OK);
        } else {
            User user = getUserBySession(session);
            return getResponseEntityByUserExistence(user);
        }
    }

    @Override
    public ResponseEntity<ResponseApi> restorePassword(RestorePassRequest restorePassRequest) {
        String email = restorePassRequest.getEmail();
        if (!isEmailValid(email)) {
            log.warn("--- Ошибка! Вы указали неверный E-mail: " + email);
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Ошибка! Вы указали неверный E-mail: " + email),
                    HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.getUserByEmail(email);
        if (user == null) {
            log.warn("--- Ошибка! Пользователь с таким E-mail: " + email + " не существует");
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пользователь с таким E-mail: "
                            + email + " не существует"),
                    HttpStatus.BAD_REQUEST);
        }
        String restoreCode = generateRandomString();
        user.setCode(restoreCode); // запоминаем код в базе
        userRepository.save(user);
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(restoreMessageSubject);
        message.setText(restorePassMessageString + restoreCode);
        emailSender.send(message);
        log.info("--- Отправлено сообщение восстановления пароля на email:" + email + ", с кодом:" + restoreCode);
        return new ResponseEntity<>(new BooleanResponse(true), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ResponseApi> changePassword(ChangePasswordRequest changePasswordRequest) {
        String code = changePasswordRequest.getCode();
        String password = changePasswordRequest.getPassword();
        String captcha = changePasswordRequest.getCaptcha();
        String captchaSecret = changePasswordRequest.getCaptchaSecret();
        if (code == null || password == null || captcha == null || captchaSecret == null ||
                code.isBlank() || password.isBlank() || captcha.isBlank() || captchaSecret.isBlank()) {
            log.warn("--- Введены не все требуемые параметры: " + "{code:" + code + ", password:" + password + "," +
                    "captcha:" + captcha + ", captchaSecret:" + captchaSecret + "}");
            return new ResponseEntity<>(new BadRequestMessageResponse(
                    code == null || code.isBlank() ? "Не указан код восстановления" : "",
                    password == null || password.isBlank() ? "Не введен пароль" : "",
                    captcha == null || captcha.isBlank() ? "Не введен текст с картинки" : "",
                    captchaSecret == null || captchaSecret.isBlank() ? "Не передан секретный код капчи" : ""),
                    HttpStatus.BAD_REQUEST);
        }
        User user = userRepository.getUserByCode(code);
        boolean isCodeValid = (user != null);
        boolean isPassValid = isPasswordValid(password);
        boolean isCaptchaCodeValid = isCaptchaValid(captcha, captchaSecret);
        if (!isCodeValid || !isPassValid || !isCaptchaCodeValid) {
            ResponseEntity<ResponseApi> response = new ResponseEntity<>(
                    new BadRequestMessageResponse(
                            !isCodeValid ? "Неверный код восстановления" : "",
                            !isPassValid ? "Пароль не соответствует требованиям" : "",
                            !isCaptchaCodeValid ? "Капча введена неверно" : ""),
                    HttpStatus.BAD_REQUEST);
            log.warn("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() +
                    "," + response.getBody() + "}");
            return response;
        }
        String newHashedPassword = getHashedString(password);
        user.setHashedPassword(newHashedPassword);
        userRepository.save(user);
        log.info("--- Для пользователя c id:" + user.getId() + " успешно установлен новый пароль");
        return new ResponseEntity<>(new BooleanResponse(true), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ResponseApi> register(RegisterRequest registerRequest) {
        String email = registerRequest.getEmail();
        if (email == null || email.isBlank()) {
            log.warn("--- В регистрационную форму введен пустой Email");
            return new ResponseEntity<>(new BadRequestMessageResponse(
                    email == null || email.isBlank() ? "Введен пустой email" : ""),
                    HttpStatus.BAD_REQUEST);
        }
        String name = registerRequest.getName();
        name = name == null || name.isBlank() ? email.replaceAll("@.+", "") : name;
        String password = registerRequest.getPassword();
        String captcha = registerRequest.getCaptcha();
        String captchaSecret = registerRequest.getCaptchaSecret();
        if (name.isBlank() || password == null || captcha == null || captchaSecret == null
                || password.isBlank() || captcha.isBlank() || captchaSecret.isBlank()) {
            log.warn("--- Введены не все требуемые параметры: " + "{name:" + name + ", password:" + password + "," +
                    "captcha:" + captcha + ", captchaSecret:" + captchaSecret + "}");
            return new ResponseEntity<>(new BadRequestMessageResponse(
                    name.isBlank() ? "Передано пустое имя" : "",
                    password == null || password.isBlank() ? "Не введен пароль" : "",
                    captcha == null || captcha.isBlank() ? "Не введен текст с картинки" : "",
                    captchaSecret == null || captchaSecret.isBlank() ? "Не передан секретный код капчи" : ""),
                    HttpStatus.BAD_REQUEST);
        }
        // Проверка уникальности имени и email
        boolean isUserExistsByEmail = userRepository.getUserByEmail(email.toLowerCase()) != null;
        boolean isNameValid = (userRepository.getUserByName(name) == null);
        boolean isEmailValid = (!isUserExistsByEmail && isEmailValid(email));
        boolean isCaptchaCodeValid = isCaptchaValid(captcha, captchaSecret);
        boolean isPassValid = isPasswordValid(password);
        if (!isNameValid || !isPassValid || !isCaptchaCodeValid || !isEmailValid) {
            ResponseEntity<ResponseApi> response = new ResponseEntity<>(
                    new BadRequestMessageResponse(
                            !isNameValid ? "Пользователь с таким именем уже существует" : "",
                            !isPassValid ? "Пароль не соответствует требованиям" : "",
                            !isCaptchaCodeValid ? "Капча введена неверно" : "",
                            !isEmailValid(email) ? "Email не соответствует требованиям" : "",
                            isUserExistsByEmail ? "Пользователь с таким Email уже существует" : ""),
                    HttpStatus.BAD_REQUEST);
            log.warn("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() +
                    "," + response.getBody() + "}");
            return response;
        }
        // Все проверки прошли, регистрация
        String hashedPassword = getHashedString(password);
        User user = new User(false, LocalDateTime.now(), name, email, hashedPassword);
        user = userRepository.save(user); // И можно получить id
        log.info("--- Успешно зарегистрирован пользователь с id:" + user.getId() + ", email:" + user.getEmail());
        return new ResponseEntity<>(new BooleanResponse(true), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<ResponseApi> editProfile(EditProfileRequest editProfileRequest,
                                                   HttpSession session) {
        Byte removePhoto = editProfileRequest.getRemovePhoto();
        String email = editProfileRequest.getEmail();
        String name = editProfileRequest.getName();
        String password = editProfileRequest.getPassword();

        User user = getUserBySession(session);
        if (user == null) {
            log.warn("--- Не найден пользователь по номеру сессии: " + session.getId());
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пользователь не авторизован"),
                    HttpStatus.BAD_REQUEST);
        }

        boolean isAnotherUserExistsByEmail = false;
        boolean isNameValid;
        boolean isEmailValid;
        boolean isPassValid;
        boolean isPhotoValid = true;
        if (password != null && !password.isBlank()) {
            isPassValid = isPasswordValid(password);
            String hashedPassword = getHashedString(password);
            if (isPassValid) user.setHashedPassword(hashedPassword);
        } else isPassValid = true;
        if (name != null && !name.isBlank() && !name.equals(user.getName())) {
            isNameValid = (userRepository.getUserByName(name) == null);
            if (isNameValid) user.setName(name);
        } else {
            isNameValid = true;
        }
        if (email != null && !email.isBlank() && !email.equalsIgnoreCase(user.getEmail())) {
            isAnotherUserExistsByEmail = !email.equalsIgnoreCase(user.getEmail()) &&
                    userRepository.getUserByEmail(email.toLowerCase()) != null;
            isEmailValid = (!isAnotherUserExistsByEmail && isEmailValid(email));
            if (isEmailValid) user.setEmail(email);
        } else {
            isEmailValid = true;
        }
        if (removePhoto != null && removePhoto == 1) {
            String currentPhoto = user.getPhoto();
            fileSystemService.deleteFileByPath(currentPhoto);
            user.setPhoto(null);
        }

        if (editProfileRequest instanceof EditProfileWithPhotoRequest) { // Если переданный запрос содержит фото
            MultipartFile photo = ((EditProfileWithPhotoRequest) editProfileRequest).getPhoto();
            if (photo != null) {
                if (photo.getSize() > maxPhotoSizeInBytes || photo.getSize() < 0) {
                    isPhotoValid = false;
                } else if (photo.getSize() > 0 && photo.getSize() <= maxPhotoSizeInBytes) {
                    // Если у юзера уже есть фото, удаляем его
                    if (user.getPhoto() != null) fileSystemService.deleteFileByPath(user.getPhoto());
                    String directoryPath = getDirectoryToUpload();          // папка для загрузки нового фото
                    String imageName = getRandomImageName();
                    if (fileSystemService.createDirectoriesByPath(directoryPath)) {
                        String fileDestPath = directoryPath + "/" + imageName;
                        while (Files.exists(Paths.get(fileDestPath))) {
                            imageName = getRandomImageName();
                            fileDestPath = directoryPath + "/" + imageName;
                        }
                        try {
                            photo.transferTo(Paths.get(directoryPath, imageName));
                            user.setPhoto(fileDestPath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else isPhotoValid = false;
            } else isPhotoValid = false;
        }

        if (!isNameValid || !isPassValid || !isEmailValid || !isPhotoValid) {
            ResponseEntity<ResponseApi> response = new ResponseEntity<>(
                    new BadRequestMessageResponse(
                            !isNameValid ? "Пользователь с таким именем уже существует" : "",
                            !isPassValid ? "Пароль не соответствует требованиям: введите не менее 6 символов" : "",
                            !isPhotoValid ? "Размер файла не соответствует ограничению "
                                    + (int) (maxPhotoSizeInBytes / 1024) + " кБ" : "",
                            email != null && !email.isBlank() ? (
                                    List.of((!isEmailValid(email) ? "Email не соответствует требованиям" : ""),
                                            (isAnotherUserExistsByEmail ? "Пользователь с таким Email уже зарегистрирован" : ""))
                                            .stream()
                                            .filter(s -> (s != null && !s.isBlank()))
                                            .collect(Collectors.joining(". "))) : ""),
                    HttpStatus.BAD_REQUEST);
            log.warn("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() +
                    "," + response.getBody() + "}");
            return response;
        }
        userRepository.save(user);
        log.info("--- Профиль пользователя с id:" + user.getId() + " успешно отредактирован");
        return new ResponseEntity<>(new BooleanResponse(true), HttpStatus.OK);
    }

    @Override
    public ResponseEntity<?> getMyStatistics(HttpSession session) {
        User user = getUserBySession(session);
        if (user == null) {
            log.warn("--- Не найден пользователь по номеру сессии: " + session.getId());
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пользователь не авторизован"),
                    HttpStatus.BAD_REQUEST);
        }
        LocalDateTime firstPostTime = null;
        Set<Post> myPosts = user.getPosts().stream()
                .filter(p -> p.getModerationStatus().equals(ModerationStatus.ACCEPTED) &&
                        p.isActive() && p.getTime().isBefore(LocalDateTime.now()))
                .collect(Collectors.toSet());
        int postsCount = myPosts.size();
        int allLikesCount = 0;
        int allDislikeCount = 0;
        int viewsCount = 0;
        for (Post p : myPosts) {
            LocalDateTime currentPostTime = p.getTime();
            if (firstPostTime == null) {
                firstPostTime = currentPostTime;
            } else if (firstPostTime.isAfter(currentPostTime)) {
                firstPostTime = currentPostTime;
            }
            viewsCount += p.getViewCount();
            Set<PostVote> currentPostVotes = p.getPostVotes();
            for (PostVote like : currentPostVotes) {
                if (like.getValue() == 1) {
                    allLikesCount += 1;
                } else if (like.getValue() == -1) {
                    allDislikeCount += 1;
                }
            }
        }
        String firstPublicationDate;
        firstPublicationDate = firstPostTime == null ? "Еще не было"
                : firstPostTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        GetStatisticsResponse responseStatistics = new GetStatisticsResponse(postsCount, allLikesCount, allDislikeCount,
                viewsCount, firstPublicationDate);
        ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(responseStatistics.getMap(), HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() +
                "," + response.getBody() + "}");
        return response;
    }

    @Override
    public ResponseEntity<?> getAllStatistics(HttpSession session) {
        boolean isStatisticsIsPublic = false;
        for (GlobalSettings s : globalSettingsRepositoryService.getAllGlobalSettingsSet()) {
            if (s.getCode().toUpperCase().equals(GlobalSettingsRepositoryService.STATISTICS_IS_PUBLIC)) {
                isStatisticsIsPublic = s.getValue().toUpperCase().equals("YES");
            }
        }
        Integer userId = getUserIdBySession(session);
        if (!isStatisticsIsPublic && userId == null) {
            log.warn("--- Ошибка! Отсутствует сессия, пользователь не авторизован и " +
                    "просмотр статистики незарегистрированными пользователями запрещен");
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пользователь не авторизован, показ" +
                            " статистики неавторизованным пользователям запрещен"),
                    HttpStatus.BAD_REQUEST);
        }
        int postsCount = postRepository.countAllPostsAtDatabase();
        int allLikesCount = postVoteRepository.countAllLikes();
        int allDislikeCount = postVoteRepository.countAllDislikes();
        int viewsCount = postRepository.countAllViews();
        String firstPublicationDate = postsCount < 1 ? "Еще не было публикаций" : postRepository
                .getFirstPublicationDate().toLocalDateTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        GetStatisticsResponse responseStatistics = new GetStatisticsResponse(postsCount, allLikesCount, allDislikeCount,
                viewsCount, firstPublicationDate);
        ResponseEntity<Map<String, Object>> response = new ResponseEntity<>(responseStatistics.getMap(), HttpStatus.OK);
        log.info("--- Направляется ответ: {" + "HttpStatus:" + response.getStatusCode() +
                "," + response.getBody() + "}");
        return response;
    }

    @Override
    public ResponseEntity<ResponseApi> logout(HttpSession session) {
        String sessionId = session.getId();
        if (!sessionIdToUserId.containsKey(sessionId)) {
            log.warn("--- Получен запрос на выход, однако сессия с id:" + sessionId + " отсутствует в сохраненных");
            return ResponseEntity.status(HttpStatus.OK).body(new BooleanResponse(true));
        } // По условию всегда возвращает true
        else {
            sessionIdToUserId.remove(sessionId);
            session.invalidate();
            log.info("Пользователь с sessionId:" + sessionId + " успешно вышел");
            return ResponseEntity.status(HttpStatus.OK).body(new BooleanResponse(true));
        }
    }

    @Override
    public Integer getUserIdBySession(HttpSession session) {
        return sessionIdToUserId.get(session.getId().toString());
    }

    private String generateRandomString() {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < keySize; i++) {
            builder.append(SYMBOLS_FOR_GENERATOR[(int) (Math.random() * (SYMBOLS_FOR_GENERATOR.length - 1))]);
        }
        String res = builder.toString();
        log.info("--- Получена случайная строка: " + res);
        return res;
    }

    private boolean isPasswordValid(String passwordToCheck) {
        return (passwordToCheck != null && passwordToCheck.matches(passwordValidationRegex));
    }

    private boolean isEmailValid(String emailToCheck) {
        return (emailToCheck != null && emailToCheck.matches(emailValidationRegex));
    }

    private String getDirectoryToUpload() {
        StringBuilder builder = new StringBuilder(rootPathToUploadAvatars).append("/")
                .append(uploadFolder).append("/").append(avatarsFolder);
        String res = builder.toString();
        log.info("--- Получена директория для загрузки: " + res);
        return res;
    }

    private String getRandomImageName() {
        String randomHash = getHashedString(String.valueOf(Math.pow(Math.random(), 100 * Math.random())));
        String res = randomHash + "." + imageFormat; // имя файла задаем хэшем
        log.info("--- Получено случаное имя файла: " + res);
        return res;
    }

    private Boolean isCaptchaValid(String captcha, String captchaSecret) {
        CaptchaCode captchaCode = captchaRepository.getCaptchaBySecretCode(captchaSecret);
        return captchaCode != null && captchaCode.getCode().equals(captcha);
    }

    private String getHashedString(String stringToHash) {
        log.info("--- Получаем хэш-строку по алгоритму: " + hashingAlgorithm + " из строки {" + stringToHash + "}");
        try {
            MessageDigest md = MessageDigest.getInstance(hashingAlgorithm);
            md.update(stringToHash.getBytes());
            byte[] digest = md.digest();
            String result = DatatypeConverter.printHexBinary(digest).toUpperCase();
            log.info("--- Успешно получена хэш-строка по алгоритму: " + hashingAlgorithm + " из строки {" + stringToHash + "}");
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            log.warn("--- Не удалось получить хэш-строку по алгоритму: " + hashingAlgorithm + " из строки {" + stringToHash + "}");
            return null;
        }
    }

    public User getUserBySession(HttpSession session) {
        Integer userId = getUserIdBySession(session);
        if (userId == null) return null;
        return getUser(userId).getBody();
    }

    private ResponseEntity<ResponseApi> getResponseEntityByUserExistence(User user) {
        if (user != null) {
            log.info("--- По сессии найден пользователь " + user.getName());
            return new ResponseEntity<>(new LoginResponse(user, postRepository.countPostsForModeration()), HttpStatus.OK);
        } else {        // "Ошибка! Пользователь найден в сессиях, однако отсутствует в БД!";
            log.warn("--- Ошибка! Отсутствует сессия, пользователь не авторизован");
            return new ResponseEntity<>(
                    new BadRequestMessageResponse("Пользователь не авторизован"),
                    HttpStatus.BAD_REQUEST);
        }
    }
}
