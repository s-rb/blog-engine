package main.model.repositories;

import main.model.entities.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TagRepository extends JpaRepository<Tag, Integer> {

    @Query(value = "SELECT * FROM tags t" +
            " WHERE t.name = ?", nativeQuery = true)
    Tag getTagByTagName(String currentTagName);

    // Получаются теги только на доступные на сайте посты
    @Query(value = "SELECT DISTINCT t.* FROM tags AS t " +
            "INNER JOIN tag2post t2p ON t.id = t2p.tag_id " +
            "INNER JOIN posts p ON p.id = t2p.post_id " +
            "WHERE p.is_active = 1 " +
            "AND p.moderation_status = 'ACCEPTED' " +
            "AND p.time < NOW() " +
            "ORDER BY t.id DESC", nativeQuery = true)
    List<Tag> getAllTagsListSortedByIdDesc();

    @Query(value = "SELECT DISTINCT t.* FROM tags AS t " +
            "INNER JOIN tag2post t2p ON t.id = t2p.tag_id " +
            "INNER JOIN posts p ON p.id = t2p.post_id " +
            "WHERE (t.name LIKE %?%) " +
            "AND p.is_active = 1 " +
            "AND p.moderation_status = 'ACCEPTED' " +
            "AND p.time < NOW() " +
            "ORDER BY t.id DESC", nativeQuery = true)
    List<Tag> getAllTagsListByQuerySortedByIdDesc(String query);

    // Для подсчета веса тега - по количеству ссылок в постах
    @Query(value = "SELECT COUNT(*) " +
            "FROM tag2post tp " +
            "WHERE tag_id = ?", nativeQuery = true)
    Integer getTagCountByTagId(int tagId);

    @Query(value = "SELECT MAX(tag_count) " +
            "FROM (SELECT COUNT(post_id) AS tag_count " +
            "FROM tag2post tp GROUP BY tp.tag_id) " +
            "AS max_tag_count", nativeQuery = true)
    Integer getMaxTagCount();
}