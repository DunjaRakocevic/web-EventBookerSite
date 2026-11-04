package rs.raf.eventbooker.api;

import rs.raf.eventbooker.config.DIContainer;
import rs.raf.eventbooker.model.UserType;
import rs.raf.eventbooker.model.Event;
import rs.raf.eventbooker.model.Booking;
import rs.raf.eventbooker.repository.EventRepository;
import rs.raf.eventbooker.repository.BookingRepository;
import rs.raf.eventbooker.repository.impl.EventRepositoryJdbcImpl;
import rs.raf.eventbooker.repository.impl.BookingRepositoryJdbcImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;
import java.util.stream.Collectors;

@Path("/ems/events/{eventId}/bookings")
@Produces(MediaType.APPLICATION_JSON)
public class EmsEventBookingsResource {

    private final EventRepository    events   = DIContainer.events();
    private final BookingRepository  bookings = DIContainer.bookings();



    private Event requireAccess(HttpServletRequest req, long eventId) {
        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("uid") == null)
            throw new WebApplicationException(Response.Status.UNAUTHORIZED);

        long uid = (long) s.getAttribute("uid");
        UserType role = (UserType) s.getAttribute("utype");

        Event e = events.findById(eventId).orElseThrow(NotFoundException::new);
        boolean isAdmin = role == UserType.ADMIN;
        if (!isAdmin && e.getAuthorId() != uid)
            throw new WebApplicationException(Response.Status.FORBIDDEN);
        return e;
    }

    private static Map<String,Object> dto(Booking b) {
        return Map.of(
                "id",        b.getId(),
                "eventId",   b.getEventId(),
                "name",      b.getName(),
                "email",     b.getEmail(),
                "seats",     b.getSeats(),
                "createdAt", b.getCreatedAt()
        );
    }



    @GET
    public Response list(@PathParam("eventId") long eventId,
                         @Context HttpServletRequest req,
                         @QueryParam("page")  @DefaultValue("1")  int page,
                         @QueryParam("limit") @DefaultValue("10") int limit) {
        try {
            requireAccess(req, eventId);
            final int p = Math.max(1, page);
            final int l = Math.max(1, limit);
            final int offset = (p - 1) * l;

            final java.util.List<rs.raf.eventbooker.model.Booking> list = bookings.listForEvent(eventId, l, offset);
            final java.util.List<java.util.Map<String,Object>> items = list.stream()
                    .map(rs.raf.eventbooker.api.EmsEventBookingsResource::dto)
                    .collect(java.util.stream.Collectors.toList());

            final int total = bookings.countForEvent(eventId);

            var payload = new rs.raf.eventbooker.dto.PagedResponse<java.util.Map<String,Object>>();
            payload.setPage(p);
            payload.setLimit(l);
            payload.setTotal(total);
            payload.setTotalPages((int) Math.max(1, Math.ceil((double) total / Math.max(1, l))));
            payload.setItems(items);

            return Response.ok(payload).build();
        } catch (WebApplicationException wae) {
            throw wae;
        } catch (RuntimeException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(java.util.Map.of("error","server_error")).build();
        }
    }
}
