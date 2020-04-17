package main.api.response;

public class GetGeneralDataResponse implements ResponseApi {

    private String title;
    private String subtitle;
    private String phone;
    private String email;
    private String copyright;
    private String copyrightFrom;

    public GetGeneralDataResponse(String title, String subtitle, String phone, String email,
                                  String copyright, String copyrightFrom) {
        this.title = title;
        this.subtitle = subtitle;
        this.phone = phone;
        this.email = email;
        this.copyright = copyright;
        this.copyrightFrom = copyrightFrom;
    }

    public GetGeneralDataResponse() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCopyright() {
        return copyright;
    }

    public void setCopyright(String copyright) {
        this.copyright = copyright;
    }

    public String getCopyrightFrom() {
        return copyrightFrom;
    }

    public void setCopyrightFrom(String copyrightFrom) {
        this.copyrightFrom = copyrightFrom;
    }

    @Override
    public String toString() {
        return "ResponseGeneralData{" +
                "title='" + title + '\'' +
                ", subtitle='" + subtitle + '\'' +
                ", phone='" + phone + '\'' +
                ", email='" + email + '\'' +
                ", copyright='" + copyright + '\'' +
                ", copyrightFrom='" + copyrightFrom + '\'' +
                '}';
    }
}