package rs.raf.eventbooker.repository;

import rs.raf.eventbooker.model.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {
    List<Booking> listForEvent(long eventId, int limit, int offset);
    int countForEvent(long eventId);
    boolean delete(long bookingId);
    Optional<Booking> findById(long bookingId);
}
