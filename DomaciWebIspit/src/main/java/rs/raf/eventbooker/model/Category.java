package rs.raf.eventbooker.model;

public class Category {
    private Long id;
    private String name;
    private String description;

    public Category() {}
    public Category(Long id, String name, String description) {
        this.id = id; this.name = name; this.description = description;
    }

    public Long getId() { return id; }
    public Category setId(Long id) { this.id = id; return this; }

    public String getName() { return name; }
    public Category setName(String name) { this.name = name; return this; }

    public String getDescription() { return description; }
    public Category setDescription(String description) { this.description = description; return this; }
}
