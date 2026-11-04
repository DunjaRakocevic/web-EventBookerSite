package rs.raf.eventbooker.repository;

import rs.raf.eventbooker.model.User;
import rs.raf.eventbooker.model.UserType;

import java.util.List;
import java.util.Optional;

public interface UserRepository {
    Optional<User> findById(long id);
    Optional<User> findByEmail(String email);

    List<User> findAll(String q, int limit, int offset);
    long countAll(String q);

    boolean emailExists(String email);

    User create(User u);

    boolean setActive(long id, boolean active);

    boolean resetPassword(long id, String bcryptHash);


    boolean update(long id, String email, String firstName, String lastName, UserType type);

}
