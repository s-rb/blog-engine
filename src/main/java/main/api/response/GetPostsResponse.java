package main.api.response;

import main.model.entities.Post;
import main.services.HtmlParserServiceImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GetPostsResponse implements ResponseApi {

    private int count;
    private List<ResponsePost> posts;

    public GetPostsResponse(int count, ArrayList<Post> postsToShow, int announceLength) {
        this.count = count;
        posts = new ArrayList<>();
        for (Post p : postsToShow) {
            ResponsePost responsePost = new ResponsePost(p, announceLength);
            posts.add(responsePost);
        }
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<ResponsePost> getPosts() {
        return posts;
    }

    public void setPosts(List<ResponsePost> posts) {
        this.posts = posts;
    }

    @Override
    public String toString() {
        return "Posts{" +
                "count=" + count +
                ", posts=" + posts +
                '}';
    }

    static class ResponsePost {

        private int id;
        private String time;
        private PostAuthor user;
        private String title;
        private String announce;
        private int likeCount;
        private int dislikeCount;
        private int commentCount;
        private int viewCount;

        public ResponsePost(Post post, int announceLength) {
            this.id = post.getId();
            this.time = getTimeString(post.getTime());
            this.user = new PostAuthor(post);
            this.title = post.getTitle();
            String temp = HtmlParserServiceImpl.getTextStringFromHtml(post.getText());
            assert temp != null;
            announce = temp.length() < announceLength ? temp
                    : temp.substring(0, announceLength) + "...";
            this.likeCount = (int) post.getPostVotes().stream().filter(l -> l.getValue() == 1).count();
            this.dislikeCount = (int) post.getPostVotes().stream().filter(l -> l.getValue() == -1).count();
            this.commentCount = post.getPostComments().size();
            this.viewCount = post.getViewCount();
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

        public int getCommentCount() {
            return commentCount;
        }

        public void setCommentCount(int commentCount) {
            this.commentCount = commentCount;
        }

        public int getViewCount() {
            return viewCount;
        }

        public void setViewCount(int viewCount) {
            this.viewCount = viewCount;
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

        @Override
        public String toString() {
            return "ResponsePostsApi{" +
                    "id=" + id +
                    ", time='" + time + '\'' +
                    ", user=" + user +
                    ", title='" + title + '\'' +
                    ", announce='" + announce + '\'' +
                    ", likeCount=" + likeCount +
                    ", dislikeCount=" + dislikeCount +
                    ", commentCount=" + commentCount +
                    ", viewCount=" + viewCount +
                    '}';
        }

        static class PostAuthor {
            private int id;
            private String name;

            public PostAuthor(Post post) {
                this.id = post.getUser().getId();
                this.name = post.getUser().getName();
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
                return "PostAuthorUser{" +
                        "id=" + id +
                        ", name='" + name + '\'' +
                        '}';
            }
        }
    }
}