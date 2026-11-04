package rs.raf.eventbooker.repository.impl;

import rs.raf.eventbooker.db.Db;
import rs.raf.eventbooker.model.Tag;
import rs.raf.eventbooker.repository.TagRepository;

import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class TagRepositoryJdbcImpl implements TagRepository {

    @Override
    public Optional<Tag> findByName(String name) {
        String sql = "SELECT id, name FROM tags WHERE name=?";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, name.trim().toLowerCase());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(new Tag(rs.getLong(1), rs.getString(2))) : Optional.empty();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Tag create(String name) {
        String sql = "INSERT INTO tags(name) VALUES (?)";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name.trim().toLowerCase());
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) {
                if (k.next()) return new Tag(k.getLong(1), name.trim().toLowerCase());
            }
            throw new RuntimeException("no key");
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Tag> findForEvent(long eventId) {
        String sql = """
            SELECT t.id, t.name
            FROM tags t
            JOIN event_tags et ON et.tag_id = t.id
            WHERE et.event_id = ?
            ORDER BY t.name ASC""";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Tag> out = new ArrayList<>();
                while (rs.next()) out.add(new Tag(rs.getLong(1), rs.getString(2)));
                return out;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void syncEventTags(long eventId, List<String> tagNames) {
        List<String> names = (tagNames == null) ? List.of() :
                tagNames.stream()
                        .filter(Objects::nonNull)
                        .map(s -> s.trim().toLowerCase())
                        .filter(s -> !s.isEmpty())
                        .distinct()
                        .toList();

        try (Connection c = Db.getConnection()) {
            c.setAutoCommit(false);

            try {
                // postojeći set
                Set<Long> existing = new HashSet<>();
                try (PreparedStatement ps = c.prepareStatement(
                        "SELECT tag_id FROM event_tags WHERE event_id=?")) {
                    ps.setLong(1, eventId);
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) existing.add(rs.getLong(1));
                    }
                }

                // željeni set
                Set<Long> desired = new HashSet<>();
                for (String n : names) {
                    Long id = findIdOrCreate(c, n);
                    desired.add(id);
                }

                // DELETE viškove
                List<Long> toRemove = existing.stream()
                        .filter(id -> !desired.contains(id))
                        .toList();
                if (!toRemove.isEmpty()) {
                    String placeholders = toRemove.stream().map(x -> "?")
                            .collect(Collectors.joining(","));
                    String delSql = "DELETE FROM event_tags WHERE event_id=? AND tag_id IN (" + placeholders + ")";
                    try (PreparedStatement ps = c.prepareStatement(delSql)) {
                        int i = 1;
                        ps.setLong(i++, eventId);
                        for (Long id : toRemove) ps.setLong(i++, id);
                        ps.executeUpdate();
                    }
                }

                // INSERT nove (IGNORE zbog UNIQUE(event_id, tag_id))
                try (PreparedStatement ps = c.prepareStatement(
                        "INSERT IGNORE INTO event_tags(event_id, tag_id) VALUES (?,?)")) {
                    for (Long id : desired) {
                        ps.setLong(1, eventId);
                        ps.setLong(2, id);
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                c.commit();
            } catch (Exception e) {
                try { c.rollback(); } catch (Exception ignore) {}
                throw e; // pusti da spoljašnji catch wrap-uje
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Long findIdOrCreate(Connection c, String name) throws SQLException {
        // lookup
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM tags WHERE name=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getLong(1); }
        }
        // create
        try (PreparedStatement ps = c.prepareStatement(
                "INSERT INTO tags(name) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, name);
            ps.executeUpdate();
            try (ResultSet k = ps.getGeneratedKeys()) { if (k.next()) return k.getLong(1); }
        }
        // fallback lookup
        try (PreparedStatement ps = c.prepareStatement("SELECT id FROM tags WHERE name=?")) {
            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) { if (rs.next()) return rs.getLong(1); }
        }
        throw new SQLException("tag upsert failed");
    }

    @Override
    public List<Long> findRelatedEventIds(long eventId, int limit) {
        // Agregacija u podupitu → nema konflikta sa ONLY_FULL_GROUP_BY
        String sql = """
            SELECT e2.id
            FROM (
                SELECT et2.event_id AS id, COUNT(*) AS overlap
                FROM event_tags et1
                JOIN event_tags et2
                  ON et1.tag_id = et2.tag_id AND et2.event_id <> et1.event_id
                WHERE et1.event_id = ?
                GROUP BY et2.event_id
            ) x
            JOIN events e2 ON e2.id = x.id
            ORDER BY x.overlap DESC, e2.created_at DESC
            LIMIT ?""";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setInt(2, limit);
            try (ResultSet rs = ps.executeQuery()) {
                List<Long> ids = new ArrayList<>();
                while (rs.next()) ids.add(rs.getLong(1));
                return ids;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<Long> findEventIdsByTag(String tagName, int limit, int offset) {
        // DISTINCT → nema GROUP BY; sortiramo po e.created_at
        String sql = """
            SELECT DISTINCT e.id
            FROM events e
            JOIN event_tags et ON et.event_id = e.id
            JOIN tags t ON t.id = et.tag_id
            WHERE t.name = ?
            ORDER BY e.created_at DESC
            LIMIT ? OFFSET ?""";
        try (Connection c = Db.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, tagName.trim().toLowerCase());
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (ResultSet rs = ps.executeQuery()) {
                List<Long> ids = new ArrayList<>();
                while (rs.next()) ids.add(rs.getLong(1));
                return ids;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
