package main.api.request;

public class EditProfileRequest implements RequestApi {

    private Byte removePhoto;
    private String name;
    private String email;
    private String password;

    public EditProfileRequest() {
    }

    public EditProfileRequest(Byte removePhoto, String name, String email, String password) {
        this.removePhoto = removePhoto;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Byte getRemovePhoto() {
        return removePhoto;
    }

    public void setRemovePhoto(Byte removePhoto) {
        this.removePhoto = removePhoto;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}
