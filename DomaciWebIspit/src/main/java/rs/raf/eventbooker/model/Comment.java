package rs.raf.eventbooker.model;

import java.time.LocalDateTime;

public class Comment {
    private Long id;
    private Long eventId;
    private String authorName;
    private String text;
    private LocalDateTime createdAt;

    public Comment() {}
    public Comment(Long id, Long eventId, String authorName, String text, LocalDateTime createdAt) {
        this.id = id; this.eventId = eventId; this.authorName = authorName; this.text = text; this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public String getAuthorName() { return authorName; }
    public String getText() { return text; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public void setText(String text) { this.text = text; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
