package main.api.response;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class GetTagsResponse implements ResponseApi {

    private List<TagWithWeight> tags;

    public GetTagsResponse(Map<String, Double> tagNameAndWeightMap) {
        tags = new LinkedList<>();
        for (String key : tagNameAndWeightMap.keySet()) {
            TagWithWeight tagWithWeight = new TagWithWeight(key, tagNameAndWeightMap.get(key));
            tags.add(tagWithWeight);
        }
    }

    @Override
    public String toString() {
        return "ResponseTags{" +
                "tags=" + tags +
                '}';
    }

    public List<TagWithWeight> getTags() {
        return tags;
    }

    public void setTags(List<TagWithWeight> tags) {
        this.tags = tags;
    }

    static class TagWithWeight {

        private String name;
        private double weight;

        private TagWithWeight(String name, double weight) {
            this.name = name;
            this.weight = weight;
        }

        @Override
        public String toString() {
            return "ResponseTagApi{" +
                    "name='" + name + '\'' +
                    ", weight=" + weight +
                    '}';
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public double getWeight() {
            return weight;
        }

        public void setWeight(double weight) {
            this.weight = weight;
        }
    }
}
