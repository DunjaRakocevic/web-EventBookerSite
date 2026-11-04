package rs.raf.eventbooker.repository.impl;

import rs.raf.eventbooker.db.Db;
import rs.raf.eventbooker.model.EventBooking;
import rs.raf.eventbooker.repository.EventBookingRepository;

import java.sql.*;
import java.util.*;

public class EventBookingRepositoryJdbcImpl implements EventBookingRepository {

    private EventBooking map(ResultSet rs) throws SQLException {
        return new EventBooking(
                rs.getLong("id"),
                rs.getLong("event_id"),
                rs.getString("visitor_name"),
                rs.getString("visitor_email"),
                rs.getInt("seats"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    @Override
    public EventBooking create(EventBooking b) {
        String sql = """
            INSERT INTO event_bookings(event_id, visitor_name, visitor_email, seats)
            VALUES (?,?,?,?)
            """;
        try (var c = Db.getConnection();
             var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, b.getEventId());
            ps.setString(2, b.getVisitorName());
            ps.setString(3, b.getVisitorEmail().toLowerCase());
            ps.setInt(4, b.getSeats());
            ps.executeUpdate();
            try (var k = ps.getGeneratedKeys()) {
                if (k.next()) b.setId(k.getLong(1));
            }
            // učitaj created_at
            try (var ps2 = c.prepareStatement("SELECT * FROM event_bookings WHERE id=?")) {
                ps2.setLong(1, b.getId());
                try (var rs = ps2.executeQuery()) {
                    if (rs.next()) return map(rs);
                }
            }
            return b;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public List<EventBooking> listByEvent(long eventId, int limit, int offset) {
        String sql = """
            SELECT * FROM event_bookings
            WHERE event_id=?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?
            """;
        try (var c = Db.getConnection();
             var ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (var rs = ps.executeQuery()) {
                var out = new ArrayList<EventBooking>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public int sumSeatsByEvent(long eventId) {
        String sql = "SELECT COALESCE(SUM(seats),0) AS s FROM event_bookings WHERE event_id=?";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean delete(long id) {
        try (var c = Db.getConnection();
             var ps = c.prepareStatement("DELETE FROM event_bookings WHERE id=?")) {
            ps.setLong(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
