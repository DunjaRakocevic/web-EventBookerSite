package rs.raf.eventbooker.service;

import rs.raf.eventbooker.dto.PagedResponse;
import java.util.Map;

public interface CommentService {
    Map<String,Object> create(long eventId, String authorName, String text);
    PagedResponse<Map<String,Object>> list(long eventId, int page, int limit);

    void react(long commentId, String visitorKey, int reaction);
    Map<String,Integer> reactionCounts(long commentId);

    boolean delete(long commentId, long requesterId, boolean isAdmin);
}
