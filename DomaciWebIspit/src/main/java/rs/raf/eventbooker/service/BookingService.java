package rs.raf.eventbooker.service;

import rs.raf.eventbooker.model.EventBooking;

import java.util.List;

public interface BookingService {
    EventBooking book(long eventId, String name, String email, int seats);
    List<EventBooking> list(long eventId, int limit, int offset);
}
