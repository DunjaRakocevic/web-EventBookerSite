package rs.raf.eventbooker.model;

import java.time.LocalDateTime;

public class Rsvp {
    private Long id;
    private Long eventId;
    private Long userId;
    private LocalDateTime createdAt;

    public Rsvp() {}


    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getEventId() { return eventId; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
