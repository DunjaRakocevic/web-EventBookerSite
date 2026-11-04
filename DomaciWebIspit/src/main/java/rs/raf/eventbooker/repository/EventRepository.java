package rs.raf.eventbooker.repository;

import rs.raf.eventbooker.model.Event;
import java.util.*;

public interface EventRepository {
    List<Event> findAll(int limit, int offset);                    // za ADMIN
    List<Event> findAllByAuthor(long authorId, int limit, int offset); // za CREATOR
    Optional<Event> findById(long id);
    Event create(Event e);
    Event update(Event e);
    boolean delete(long id);

    void incrementViewsOnce(long eventId);
    Map<String,Integer> reactionCounts(long eventId);
    void upsertReaction(long eventId, String visitorId, int reaction);
    List<Long> listMostReactedIds(int limit);
    List<Event> findAllByTag(String tag, int limit, int offset);
    int recordViewOnce(long eventId, String visitorKey);
    java.util.List<Long> mostVisitedLastDaysIds(int days, int limit);

    List<Event> findAllSearch(String q, int limit, int offset);
    List<Event> findAllByAuthorSearch(long authorId, String q, int limit, int offset);
    int countSearch(String q);
    int countAll();
    int countByAuthor(long authorId);
    int countByTag(String tagName);


    List<Event> findAllByCategory(long categoryId, int limit, int offset);

    int countByAuthorSearch(long authorId, String q);

    int countByCategory(long categoryId);



}