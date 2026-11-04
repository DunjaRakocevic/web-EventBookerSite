package rs.raf.eventbooker.model;

import java.time.LocalDateTime;

public class Booking {
    private Long id;
    private Long eventId;
    private String name;
    private String email;
    private int seats;
    private LocalDateTime createdAt;

    public Booking() { }

    public Booking(Long id, Long eventId, String name, String email, int seats, LocalDateTime createdAt) {
        this.id = id;
        this.eventId = eventId;
        this.name = name;
        this.email = email;
        this.seats = seats;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public int getSeats() { return seats; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setEventId(Long eventId) { this.eventId = eventId; }
    public void setName(String name) { this.name = name; }
    public void setEmail(String email) { this.email = email; }
    public void setSeats(int seats) { this.seats = seats; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
