package rs.raf.eventbooker.repository;

import rs.raf.eventbooker.model.EventBooking;
import java.util.*;

public interface EventBookingRepository {
    EventBooking create(EventBooking b);
    List<EventBooking> listByEvent(long eventId, int limit, int offset);
    int sumSeatsByEvent(long eventId);
    boolean delete(long id);
}
