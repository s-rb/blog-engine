package main.services;

import lombok.extern.slf4j.Slf4j;
import main.api.response.GetTagsResponse;
import main.api.response.ResponseApi;
import main.model.entities.Tag;
import main.model.repositories.TagRepository;
import main.services.interfaces.TagRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

@Slf4j
@Service
public class TagRepositoryServiceImpl implements TagRepositoryService {

    @Autowired
    private TagRepository tagRepository;

    @Override
    public ResponseEntity<ResponseApi> getTags(String query) {
        if (query == null || query.equals("") || query.isBlank()) {
            return getTagsWithoutQuery();
        } else {
            List<Tag> queriedTags = tagRepository.getAllTagsListByQuerySortedByIdDesc(query);
            return getResponseEntityByTagsList(queriedTags);
        }
    }

    @Override
    public Tag addTag(Tag tag) {
        if (tag == null) {
            log.warn("--- Для добавления в репозиторий передан пустой тэг");
            return null;
        } else {
            Tag newTag = tagRepository.save(tag);
            log.info("--- Добавлен тэг: {id:" + newTag.getId() + ", name:" + newTag.getName() + "}");
            return newTag;
        }
    }

    @Override
    public void deleteTag(Tag tag) {
        tagRepository.delete(tag);
        log.info("--- Удален тэг: " + tag.getName());
    }

    @Override
    public ResponseEntity<ResponseApi> getTagsWithoutQuery() {
        List<Tag> allTags = tagRepository.getAllTagsListSortedByIdDesc();
        return getResponseEntityByTagsList(allTags);
    }

    private ResponseEntity<ResponseApi> getResponseEntityByTagsList(List<Tag> allTags) {
        HashMap<String, Double> queryTagsMap = new HashMap<>();
        if (!allTags.isEmpty()) {
            Integer mostFrequentTagCount = tagRepository.getMaxTagCount();
            for (Tag tag : allTags) {
                Double weight = ((double) tagRepository.getTagCountByTagId(tag.getId()) / (double) mostFrequentTagCount);
                queryTagsMap.put(tag.getName(), weight);
            }
            ResponseEntity<ResponseApi> response = new ResponseEntity<>(new GetTagsResponse(queryTagsMap), HttpStatus.OK);
            log.info("--- Направляется ответ " + (response.getBody() == null ? "с пустым телом"
                    : "с тегами: " + response.getBody().toString()));
            return response;
        } else {
            ResponseEntity<ResponseApi> response = ResponseEntity.status(HttpStatus.OK).body(null);
            log.info("--- Теги отсутствуют");
            return response;
        }
    }
}