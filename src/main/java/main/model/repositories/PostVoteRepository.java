package main.model.repositories;

import main.model.entities.PostVote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface PostVoteRepository extends JpaRepository<PostVote, Integer> {

    @Query(value = "SELECT * FROM post_votes p" +
            " WHERE p.post_id = ? AND p.user_id = ?", nativeQuery = true)
    PostVote getPostVoteByUserIdAndPostId(int postId, int userId);

    @Query(value = "SELECT count(p.id) AS count " +
            "FROM post_votes p WHERE p.value = 1", nativeQuery = true)
    int countAllLikes();

    @Query(value = "SELECT count(p.id) AS count " +
            "FROM post_votes p WHERE p.value = -1", nativeQuery = true)
    int countAllDislikes();
}