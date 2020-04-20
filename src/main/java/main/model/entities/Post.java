package main.model.entities;

import main.model.ModerationStatus;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "posts")
public class Post implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private int id;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    @Column(name = "moderation_status", nullable = false)
    @Enumerated(EnumType.STRING)
    private ModerationStatus moderationStatus; // По умолчанию NEW

    @Column(name = "moderator_id", nullable = true)
    private Integer moderatorId = null;

    @ManyToOne(cascade = CascadeType.PERSIST)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "time", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime time;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(name = "view_count", nullable = false)
    private int viewCount;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PostVote> postVotes = new HashSet<PostVote>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<PostComment> postComments = new HashSet<PostComment>();

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<TagToPost> tagsToPostsSet = new HashSet<TagToPost>();

    public Post() {
    }

    public Post(boolean isActive, ModerationStatus moderationStatus,
                User user, LocalDateTime time, String title, String text) {
        this.isActive = isActive;
        this.moderationStatus = moderationStatus;
        this.user = user;
        this.time = time;
        this.title = title;
        this.text = text;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public ModerationStatus getModerationStatus() {
        return moderationStatus;
    }

    public void setModerationStatus(ModerationStatus moderationStatus) {
        this.moderationStatus = moderationStatus;
    }

    public Integer getModeratorId() {
        return moderatorId;
    }

    public void setModeratorId(Integer moderatorId) {
        this.moderatorId = moderatorId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public int getViewCount() {
        return viewCount;
    }

    public void setViewCount(int viewCount) {
        this.viewCount = viewCount;
    }

    public Set<PostVote> getPostVotes() {
        return postVotes;
    }

    public void setPostVotes(Set<PostVote> postVotes) {
        this.postVotes = postVotes;
    }

    public Set<PostComment> getPostComments() {
        return postComments;
    }

    public void setPostComments(Set<PostComment> postComments) {
        this.postComments = postComments;
    }

    public Set<TagToPost> getTagsToPostsSet() {
        return tagsToPostsSet;
    }

    public void setTagsToPostsSet(Set<TagToPost> tagsToPostsSet) {
        this.tagsToPostsSet = tagsToPostsSet;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", isActive=" + isActive +
                ", moderationStatus=" + moderationStatus +
                ", moderatorId=" + moderatorId +
                ", time=" + time +
                ", title='" + title + '\'' +
                ", text='" + text + '\'' +
                ", viewCount=" + viewCount +
                '}';
    }
}