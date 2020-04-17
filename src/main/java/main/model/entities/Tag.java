package main.model.entities;

import javax.persistence.*;
import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
public class Tag implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(nullable = false)
    private int id;

    @Column(nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;

    @OneToMany(mappedBy = "tag", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<TagToPost> tagsToPostsSet = new HashSet<TagToPost>();

    public Tag() {
    }

    public Tag(String tagName) {
        name = tagName;
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

    public Set<TagToPost> getTagsToPostsSet() {
        return tagsToPostsSet;
    }

    public void setTagsToPostsSet(Set<TagToPost> tagsToPostsSet) {
        this.tagsToPostsSet = tagsToPostsSet;
    }


}