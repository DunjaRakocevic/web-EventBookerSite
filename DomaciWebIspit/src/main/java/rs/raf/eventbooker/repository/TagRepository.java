package rs.raf.eventbooker.repository;

import rs.raf.eventbooker.model.Tag;
import java.util.*;

public interface TagRepository {
    Optional<Tag> findByName(String name);
    Tag create(String name);
    List<Tag> findForEvent(long eventId);
    void syncEventTags(long eventId, List<String> tagNames); // kreira nedostajuće, osvežava event_tags
    List<Long> findRelatedEventIds(long eventId, int limit); // do 3 srodna po deljenim tagovima
    List<Long> findEventIdsByTag(String tagName, int limit, int offset);
}
