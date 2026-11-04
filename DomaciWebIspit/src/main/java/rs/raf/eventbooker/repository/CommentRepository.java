// rs/raf/eventbooker/repository/CommentRepository.java
package rs.raf.eventbooker.repository;

import rs.raf.eventbooker.model.Comment;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface CommentRepository {
    Map<String,Object> create(long eventId, String name, String text);
    List<Map<String,Object>> listForEvent(long eventId, int limit, int offset);

    // DODATO za EMS:
    Optional<Long> findEventAuthorIdForComment(long commentId); // vrati author_id eventa kome komentar pripada
    boolean delete(long commentId);

    void upsertReaction(long commentId, String visitorKey, int reaction);
    Map<String,Integer> reactionCounts(long commentId); // value: 1 ili -1
    List<Long> mostReactedIdsForEvent(long eventId, int limit);


    java.util.Optional<Comment> findById(long id);

    int countByEventId(long eventId);
}
