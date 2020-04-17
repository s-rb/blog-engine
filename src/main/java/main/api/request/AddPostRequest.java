package main.api.request;

import java.util.List;

public class AddPostRequest implements RequestApi {

    private String time;
    private Byte active;
    private String title;
    private String text;
    private List<String> tags;

    public AddPostRequest(String time, byte active, String title, String text, List<String> tags) {
        this.time = time;
        this.active = active;
        this.title = title;
        this.text = text;
        this.tags = tags;
    }

    public AddPostRequest() {
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public byte getActive() {
        return active;
    }

    public void setActive(byte active) {
        this.active = active;
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

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
