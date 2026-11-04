package rs.raf.eventbooker.service.impl;


import rs.raf.eventbooker.model.EventBooking;
import rs.raf.eventbooker.repository.EventBookingRepository;
import rs.raf.eventbooker.repository.EventRepository;
import rs.raf.eventbooker.service.BookingService;

import java.util.List;

public class BookingServiceImpl implements BookingService {

    private final EventRepository events;
    private final EventBookingRepository bookings;

    public BookingServiceImpl(EventRepository events, EventBookingRepository bookings) {
        this.events = events; this.bookings = bookings;
    }

    @Override
    public EventBooking book(long eventId, String name, String email, int seats) {

        if (name == null || name.trim().isEmpty())
            throw new IllegalArgumentException("name_required");
        if (email == null || email.trim().isEmpty() || !email.contains("@"))
            throw new IllegalArgumentException("email_invalid");
        if (seats <= 0)
            throw new IllegalArgumentException("seats_must_be_positive");


        var ev = events.findById(eventId).orElseThrow(() -> new javax.ws.rs.NotFoundException("event_not_found"));


        Integer maxCap = ev.getMaxCapacity(); // null = neograničeno
        if (maxCap != null) {
            int already = bookings.sumSeatsByEvent(eventId);
            if ((long) already + seats > maxCap) {
                throw new IllegalStateException("capacity_exceeded");
            }
        }


        var eb = new EventBooking();
        eb.setEventId(eventId);
        eb.setVisitorName(name.trim());
        eb.setVisitorEmail(email.trim().toLowerCase());
        eb.setSeats(seats);

        try {
            return bookings.create(eb);
        } catch (RuntimeException e) {
            String msg = (e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            if (msg != null && msg.toLowerCase().contains("duplicate")) {
                throw new IllegalStateException("already_booked");
            }
            throw e;
        }
    }



    @Override
    public List<EventBooking> list(long eventId, int limit, int offset) {
        events.findById(eventId).orElseThrow(() -> new IllegalArgumentException("event_not_found"));
        return bookings.listByEvent(eventId, limit, offset);
    }
}
