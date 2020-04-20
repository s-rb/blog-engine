package main.services.interfaces;

import main.api.response.ResponseApi;
import main.model.entities.Tag;
import org.springframework.http.ResponseEntity;

public interface TagRepositoryService {

    ResponseEntity<ResponseApi> getTags(String query);

    Tag addTag(Tag tag);

    ResponseEntity<ResponseApi> getTagsWithoutQuery();
}
