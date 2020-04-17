package main.services.interfaces;

import main.api.request.*;
import main.api.response.ResponseApi;
import main.model.entities.User;
import org.springframework.http.ResponseEntity;

import javax.servlet.http.HttpSession;

public interface UserRepositoryService {

    ResponseEntity<User> getUser(int id);

    ResponseEntity<ResponseApi> login(LoginRequest loginRequest, HttpSession session);

    ResponseEntity<ResponseApi> checkAuth(HttpSession session);

    ResponseEntity<ResponseApi> restorePassword(RestorePassRequest restorePassRequest);

    ResponseEntity<ResponseApi> changePassword(ChangePasswordRequest changePasswordRequest);

    ResponseEntity<ResponseApi> register(RegisterRequest registerRequest);

    ResponseEntity<ResponseApi> editProfile(EditProfileRequest editProfileRequest,
                                            HttpSession session);

    ResponseEntity<?> getMyStatistics(HttpSession session);

    ResponseEntity<?> getAllStatistics(HttpSession session);

    ResponseEntity<ResponseApi> logout(HttpSession session);

    Integer getUserIdBySession(HttpSession session);

    User getUserBySession(HttpSession session);
}
