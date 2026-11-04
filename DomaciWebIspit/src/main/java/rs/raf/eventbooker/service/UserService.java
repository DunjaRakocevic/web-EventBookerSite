package rs.raf.eventbooker.service;

import rs.raf.eventbooker.model.User;
import rs.raf.eventbooker.model.UserType;

import java.util.List;
import java.util.Optional;

public interface UserService {

    List<User> list(String q, int limit, int offset);
    long count(String q);

    Optional<User> findById(long id);
    boolean setActive(long id, boolean active);
    User create(String email, String firstName, String lastName, String rawPassword, UserType type, boolean active);
    boolean update(long id, String email, String firstName, String lastName, UserType type); // ADMIN only
    boolean resetPassword(long id, String rawPassword);
}
