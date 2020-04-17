package main.api.response;

import main.model.entities.User;

public class LoginResponse implements ResponseApi {

    private boolean result;
    private LogginedUser user;

    public LoginResponse(User user, int moderationCount) {
        result = true;
        this.user = new LogginedUser(user, moderationCount);
    }

    public boolean getResult() {
        return result;
    }

    public void setResult(boolean result) {
        this.result = result;
    }

    public LogginedUser getUser() {
        return user;
    }

    public void setUser(LogginedUser user) {
        this.user = user;
    }

    static class LogginedUser {

        private int id;
        private String name;
        private String photo;
        private String email;
        private boolean moderation;
        private int moderationCount;
        private boolean settings;

        private LogginedUser(User user, int moderationCount) {
            id = user.getId();
            name = user.getName();
            photo = user.getPhoto();
            email = user.getEmail();
            moderation = user.isModerator();
//            moderationCount = user.getPostsModerated().size(); // Меняем на общее количество постов,
//            требующих модерации, подгон под фронт, вразрез с API док
            this.moderationCount = moderationCount;
            settings = user.isModerator();
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPhoto() {
            return photo;
        }

        public void setPhoto(String photo) {
            this.photo = photo;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public boolean isModeration() {
            return moderation;
        }

        public void setModeration(boolean moderation) {
            this.moderation = moderation;
        }

        public int getModerationCount() {
            return moderationCount;
        }

        public void setModerationCount(int moderationCount) {
            this.moderationCount = moderationCount;
        }

        public boolean isSettings() {
            return settings;
        }

        public void setSettings(boolean settings) {
            this.settings = settings;
        }
    }
}