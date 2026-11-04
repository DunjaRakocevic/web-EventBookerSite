package rs.raf.eventbooker.repository.impl;

import rs.raf.eventbooker.db.Db;
import rs.raf.eventbooker.model.Category;
import rs.raf.eventbooker.repository.CategoryRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class CategoryRepositoryJdbcImpl implements CategoryRepository {

    @Override
    public Category create(String name, String description) {
        String sql = "INSERT INTO categories(name, description) VALUES (?, ?)";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim());
            ps.setString(2, description.trim());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return new Category(rs.getLong(1), name.trim(), description.trim());
                }
            }
            throw new RuntimeException("No generated key for category");
        } catch (SQLIntegrityConstraintViolationException dup) {
            throw new IllegalStateException("Category name already exists", dup); // 409 u resource-u
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Category> findById(long id) {
        String sql = "SELECT id, name, description FROM categories WHERE id = ?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(map(rs));
                }
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Category> findByName(String name) {
        String sql = "SELECT id, name, description FROM categories WHERE name = ?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(map(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Category update(long id, String name, String description) {
        String sql = "UPDATE categories SET name = ?, description = ? WHERE id = ?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, name.trim());
            ps.setString(2, description.trim());
            ps.setLong(3, id);
            int n = ps.executeUpdate();
            if (n == 0) throw new IllegalStateException("Category not found"); // 404 u resource-u
            return new Category(id, name.trim(), description.trim());
        } catch (SQLIntegrityConstraintViolationException dup) {
            throw new IllegalStateException("Category name already exists", dup); // 409
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean delete(long id) {
        String sql = "DELETE FROM categories WHERE id = ?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public boolean hasEvents(long categoryId) {
        String sql = "SELECT COUNT(*) FROM events WHERE category_id = ?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM categories";
        try (Connection con = Db.getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            rs.next();
            return rs.getLong(1);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Category> list(int page, int limit) {
        int p = Math.max(1, page);
        int l = Math.max(1, limit);
        int offset = (p - 1) * l;
        String sql = "SELECT id, name, description FROM categories ORDER BY name ASC LIMIT ? OFFSET ?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setInt(1, l);
            ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Category> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Category> listFiltered(String q, int page, int limit) {
        int p = Math.max(1, page);
        int l = Math.max(1, limit);
        int offset = (p - 1) * l;
        String like = "%" + q.trim().toLowerCase() + "%";
        String sql = "SELECT id, name, description FROM categories " +
                "WHERE LOWER(name) LIKE ? OR LOWER(description) LIKE ? " +
                "ORDER BY name ASC LIMIT ? OFFSET ?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setInt(3, l);
            ps.setInt(4, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Category> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public long countFiltered(String q) {
        String like = "%" + q.trim().toLowerCase() + "%";
        String sql = "SELECT COUNT(*) FROM categories WHERE LOWER(name) LIKE ? OR LOWER(description) LIKE ?";
        try (Connection con = Db.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Category map(ResultSet rs) throws SQLException {
        return new Category(
                rs.getLong("id"),
                rs.getString("name"),
                rs.getString("description")
        );
    }
}
