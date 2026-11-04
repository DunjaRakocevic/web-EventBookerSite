package rs.raf.eventbooker.service.impl;

import org.mindrot.jbcrypt.BCrypt;
import rs.raf.eventbooker.model.User;
import rs.raf.eventbooker.model.UserType;
import rs.raf.eventbooker.repository.UserRepository;
import rs.raf.eventbooker.service.UserService;

import java.util.List;
import java.util.Optional;

public class UserServiceImpl implements UserService {

    private final UserRepository users;

    public UserServiceImpl(UserRepository users) {
        this.users = users;
    }

    @Override
    public List<User> list(String q, int limit, int offset) {
        return users.findAll(q, limit, offset);
    }

    @Override
    public long count(String q) {
        return users.countAll(q);
    }

    @Override
    public Optional<User> findById(long id) {
        return users.findById(id);
    }


    @Override
    public boolean setActive(long id, boolean active) {
        return users.setActive(id, active);
    }

    @Override
    public boolean resetPassword(long id, String rawPassword) {
        if (rawPassword == null || rawPassword.isBlank() || rawPassword.length() < 6)
            throw new IllegalArgumentException("password_too_short");
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
        return users.resetPassword(id, hash);
    }

    private void validate(String email, String firstName, String lastName, String rawPassword) {
        if (email == null || email.isBlank() || !email.contains("@")) throw new IllegalArgumentException("email_invalid");
        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("first_name_required");
        if (lastName == null || lastName.isBlank()) throw new IllegalArgumentException("last_name_required");
        if (rawPassword == null || rawPassword.length() < 6) throw new IllegalArgumentException("password_too_short");
    }


    @Override
    public User create(String email, String firstName, String lastName, String rawPassword, UserType type, boolean active) {
        if (type == null) type = UserType.EVENT_CREATOR;
        validate(email, firstName, lastName, rawPassword);
        if (users.emailExists(email)) throw new IllegalStateException("email_taken");
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt(10));
        User u = new User(null, email.trim().toLowerCase(), firstName.trim(), lastName.trim(), hash,
                type == UserType.ADMIN ? true : active, // ADMIN je uvek aktivan
                type);
        return users.create(u);
    }

    @Override
    public boolean update(long id, String email, String firstName, String lastName, UserType type) {
        if (email == null || email.isBlank() || !email.contains("@")) throw new IllegalArgumentException("email_invalid");
        if (firstName == null || firstName.isBlank()) throw new IllegalArgumentException("first_name_required");
        if (lastName == null || lastName.isBlank()) throw new IllegalArgumentException("last_name_required");
        return users.update(id, email.trim().toLowerCase(), firstName.trim(), lastName.trim(), type);
    }

}
