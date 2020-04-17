package main.services.interfaces;

import main.model.entities.TagToPost;

public interface TagToPostRepositoryService {

    TagToPost addTagToPost(TagToPost tagToPost);

    void deleteTagToPost(TagToPost tagToPost);
}
