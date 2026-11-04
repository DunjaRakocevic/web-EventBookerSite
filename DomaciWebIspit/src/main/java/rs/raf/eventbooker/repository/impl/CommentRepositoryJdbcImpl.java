package rs.raf.eventbooker.repository.impl;

import rs.raf.eventbooker.db.Db;
import rs.raf.eventbooker.model.Comment;
import rs.raf.eventbooker.repository.CommentRepository;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class CommentRepositoryJdbcImpl implements CommentRepository {

    @Override
    public Map<String,Object> create(long eventId, String name, String text) {
        String sql = """
        INSERT INTO comments(event_id, author_name, `text`, created_at)
        VALUES (?,?,?, NOW())
    """;
        try (var c = Db.getConnection();
             var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setLong(1, eventId);
            ps.setString(2, name);
            ps.setString(3, text);
            ps.executeUpdate();

            long id;
            try (var rs = ps.getGeneratedKeys()) {
                rs.next();
                id = rs.getLong(1);
            }

            // pročitaj tačan timestamp iz baze
            LocalDateTime createdAt;
            try (var ps2 = c.prepareStatement("SELECT created_at FROM comments WHERE id=?")) {
                ps2.setLong(1, id);
                try (var rs2 = ps2.executeQuery()) {
                    rs2.next();
                    createdAt = rs2.getTimestamp(1).toLocalDateTime();
                }
            }

            return Map.of(
                    "id",        id,
                    "eventId",   eventId,
                    "authorName", name,
                    "name",       name,          // kompatibilnost
                    "text",      text,
                    "createdAt", createdAt,
                    "likes",     0,
                    "dislikes",  0
            );
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public List<Map<String,Object>> listForEvent(long eventId, int limit, int offset) {
        String sql = """
        SELECT c.id, c.event_id, c.author_name, c.`text`, c.created_at,
               IFNULL(l.likes,0)    AS likes,
               IFNULL(d.dislikes,0) AS dislikes
        FROM comments c
        LEFT JOIN (
            SELECT comment_id, COUNT(*) AS likes
            FROM comment_reactions
            WHERE value = 1
            GROUP BY comment_id
        ) l ON l.comment_id = c.id
        LEFT JOIN (
            SELECT comment_id, COUNT(*) AS dislikes
            FROM comment_reactions
            WHERE value = -1
            GROUP BY comment_id
        ) d ON d.comment_id = c.id
        WHERE c.event_id = ?
        ORDER BY c.created_at DESC
        LIMIT ? OFFSET ?
    """;
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setInt(2, Math.max(1, limit));
            ps.setInt(3, Math.max(0, offset));
            try (var rs = ps.executeQuery()) {
                var out = new ArrayList<Map<String,Object>>();
                while (rs.next()) {
                    var name = rs.getString("author_name");
                    var m = new LinkedHashMap<String,Object>();
                    m.put("id",        rs.getLong("id"));
                    m.put("eventId",   rs.getLong("event_id"));
                    m.put("authorName", name);
                    m.put("name",       name); // zadržavam i "name" za kompatibilnost fronta
                    m.put("text",      rs.getString("text"));
                    m.put("createdAt", rs.getTimestamp("created_at").toLocalDateTime());
                    m.put("likes",     rs.getInt("likes"));
                    m.put("dislikes",  rs.getInt("dislikes"));
                    out.add(m);
                }
                return out;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Optional<Comment> findById(long id) {
        String sql = "SELECT id, event_id, author_name, text, created_at FROM comments WHERE id=?";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    var cm = new Comment(
                            rs.getLong("id"),
                            rs.getLong("event_id"),
                            rs.getString("author_name"),
                            rs.getString("text"),
                            rs.getTimestamp("created_at").toLocalDateTime()
                    );
                    return Optional.of(cm);
                }
                return Optional.empty();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public void upsertReaction(long commentId, String visitorKey, int reaction) {
        String sql = """
      INSERT INTO comment_reactions(comment_id, visitor_key, value, created_at)
      VALUES (?,?,?,NOW())
      ON DUPLICATE KEY UPDATE value = VALUES(value), created_at = NOW()
    """;
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, commentId);
            ps.setString(2, visitorKey);
            ps.setInt(3, reaction);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public java.util.Map<String,Integer> reactionCounts(long commentId) {
        String sql = """
      SELECT
        SUM(CASE WHEN value= 1 THEN 1 ELSE 0 END) AS likes,
        SUM(CASE WHEN value=-1 THEN 1 ELSE 0 END) AS dislikes
      FROM comment_reactions WHERE comment_id=?
    """;
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, commentId);
            try (var rs = ps.executeQuery()) {
                int likes = 0, dislikes = 0;
                if (rs.next()) { likes = rs.getInt("likes"); dislikes = rs.getInt("dislikes"); }
                return java.util.Map.of("likes", likes, "dislikes", dislikes);
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }


    @Override
    public List<Long> mostReactedIdsForEvent(long eventId, int limit) {
        String sql = """
        SELECT c.id
        FROM comments c
        JOIN comment_reactions cr ON cr.comment_id = c.id
        WHERE c.event_id = ?
        GROUP BY c.id
        ORDER BY COUNT(*) DESC, MAX(cr.created_at) DESC
        LIMIT ?""";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setInt(2, limit);
            try (var rs = ps.executeQuery()) {
                var ids = new java.util.ArrayList<Long>();
                while (rs.next()) ids.add(rs.getLong(1));
                return ids;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }


    // rs/raf/eventbooker/repository/impl/CommentRepositoryJdbcImpl.java
    @Override
    public Optional<Long> findEventAuthorIdForComment(long commentId) {
        String sql = """
        SELECT e.author_id
        FROM comments c
        JOIN events e ON e.id = c.event_id
        WHERE c.id = ?
        """;
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, commentId);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(rs.getLong(1)) : Optional.empty();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean delete(long commentId) {
        try (var c = Db.getConnection();
             var ps = c.prepareStatement("DELETE FROM comments WHERE id=?")) {
            ps.setLong(1, commentId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }


    @Override
    public int countByEventId(long eventId) {
        final String sql = "SELECT COUNT(*) FROM comments WHERE event_id = ?";
        try (var c = rs.raf.eventbooker.db.Db.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (java.sql.SQLException e) {
            throw new RuntimeException(e);
        }
    }



}
