package main.services;

import main.model.entities.TagToPost;
import main.model.repositories.TagToPostRepository;
import main.services.interfaces.TagToPostRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TagToPostRepositoryServiceImpl implements TagToPostRepositoryService {

    @Autowired
    private TagToPostRepository tagToPostRepository;

    @Override
    public TagToPost addTagToPost(TagToPost tagToPost) {
        if (tagToPost == null) {
            return null;
        } else {
            return tagToPostRepository.save(tagToPost);
        }
    }

    @Override
    public void deleteTagToPost(TagToPost tagToPost) {
        tagToPostRepository.delete(tagToPost);
    }
}
