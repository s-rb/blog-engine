package main.api.response;

import main.model.entities.Post;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class GetPostsForModerationResponse implements ResponseApi {

    private int count;
    private List<ResponsePost> posts;

    public GetPostsForModerationResponse(int count, ArrayList<Post> postsToShow, int announceLength) {
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
        return "ResponsePostsForModeration{" +
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

        public ResponsePost(Post post, int announceLength) {
            this.id = post.getId();
            this.time = getTimeString(post.getTime());
            this.user = new PostAuthor(post);
            this.title = post.getTitle();
            String temp = post.getText().replaceAll("<.+?>", "");
            announce = temp.length() < announceLength ? temp
                    : temp.substring(0, announceLength) + "...";
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