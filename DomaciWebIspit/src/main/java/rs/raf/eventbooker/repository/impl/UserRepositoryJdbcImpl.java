package rs.raf.eventbooker.repository.impl;

import rs.raf.eventbooker.db.Db;
import rs.raf.eventbooker.model.User;
import rs.raf.eventbooker.model.UserType;
import rs.raf.eventbooker.repository.UserRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserRepositoryJdbcImpl implements UserRepository {

    private User map(ResultSet rs) throws SQLException {
        return new User(
                rs.getLong("id"),
                rs.getString("email"),
                rs.getString("first_name"),
                rs.getString("last_name"),
                rs.getString("password_hash"),
                rs.getBoolean("active"),
                UserType.valueOf(rs.getString("type"))
        );
    }

    @Override
    public Optional<User> findById(long id) {
        String sql = "SELECT * FROM users WHERE id=?";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = "SELECT * FROM users WHERE email=?";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (var rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<User> findAll(String q, int limit, int offset) {
        String base = """
            SELECT id,email,first_name,last_name,password_hash,active,type
            FROM users
            """;
        boolean hasQ = q != null && !q.isBlank();
        String where = hasQ ? " WHERE LOWER(email) LIKE ? OR LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? " : "";
        String sql = base + where + " ORDER BY id DESC LIMIT ? OFFSET ?";

        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            int i = 1;
            if (hasQ) {
                String like = "%" + q.trim().toLowerCase() + "%";
                ps.setString(i++, like);
                ps.setString(i++, like);
                ps.setString(i++, like);
            }
            ps.setInt(i++, limit);
            ps.setInt(i, offset);

            try (var rs = ps.executeQuery()) {
                List<User> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public long countAll(String q) {
        String base = "SELECT COUNT(*) FROM users";
        boolean hasQ = q != null && !q.isBlank();
        String where = hasQ ? " WHERE LOWER(email) LIKE ? OR LOWER(first_name) LIKE ? OR LOWER(last_name) LIKE ? " : "";
        String sql = base + where;
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            int i = 1;
            if (hasQ) {
                String like = "%" + q.trim().toLowerCase() + "%";
                ps.setString(i++, like);
                ps.setString(i++, like);
                ps.setString(i, like);
            }
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong(1) : 0L;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean emailExists(String email) {
        String sql = "SELECT 1 FROM users WHERE email=?";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, email.trim().toLowerCase());
            try (var rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public User create(User u) {
        String sql = """
            INSERT INTO users(email, first_name, last_name, password_hash, active, type)
            VALUES (?,?,?,?,?,?)
            """;
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, u.getEmail());
            ps.setString(2, u.getFirstName());
            ps.setString(3, u.getLastName());
            ps.setString(4, u.getPasswordHash());
            ps.setBoolean(5, u.isActive());
            ps.setString(6, u.getType().name());
            ps.executeUpdate();
            try (var k = ps.getGeneratedKeys()) {
                if (k.next()) u.setId(k.getLong(1));
            }
            return u;
        } catch (SQLException e) {
            // unique email
            if (e instanceof SQLIntegrityConstraintViolationException) {
                throw new RuntimeException("email_taken", e);
            }
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean setActive(long id, boolean active) {
        String sql = "UPDATE users SET active=? WHERE id=?";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setBoolean(1, active);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean resetPassword(long id, String bcryptHash) {
        String sql = "UPDATE users SET password_hash=? WHERE id=?";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, bcryptHash);
            ps.setLong(2, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }



    @Override
    public boolean update(long id, String email, String firstName, String lastName, UserType type) {
        String sql = "UPDATE users SET email=?, first_name=?, last_name=?, type=? WHERE id=?";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, email);
            ps.setString(2, firstName);
            ps.setString(3, lastName);
            ps.setString(4, type == null ? "EVENT_CREATOR" : type.name());
            ps.setLong(5, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            if (e instanceof SQLIntegrityConstraintViolationException) throw new RuntimeException("email_taken", e);
            throw new RuntimeException(e);
        }
    }

}
