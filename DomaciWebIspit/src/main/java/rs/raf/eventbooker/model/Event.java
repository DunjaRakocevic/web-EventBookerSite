package rs.raf.eventbooker.model;

import java.time.LocalDateTime;
import java.util.List;

public class Event {
    private Long id;
    private String title;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime scheduledAt;
    private String location;
    private long views;
    private Long authorId;
    private Long categoryId;
    private Integer maxCapacity;
    private List<String> tags;

    public Event() {}
    public Event(Long id, String title, String description, LocalDateTime createdAt,
                 LocalDateTime scheduledAt, String location, long views,
                 Long authorId, Long categoryId, Integer maxCapacity) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.createdAt = createdAt;
        this.scheduledAt = scheduledAt;
        this.location = location;
        this.views = views;
        this.authorId = authorId;
        this.categoryId = categoryId;
        this.maxCapacity = maxCapacity;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getScheduledAt() { return scheduledAt; }
    public void setScheduledAt(LocalDateTime scheduledAt) { this.scheduledAt = scheduledAt; }
    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }
    public long getViews() { return views; }
    public void setViews(long views) { this.views = views; }
    public Long getAuthorId() { return authorId; }
    public void setAuthorId(Long authorId) { this.authorId = authorId; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public Integer getMaxCapacity() { return maxCapacity; }
    public void setMaxCapacity(Integer maxCapacity) { this.maxCapacity = maxCapacity; }
    public List<String> getTags() { return tags; }
    public void setTags(List<String> tags) { this.tags = tags; }
}
