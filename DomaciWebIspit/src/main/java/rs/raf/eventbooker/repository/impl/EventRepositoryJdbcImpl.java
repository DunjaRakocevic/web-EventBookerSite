package rs.raf.eventbooker.repository.impl;

import rs.raf.eventbooker.db.Db;
import rs.raf.eventbooker.model.Event;
import rs.raf.eventbooker.repository.EventRepository;

import java.sql.*;
import java.util.*;

public class EventRepositoryJdbcImpl implements EventRepository {

    private Event map(ResultSet rs) throws SQLException {
        return new Event(
                rs.getLong("id"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("scheduled_at").toLocalDateTime(),
                rs.getString("location"),
                rs.getLong("views"),
                rs.getLong("author_id"),
                rs.getLong("category_id"),
                rs.getObject("max_capacity") == null ? null : rs.getInt("max_capacity")
        );
    }

    @Override public List<Event> findAll(int limit, int offset) {
        String sql = """
            SELECT * FROM events
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?""";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit); ps.setInt(2, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Event> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override public List<Event> findAllByAuthor(long authorId, int limit, int offset) {
        String sql = """
            SELECT * FROM events WHERE author_id=?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?""";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, authorId); ps.setInt(2, limit); ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Event> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override public Optional<Event> findById(long id) {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("SELECT * FROM events WHERE id=?")) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Event create(Event e) {
        String sql = """
        INSERT INTO events
          (title, description, created_at, scheduled_at, location, views, author_id, category_id, max_capacity)
        VALUES
          (?, ?, NOW(), ?, ?, 0, ?, ?, ?)
        """;
        try (var c = Db.getConnection();
             var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            int i = 1;
            ps.setString(i++, e.getTitle());
            ps.setString(i++, e.getDescription());
            ps.setTimestamp(i++, java.sql.Timestamp.valueOf(e.getScheduledAt()));
            ps.setString(i++, e.getLocation());
            ps.setLong(i++, e.getAuthorId());
            ps.setLong(i++, e.getCategoryId());
            if (e.getMaxCapacity() == null) ps.setNull(i++, Types.INTEGER);
            else ps.setInt(i++, e.getMaxCapacity());

            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                if (rs.next()) e.setId(rs.getLong(1));
            }
            // Vrati sveže učitan entitet (sa created_at i views iz baze)
            return findById(e.getId()).orElseThrow();
        } catch (SQLException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void incrementViewsOnce(long eventId) {
        String sql = "UPDATE events SET views = views + 1 WHERE id=?";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int recordViewOnce(long eventId, String visitorKey) {
        final String sql = """
        INSERT IGNORE INTO event_views(event_id, visitor_key, first_viewed_at)
        VALUES (?,?, NOW())
        """;
        try (var c = rs.raf.eventbooker.db.Db.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setString(2, visitorKey);
            final int inserted = ps.executeUpdate(); // 1 = prvi put, 0 = već postoji
            if (inserted == 1) {
                incrementViewsOnce(eventId);
            }
            return inserted;
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
    }



    @Override
    public Map<String,Integer> reactionCounts(long eventId) {
        String sql = """
        SELECT
          SUM(CASE WHEN value =  1 THEN 1 ELSE 0 END) AS likes,
          SUM(CASE WHEN value = -1 THEN 1 ELSE 0 END) AS dislikes
        FROM event_reactions
        WHERE event_id = ?""";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (var rs = ps.executeQuery()) {
                int likes = 0, dislikes = 0;
                if (rs.next()) {
                    likes = rs.getInt("likes");
                    dislikes = rs.getInt("dislikes");
                }
                return Map.of("likes", likes, "dislikes", dislikes);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void upsertReaction(long eventId, String visitorId, int reaction) {
        String sql = """
        INSERT INTO event_reactions(event_id, visitor_key, value, created_at)
        VALUES (?,?,?,NOW())
        ON DUPLICATE KEY UPDATE
            value = VALUES(value),
            created_at = NOW()""";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setString(2, visitorId);
            ps.setInt(3, reaction);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Long> listMostReactedIds(int limit) {
        String sql = """
        SELECT event_id, COUNT(*) AS total
        FROM event_reactions
        GROUP BY event_id
        ORDER BY total DESC
        LIMIT ?""";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (var rs = ps.executeQuery()) {
                List<Long> ids = new ArrayList<>();
                while (rs.next()) ids.add(rs.getLong("event_id"));
                return ids;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override public Event update(Event e) {
        String sql = """
            UPDATE events SET title=?, description=?, scheduled_at=?, location=?, category_id=?, max_capacity=?
            WHERE id=?""";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, e.getTitle());
            ps.setString(2, e.getDescription());
            ps.setTimestamp(3, Timestamp.valueOf(e.getScheduledAt()));
            ps.setString(4, e.getLocation());
            ps.setLong(5, e.getCategoryId());
            if (e.getMaxCapacity()==null) ps.setNull(6, Types.INTEGER); else ps.setInt(6, e.getMaxCapacity());
            ps.setLong(7, e.getId());
            ps.executeUpdate();
            return findById(e.getId()).orElseThrow();
        } catch (SQLException ex) { throw new RuntimeException(ex); }
    }

    @Override public boolean delete(long id) {
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement("DELETE FROM events WHERE id=?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Event> findAllByTag(String tag, int limit, int offset) {
        String sql = """
        SELECT e.*
        FROM events e
        JOIN event_tags et ON et.event_id = e.id
        JOIN tags t       ON t.id       = et.tag_id
        WHERE t.name = ?
        ORDER BY e.created_at DESC
        LIMIT ? OFFSET ?
        """;
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, tag.trim().toLowerCase());
            ps.setInt(2, limit);
            ps.setInt(3, Math.max(0, offset));
            try (var rs = ps.executeQuery()) {
                var out = new ArrayList<Event>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public java.util.List<Long> mostVisitedLastDaysIds(int days, int limit) {
        String sql = """
        SELECT event_id, COUNT(*) AS total
        FROM event_views
        WHERE first_viewed_at >= (NOW() - INTERVAL ? DAY)
        GROUP BY event_id
        ORDER BY total DESC
        LIMIT ?
        """;
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setInt(1, Math.max(1, days));
            ps.setInt(2, Math.max(1, limit));
            try (var rs = ps.executeQuery()) {
                var ids = new java.util.ArrayList<Long>();
                while (rs.next()) ids.add(rs.getLong("event_id"));
                return ids;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Event> findAllSearch(String q, int limit, int offset) {
        String sql = """
        SELECT * FROM events
        WHERE LOWER(title) LIKE ? OR LOWER(description) LIKE ?
        ORDER BY created_at DESC
        LIMIT ? OFFSET ?""";
        String pat = "%" + q.toLowerCase() + "%";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setString(i++, pat);
            ps.setString(i++, pat);
            ps.setInt(i++, limit);
            ps.setInt(i++, offset);
            try (var rs = ps.executeQuery()) {
                List<Event> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Event> findAllByAuthorSearch(long authorId, String q, int limit, int offset) {
        String sql = """
        SELECT * FROM events
        WHERE author_id = ?
          AND (LOWER(title) LIKE ? OR LOWER(description) LIKE ?)
        ORDER BY created_at DESC
        LIMIT ? OFFSET ?""";
        String pat = "%" + q.toLowerCase() + "%";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            int i = 1;
            ps.setLong(i++, authorId);
            ps.setString(i++, pat);
            ps.setString(i++, pat);
            ps.setInt(i++, limit);
            ps.setInt(i++, offset);
            try (var rs = ps.executeQuery()) {
                List<Event> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }


    @Override
    public int countSearch(String q) {
        String like = "%" + q.trim().toLowerCase() + "%";
        String sql = """
        SELECT COUNT(*)
        FROM events
        WHERE LOWER(title) LIKE ?
           OR LOWER(description) LIKE ?
    """;
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, like);
            ps.setString(2, like);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }



    @Override public int countAll() {
        String sql = "SELECT COUNT(*) FROM events";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql); var rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override public int countByAuthor(long authorId) {
        String sql = "SELECT COUNT(*) FROM events WHERE author_id=?";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, authorId);
            try (var rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override public int countByTag(String tagName) {
        String sql = """
        SELECT COUNT(DISTINCT e.id)
        FROM events e
        JOIN event_tags et ON et.event_id = e.id
        JOIN tags t       ON t.id = et.tag_id
        WHERE t.name = ?""";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setString(1, tagName.trim().toLowerCase());
            try (var rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Event> findAllByCategory(long categoryId, int limit, int offset) {
        String sql = """
        SELECT * FROM events
        WHERE category_id=?
        ORDER BY created_at DESC
        LIMIT ? OFFSET ?""";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (var rs = ps.executeQuery()) {
                java.util.List<Event> out = new java.util.ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (java.sql.SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int countByCategory(long categoryId) {
        String sql = "SELECT COUNT(*) FROM events WHERE category_id=?";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, categoryId);
            try (var rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int countByAuthorSearch(long authorId, String q) {
        final String like = "%" + q.trim().toLowerCase() + "%";
        final String sql = """
        SELECT COUNT(*)
        FROM events
        WHERE author_id = ?
          AND (LOWER(title) LIKE ?
               OR LOWER(description) LIKE ?
               OR LOWER(location) LIKE ?)
        """;
        try (var c = Db.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setLong(1, authorId);
            ps.setString(2, like);
            ps.setString(3, like);
            ps.setString(4, like);
            try (var rs = ps.executeQuery()) {
                rs.next();
                return rs.getInt(1);
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
    }

}
