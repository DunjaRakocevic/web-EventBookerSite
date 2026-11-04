package rs.raf.eventbooker.model;

public class User {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String passwordHash;
    private boolean active;
    private UserType type;

    public User(Long id, String email, String firstName, String lastName, String passwordHash, boolean active, UserType type) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.passwordHash = passwordHash;
        this.active = active;
        this.type = type;
    }

    public UserType getType() { return type; }
    public User setType(UserType type) { this.type = type; return this; }

    public Long getId() { return id; }
    public User setId(Long id) { this.id = id; return this; }
    public String getEmail() { return email; }
    public User setEmail(String email) { this.email = email; return this; }
    public String getFirstName() { return firstName; }
    public User setFirstName(String firstName) { this.firstName = firstName; return this; }
    public String getLastName() { return lastName; }
    public User setLastName(String lastName) { this.lastName = lastName; return this; }
    public String getPasswordHash() { return passwordHash; }
    public User setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; return this; }
    public boolean isActive() { return active; }
    public User setActive(boolean active) { this.active = active; return this; }
    }
