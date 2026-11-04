package rs.raf.eventbooker.service;

import rs.raf.eventbooker.model.Event;

import java.time.LocalDateTime;
import java.util.List;

public interface EventService {


    List<Event> listForAdmin(int limit, int offset);
    List<Event> listForAuthor(long authorId, int limit, int offset);
    List<Event> searchForAdmin(String q, int limit, int offset);
    List<Event> searchForAuthor(long authorId, String q, int limit, int offset);


    Event create(long authorId,
                 String title,
                 String description,
                 LocalDateTime scheduledAt,
                 String location,
                 Long categoryId,
                 Integer maxCapacity,
                 List<String> tagNames);

    Event update(long authorId,
                 boolean isAdmin,
                 long eventId,
                 String title,
                 String description,
                 LocalDateTime scheduledAt,
                 String location,
                 Long categoryId,
                 Integer maxCapacity,
                 List<String> tagNames);

    boolean delete(long authorId, boolean isAdmin, long eventId);
}
