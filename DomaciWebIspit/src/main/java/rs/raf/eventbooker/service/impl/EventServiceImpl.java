package rs.raf.eventbooker.service.impl;

import rs.raf.eventbooker.model.Event;
import rs.raf.eventbooker.repository.CategoryRepository;
import rs.raf.eventbooker.repository.EventRepository;
import rs.raf.eventbooker.repository.TagRepository;
import rs.raf.eventbooker.service.EventService;

import javax.ws.rs.NotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

public class EventServiceImpl implements EventService {

    private final EventRepository events;
    private final CategoryRepository categories;
    private final TagRepository tags;

    public EventServiceImpl(EventRepository events,
                            CategoryRepository categories,
                            TagRepository tags) {
        this.events = Objects.requireNonNull(events);
        this.categories = Objects.requireNonNull(categories);
        this.tags = Objects.requireNonNull(tags);
    }



    @Override
    public List<Event> listForAdmin(int limit, int offset) {
        return events.findAll(Math.max(1, limit), Math.max(0, offset));
    }

    @Override
    public List<Event> listForAuthor(long authorId, int limit, int offset) {
        return events.findAllByAuthor(authorId, Math.max(1, limit), Math.max(0, offset));
    }

    @Override
    public List<Event> searchForAdmin(String q, int limit, int offset) {
        final String qq = (q == null) ? "" : q.trim();
        if (qq.isEmpty()) return listForAdmin(limit, offset);
        return events.findAllSearch(qq, Math.max(1, limit), Math.max(0, offset));
    }

    @Override
    public List<Event> searchForAuthor(long authorId, String q, int limit, int offset) {
        final String qq = (q == null) ? "" : q.trim();
        if (qq.isEmpty()) return listForAuthor(authorId, limit, offset);
        return events.findAllByAuthorSearch(authorId, qq, Math.max(1, limit), Math.max(0, offset));
    }


    private void validateCommon(String title, String description, LocalDateTime scheduledAt,
                                String location, Long categoryId) {
        if (title == null || title.isBlank()) throw new IllegalArgumentException("title_required");
        if (description == null || description.isBlank()) throw new IllegalArgumentException("description_required");
        if (scheduledAt == null) throw new IllegalArgumentException("scheduled_at_required");
        if (location == null || location.isBlank()) throw new IllegalArgumentException("location_required");
        if (categoryId == null) throw new IllegalArgumentException("category_required");
        categories.findById(categoryId).orElseThrow(() -> new IllegalArgumentException("category_not_found"));
    }


    @Override
    public Event create(long authorId,
                        String title,
                        String description,
                        LocalDateTime scheduledAt,
                        String location,
                        Long categoryId,
                        Integer maxCapacity,
                        List<String> tagNames) {

        validateCommon(title, description, scheduledAt, location, categoryId);

        Event e = new Event();
        e.setTitle(title.trim());
        e.setDescription(description.trim());
        e.setScheduledAt(scheduledAt);
        e.setLocation(location.trim());
        e.setAuthorId(authorId);
        e.setCategoryId(categoryId);
        e.setMaxCapacity(maxCapacity);


        Event created = events.create(e);


        tags.syncEventTags(created.getId(), tagNames);

        return events.findById(created.getId()).orElse(created);
    }


    @Override
    public Event update(long authorId,
                        boolean isAdmin,
                        long eventId,
                        String title,
                        String description,
                        LocalDateTime scheduledAt,
                        String location,
                        Long categoryId,
                        Integer maxCapacity,
                        List<String> tagNames) {

        Event existing = events.findById(eventId).orElseThrow(NotFoundException::new);
        if (!isAdmin && !Objects.equals(existing.getAuthorId(), authorId)) {
            throw new IllegalStateException("forbidden");
        }

        validateCommon(title, description, scheduledAt, location, categoryId);

        existing.setTitle(title.trim());
        existing.setDescription(description.trim());
        existing.setScheduledAt(scheduledAt);
        existing.setLocation(location.trim());
        existing.setCategoryId(categoryId);
        existing.setMaxCapacity(maxCapacity);

        Event updated = events.update(existing);


        tags.syncEventTags(updated.getId(), tagNames);

        return events.findById(updated.getId()).orElse(updated);
    }


    @Override
    public boolean delete(long authorId, boolean isAdmin, long eventId) {
        Event existing = events.findById(eventId).orElseThrow(NotFoundException::new);
        if (!isAdmin && !Objects.equals(existing.getAuthorId(), authorId)) {
            throw new IllegalStateException("forbidden");
        }
        return events.delete(eventId);
    }
}
