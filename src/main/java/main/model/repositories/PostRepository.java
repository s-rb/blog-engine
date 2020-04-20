package main.model.repositories;

import main.model.entities.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;

@Repository
public interface PostRepository extends JpaRepository<Post, Integer> {

    @Query(value = "SELECT * FROM posts p WHERE p.is_active = 1 " +
            "AND p.moderation_status = 'ACCEPTED' " +
            "AND p.time < NOW() " +
            "ORDER BY p.time DESC LIMIT ?2 OFFSET ?1", nativeQuery = true)
    List<Post> getRecentPosts(int offset, int limit);

    // Лучшие посты - по количеству лайков
    @Query(value = "SELECT p.* FROM posts AS p " +
            "LEFT JOIN (SELECT post_id, SUM(value) AS sum_values " +
            "FROM post_votes GROUP BY post_id) AS sum_votes " +
            "ON p.id=sum_votes.post_id " +
            "WHERE p.is_active = 1 " +
            "AND p.moderation_status = 'ACCEPTED' " +
            "AND p.time < NOW() " +
            "ORDER BY sum_values DESC " +
            "LIMIT ?2 OFFSET ?1", nativeQuery = true)
    List<Post> getBestPosts(int offset, int limit);

    // Самые обсуждаемые (популярные) посты по комментам
    @Query(value = "SELECT p.* FROM posts AS p " +
            "LEFT JOIN (SELECT post_id, COUNT(post_id) AS post_counts " +
            "FROM post_comments GROUP BY post_id) AS posts_w_counts " +
            "ON p.id=posts_w_counts.post_id " +
            "WHERE p.is_active = 1 " +
            "AND p.moderation_status = 'ACCEPTED' " +
            "AND p.time < NOW() " +
            "ORDER BY post_counts DESC " +
            "LIMIT ?2 OFFSET ?1", nativeQuery = true)
    List<Post> getPopularPosts(int offset, int limit);

    @Query(value = "SELECT * FROM posts p WHERE p.is_active = 1 " +
            "AND p.moderation_status = 'ACCEPTED' " +
            "AND p.time < NOW() " +
            "ORDER BY p.time ASC LIMIT ?2 OFFSET ?1", nativeQuery = true)
    List<Post> getEarlyPosts(int offset, int limit);

    @Query(value = "SELECT count(id) AS count FROM posts", nativeQuery = true)
    int countAllPostsAtDatabase();

    // Все доступные посты на сайте
    @Query(value = "SELECT COUNT(filtered_posts.id) " +
            "FROM (SELECT * FROM posts p WHERE p.is_active = 1 " +
            "AND p.moderation_status = 'ACCEPTED' " +
            "AND p.time < NOW()) AS filtered_posts", nativeQuery = true)
    int countAllPostsAtSite();

    @Query(value = "SELECT DISTINCT * FROM posts p " +
            "WHERE (p.text LIKE %?3% OR p.title LIKE %?3%) " +
            "AND p.is_active = 1 " +
            "AND p.moderation_status = 'ACCEPTED' " +
            "AND p.time < NOW() " +
            "ORDER BY p.time DESC LIMIT ?2 OFFSET ?1", nativeQuery = true)
    List<Post> searchPosts(int offset, int limit, String query);

    @Query(value = "SELECT * FROM posts p " +
            "WHERE DATEDIFF(p.time, ?) = 0 " +
            "AND p.is_active = 1 " +
            "AND p.moderation_status = 'ACCEPTED' " +
            "AND p.time < NOW() " +
            "ORDER BY p.time DESC LIMIT ? OFFSET ?", nativeQuery = true)
    List<Post> getPostsByDate(String date, int limit, int offset);

    @Query(value = "SELECT DISTINCT p.* " +
            "FROM posts AS p " +
            "INNER JOIN tag2post t2 ON p.id = t2.post_id " +
            "INNER JOIN tags t ON t.id  = t2.tag_id " +
            "WHERE (t.name LIKE %?1%) " +
            "AND p.is_active = 1 " +
            "AND p.moderation_status = 'ACCEPTED' " +
            "AND p.time < NOW() " +
            "ORDER BY p.time DESC " +
            "LIMIT ?2 OFFSET ?3", nativeQuery = true)
    List<Post> getPostsByTag(String tag, int limit, int offset);

    @Query(value = "SELECT * FROM posts p " +
            "WHERE p.is_active = 1 " +
            "AND p.moderation_status = 'NEW' " +
            "ORDER BY p.time DESC LIMIT ? OFFSET ?", nativeQuery = true)
    List<Post> getPostsForModeration(int limit, int offset);

