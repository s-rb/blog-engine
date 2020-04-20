package main.api.response;

import main.model.entities.Post;
import main.model.entities.PostComment;
import main.model.entities.TagToPost;
import main.services.HtmlParserServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedList;
import java.util.List;

public class GetPostResponse implements ResponseApi {

    private int id;
    private String time;
    private PostAuthor user;
    private String title;
    private String text;
    private String announce;
    private int likeCount;
    private int dislikeCount;
    private int commentCount;
    private int viewCount;
    private List<Comment> comments;
    private List<String> tags;

    public GetPostResponse(Post post, int announceLength) {
        id = post.getId();
        time = getTimeString(post.getTime());
        user = new PostAuthor(post.getUser().getId(), post.getUser().getName());
        title = post.getTitle();
        text = post.getText();
        String temp = HtmlParserServiceImpl.getTextStringFromHtml(post.getText());
        assert temp != null;
        announce = temp.length() < announceLength ? temp
                : temp.substring(0, announceLength) + "...";
        likeCount = (int) post.getPostVotes().stream().filter(l -> l.getValue() == 1).count();
        dislikeCount = (int) post.getPostVotes().stream().filter(l -> l.getValue() == -1).count();
        commentCount = post.getPostComments().size();
        viewCount = post.getViewCount();
        comments = new LinkedList<>();
        tags = new LinkedList<>();
        for (PostComment c : post.getPostComments()) {
            int commentAuthorId = c.getUser().getId();
            String commentAuthorName = c.getUser().getName();
            String commentAuthorPhoto = c.getUser().getPhoto();
            Comment.CommentAuthor commentAuthor =
                    new Comment.CommentAuthor(commentAuthorId, commentAuthorName, commentAuthorPhoto);
            int commentId = c.getId();
            String commentTime = getTimeString(c.getTime());
            String commentText = c.getText();
            Comment comment = new Comment(commentId, commentTime, commentAuthor, commentText);
            comments.add(comment);
        }
        for (TagToPost t : post.getTagsToPostsSet()) {
            String tagName = t.getTag().getName();
            if (!tags.contains(tagName)) {
                tags.add(tagName);
            }
        }
    }

    private String getTimeString(LocalDateTime objectCreatedTime) {
        StringBuilder timeString = new StringBuilder();
        if (objectCreatedTime.isAfter(LocalDate.now().atStartOfDay())
                && objectCreatedTime.isBefore(LocalDateTime.now())) {
            timeString.append("Сегодня, ").append(objectCreatedTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        } else if (objectCreatedTime.isAfter(LocalDate.now().atStartOfDay().minusDays(1))) {
            timeString.append("Вчера, ").append(objectCreatedTime.format(DateTimeFormatter.ofPattern("HH:mm")));
        } else {
            timeString.append(objectCreatedTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd, HH:mm")));
        }
        return timeString.toString();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public PostAuthor getUser() {
        return user;
    }

    public void setUser(PostAuthor user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAnnounce() {
        return announce;
    }

    public void setAnnounce(String announce) {
        this.announce = announce;
    }

    public int getCommentCount() {
        return commentCount;
    }

    public void setCommentCount(int commentCount) {
        this.commentCount = commentCount;
    }

    public int getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(int likeCount) {
        this.likeCount = likeCount;
    }

    public int getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(int dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public List<Comment> getComments() {
        return comments;
    }

    public void setComments(List<Comment> comments) {
        this.comments = comments;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    static class PostAuthor {

        private int id;
        private String name;

        public PostAuthor(int id, String name) {
            this.id = id;
            this.name = name;
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

        @Override
        public String toString() {
            return "PostAuthorApi{" +
                    "id=" + id +
                    ", name='" + name + '\'' +
                    '}';
        }
    }

    static class Comment {

        private int id;
        private String time;
        private CommentAuthor user;
        private String text;

        public Comment(int id, String time, CommentAuthor user, String text) {
            this.id = id;
            this.time = time;
            this.user = user;
            this.text = text;
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTime() {
            return time;
        }

        public void setTime(String time) {
            this.time = time;
        }

        public CommentAuthor getUser() {
            return user;
        }

        public void setUser(CommentAuthor user) {
            this.user = user;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return "PostCommentApi{" +
                    "id=" + id +
                    ", time='" + time + '\'' +
                    ", user=" + user +
                    ", text='" + text + '\'' +
                    '}';
        }

        static class CommentAuthor {

            private int id;
            private String name;
            private String photo;

            public CommentAuthor(int id, String name, String photo) {
                this.id = id;
                this.name = name;
                this.photo = photo;
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

            @Override
            public String toString() {
                return "PostCommentAuthorApi{" +
                        "id=" + id +
                        ", name='" + name + '\'' +
                        ", photo='" + photo + '\'' +
                        '}';
            }
        }
    }

    @Override
    public String toString() {
        return "ResponsePost{" +
                "id=" + id +
                ", time='" + time + '\'' +
                ", user=" + user +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", announce='" + announce + '\'' +
                ", likeCount=" + likeCount +
                ", dislikeCount=" + dislikeCount +
                ", commentCount=" + commentCount +
                ", viewCount=" + viewCount +
                ", comments=" + comments +
                ", tags=" + tags +
                '}';
    }
}