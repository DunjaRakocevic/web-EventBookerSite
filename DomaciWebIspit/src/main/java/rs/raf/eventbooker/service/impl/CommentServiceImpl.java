package rs.raf.eventbooker.service.impl;

import rs.raf.eventbooker.dto.PagedResponse;
import rs.raf.eventbooker.repository.CommentRepository;
import rs.raf.eventbooker.repository.EventRepository;
import rs.raf.eventbooker.service.CommentService;

import javax.ws.rs.NotFoundException;
import java.util.List;
import java.util.Map;

public class CommentServiceImpl implements CommentService {

    private final CommentRepository comments;
    private final EventRepository events;

    public CommentServiceImpl(CommentRepository comments, EventRepository events) {
        this.comments = comments;
        this.events = events;
    }

    @Override
    public Map<String, Object> create(long eventId, String authorName, String text) {
        if (authorName == null || authorName.isBlank() || authorName.length() > 100)
            throw new IllegalArgumentException("name_invalid");
        if (text == null || text.isBlank())
            throw new IllegalArgumentException("text_required");

        events.findById(eventId).orElseThrow(NotFoundException::new);
        return comments.create(eventId, authorName.trim(), text.trim());
    }

    @Override
    public PagedResponse<Map<String, Object>> list(long eventId, int page, int limit) {
        events.findById(eventId).orElseThrow(NotFoundException::new);

        final int p = Math.max(1, page);
        final int l = Math.max(1, limit);
        final int offset = (p - 1) * l;

        List<Map<String,Object>> items = comments.listForEvent(eventId, l, offset);
        int total = comments.countByEventId(eventId);

        PagedResponse<Map<String,Object>> out = new PagedResponse<>();
        out.setPage(p);
        out.setLimit(l);
        out.setTotal(total);
        out.setTotalPages((int)Math.max(1, Math.ceil((double)total / Math.max(1, l))));
        out.setItems(items);
        return out;
    }

    @Override
    public void react(long commentId, String visitorKey, int reaction) {
        if (reaction != 1 && reaction != -1) throw new IllegalArgumentException("reaction_invalid");
        comments.findById(commentId).orElseThrow(NotFoundException::new);
        comments.upsertReaction(commentId, visitorKey, reaction);
    }

    @Override
    public Map<String, Integer> reactionCounts(long commentId) {
        comments.findById(commentId).orElseThrow(NotFoundException::new);
        return comments.reactionCounts(commentId);
    }

    @Override
    public boolean delete(long commentId, long requesterId, boolean isAdmin) {
        long owner = comments.findEventAuthorIdForComment(commentId)
                .orElseThrow(NotFoundException::new);
        if (!isAdmin && owner != requesterId) throw new IllegalStateException("forbidden");
        return comments.delete(commentId);
    }
}