    @Query(value = "SELECT * FROM posts p " +
            "WHERE p.moderator_id = ?4 " +
            "AND p.is_active = 1 " +
            "AND p.moderation_status = ?1 " +
            "ORDER BY p.time DESC LIMIT ?2 OFFSET ?3", nativeQuery = true)
    List<Post> getPostsModeratedByMe(String status, int limit, int offset, int moderatorId);

    @Query(value = "SELECT * FROM posts p " +
            "WHERE p.user_id = ?4 " +
            "AND p.is_active = 1 " +
            "AND p.moderation_status = ?1 " +
            "ORDER BY p.time DESC LIMIT ?2 OFFSET ?3", nativeQuery = true)
    List<Post> getMyActivePosts(String status, int limit, int offset, int userId);

    @Query(value = "SELECT * FROM posts p " +
            "WHERE p.user_id = ?1 " +
            "AND p.is_active = 0 " +
            "ORDER BY p.time DESC LIMIT ?2 OFFSET ?3", nativeQuery = true)
    List<Post> getMyNotActivePosts(int userId, int limit, int offset);

    @Query(value = "SELECT * FROM posts p  " +
            "WHERE YEAR(p.time) = ?", nativeQuery = true)
    List<Post> getPostsByYear(int year);

    @Query(value = "SELECT DISTINCT YEAR(p.time) AS post_year " +
            "FROM posts p ORDER BY post_year DESC", nativeQuery = true)
    List<Integer> getYearsWithAnyPosts();

    @Query(value = "SELECT COUNT(p.id) FROM posts p " +
            "WHERE p.is_active = 1 " +
            "AND p.moderation_status = 'NEW'", nativeQuery = true)
    int countPostsForModeration();

    @Query(value = "SELECT sum(p.view_count) AS views FROM posts p", nativeQuery = true)
    int countAllViews();

    @Query(value = "SELECT p.time FROM posts p " +
            "WHERE p.is_active = 1 " +
            "AND p.moderation_status = 'ACCEPTED' " +
            "ORDER BY p.time ASC LIMIT 1", nativeQuery = true)
    Timestamp getFirstPublicationDate();

    @Query(value = "SELECT COUNT(searched_posts.id) FROM " +
            "(SELECT DISTINCT * FROM posts p " +
            "WHERE (p.text LIKE %?1% OR p.title LIKE %?1%) " +
            "AND p.is_active = 1 " +
            "AND p.moderation_status = 'ACCEPTED' " +
            "AND p.time < NOW()) AS searched_posts", nativeQuery = true)
    int countSearchedPosts(String query);

    @Query(value = "SELECT COUNT(searched_posts.id) FROM " +
            "(SELECT * FROM posts p " +
            "WHERE DATEDIFF(p.time, ?) = 0 " +
            "AND p.is_active = 1 " +
            "AND p.moderation_status = 'ACCEPTED' " +
            "AND p.time < NOW()) AS searched_posts", nativeQuery = true)
    int countPostsByDate(String dateString);

    @Query(value = "SELECT COUNT(searched_posts.id) FROM (SELECT DISTINCT p.* " +
            "FROM posts AS p " +
            "INNER JOIN tag2post t2 ON p.id = t2.post_id " +
            "INNER JOIN tags t ON t.id  = t2.tag_id " +
            "WHERE (t.name LIKE %?1%) " +
            "AND p.is_active = 1 " +
            "AND p.moderation_status = 'ACCEPTED' " +
            "AND p.time < NOW()) AS searched_posts", nativeQuery = true)
    int countPostsByTag(String tag);

    @Query(value = "SELECT COUNT(searched_posts.id) FROM (SELECT * FROM posts p " +
            "WHERE p.moderator_id = ?2 " +
            "AND p.is_active = 1 " +
            "AND p.moderation_status = ?1) AS searched_posts", nativeQuery = true)
    int countPostsModeratedByMe(String moderationStatus, int id);

    @Query(value = "SELECT COUNT(searched_posts.id) FROM (SELECT * FROM posts p " +
            "WHERE p.user_id = ?1 " +
            "AND p.is_active = 0) AS searched_posts", nativeQuery = true)
    int countMyNotActivePosts(int id);

    @Query(value = "SELECT COUNT(searched_posts.id) FROM (SELECT * FROM posts p " +
            "WHERE p.user_id = ?2 " +
            "AND p.is_active = 1 " +
            "AND p.moderation_status = ?1) AS searched_posts", nativeQuery = true)
    int countMyActivePosts(String moderationStatus, int id);

    @Query(value = "SELECT DISTINCT * FROM posts p " +
            "WHERE (p.title LIKE %?%) " +
            "LIMIT 1", nativeQuery = true)
    Post getPostByTitle(String title);
}