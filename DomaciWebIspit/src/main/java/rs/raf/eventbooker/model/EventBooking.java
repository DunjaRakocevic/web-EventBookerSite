package rs.raf.eventbooker.model;

import java.time.LocalDateTime;

public class EventBooking {
    private Long id;
    private Long eventId;
    private String visitorName;
    private String visitorEmail;
    private Integer seats;
    private LocalDateTime createdAt;

    public EventBooking() { }

    public EventBooking(Long id, Long eventId, String visitorName, String visitorEmail, Integer seats, LocalDateTime createdAt) {
        this.id = id; this.eventId = eventId; this.visitorName = visitorName;
        this.visitorEmail = visitorEmail; this.seats = seats; this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEventId() {
        return eventId;
    }

    public void setEventId(Long eventId) {
        this.eventId = eventId;
    }

    public String getVisitorName() {
        return visitorName;
    }

    public void setVisitorName(String visitorName) {
        this.visitorName = visitorName;
    }

    public String getVisitorEmail() {
        return visitorEmail;
    }

    public void setVisitorEmail(String visitorEmail) {
        this.visitorEmail = visitorEmail;
    }

    public Integer getSeats() {
        return seats;
    }

    public void setSeats(Integer seats) {
        this.seats = seats;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
