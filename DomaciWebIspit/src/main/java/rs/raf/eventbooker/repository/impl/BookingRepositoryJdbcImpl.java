package rs.raf.eventbooker.repository.impl;

import rs.raf.eventbooker.db.Db;
import rs.raf.eventbooker.model.Booking;
import rs.raf.eventbooker.repository.BookingRepository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class BookingRepositoryJdbcImpl implements BookingRepository {

    private Booking map(ResultSet rs) throws SQLException {
        return new Booking(
                rs.getLong("id"),
                rs.getLong("event_id"),
                rs.getString("visitor_name"),
                rs.getString("visitor_email"),
                rs.getInt("seats"),
                rs.getTimestamp("created_at").toLocalDateTime()
        );
    }

    @Override
    public List<Booking> listForEvent(long eventId, int limit, int offset) {
        String sql = """
            SELECT id, event_id, visitor_name, visitor_email, seats, created_at
            FROM event_bookings
            WHERE event_id=?
            ORDER BY created_at DESC
            LIMIT ? OFFSET ?""";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            ps.setInt(2, limit);
            ps.setInt(3, offset);
            try (var rs = ps.executeQuery()) {
                List<Booking> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }


    @Override
    public int countForEvent(long eventId) {
        String sql = "SELECT COUNT(*) FROM event_bookings WHERE event_id=?";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, eventId);
            try (var rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public boolean delete(long bookingId) {
        String sql = "DELETE FROM event_bookings WHERE id=?";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    @Override
    public Optional<Booking> findById(long bookingId) {
        String sql = "SELECT id, event_id, visitor_name, visitor_email, seats, created_at FROM event_bookings WHERE id=?";
        try (var c = Db.getConnection(); var ps = c.prepareStatement(sql)) {
            ps.setLong(1, bookingId);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? Optional.of(map(rs)) : Optional.empty();
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}
