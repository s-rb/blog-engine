package main.api.response;

import java.util.HashMap;
import java.util.Map;

public class GetStatisticsResponse implements ResponseApi {

    private Map<String, Object> map;

    public GetStatisticsResponse(int postsCount, int allLikesCount, int allDislikeCount, int viewsCount,
                                 String firstPublicationDate) {
        this.map = new HashMap<>();
        map.put("postsCount", postsCount);
        map.put("likesCount", allLikesCount);
        map.put("dislikesCount", allDislikeCount);
        map.put("viewsCount", viewsCount);
        map.put("firstPublication", firstPublicationDate);
    }

    public Map<String, Object> getMap() {
        return map;
    }

    public void setMap(Map<String, Object> map) {
        this.map = map;
    }

    @Override
    public String toString() {
        return "ResponseStatistics{" +
                "map=" + map +
                '}';
    }
}
