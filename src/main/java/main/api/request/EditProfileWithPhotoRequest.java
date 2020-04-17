package main.api.request;

import org.springframework.web.multipart.MultipartFile;

public class EditProfileWithPhotoRequest extends EditProfileRequest {

    private MultipartFile photo;

    public EditProfileWithPhotoRequest(Byte removePhoto, String name, String email,
                                       String password, MultipartFile photo) {
        super(removePhoto, name, email, password);
        this.photo = photo;
    }

    public EditProfileWithPhotoRequest() {
    }

    public MultipartFile getPhoto() {
        return photo;
    }

    public void setPhoto(MultipartFile photo) {
        this.photo = photo;
    }

    public EditProfileWithPhotoRequest(MultipartFile photo) {
        this.photo = photo;
    }
}
