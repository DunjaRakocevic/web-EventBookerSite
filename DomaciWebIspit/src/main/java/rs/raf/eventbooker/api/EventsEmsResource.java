package rs.raf.eventbooker.api;

import rs.raf.eventbooker.config.DIContainer;
import rs.raf.eventbooker.model.Event;
import rs.raf.eventbooker.model.UserType;
import rs.raf.eventbooker.repository.EventRepository;
import rs.raf.eventbooker.service.BookingService;
import rs.raf.eventbooker.service.EventService;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.servlet.http.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Path("/ems/events")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class EventsEmsResource {

    private final EventService service = DIContainer.eventService();
    private final EventRepository repo = DIContainer.events();
    private final BookingService bookingService = DIContainer.bookingService();

    static class EventIn {
        public String title;
        public String description;
        public String scheduledAt;
        public String location;
        public Long   categoryId;
        public Integer maxCapacity;
        public List<String> tags;
    }

    private static LocalDateTime parseIso(String s) { return LocalDateTime.parse(s); }

    private static Map<String,Object> dto(Event e) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id", e.getId());
        m.put("title", e.getTitle());
        m.put("description", e.getDescription());
        m.put("createdAt", e.getCreatedAt());
        m.put("scheduledAt", e.getScheduledAt());
        m.put("location", e.getLocation());
        m.put("views", e.getViews());
        m.put("authorId", e.getAuthorId());
        m.put("categoryId", e.getCategoryId());
        m.put("maxCapacity", e.getMaxCapacity());

        DIContainer.users().findById(e.getAuthorId()).ifPresent(u -> {
            m.put("authorFirstName", u.getFirstName());
            m.put("authorLastName",  u.getLastName());
            m.put("authorName",      (u.getFirstName() + " " + u.getLastName()).trim());
        });

        return m;
    }

    @GET
    public Response list(@Context HttpServletRequest req,
                         @QueryParam("page")  @DefaultValue("1")  int page,
                         @QueryParam("limit") @DefaultValue("10") int limit,
                         @QueryParam("q")     String q) {

        final int p = Math.max(1, page);
        final int l = Math.max(1, limit);
        final int offset = (p - 1) * l;

        HttpSession s = req.getSession(false);
        long uid = (long) s.getAttribute("uid");
        UserType role = (UserType) s.getAttribute("utype");

        final boolean hasQ = (q != null && !q.isBlank());

        List<Event> items;
        int total;

        if (hasQ) {
            final String qq = q.trim();
            if (role == UserType.ADMIN) {
                items = service.searchForAdmin(qq, l, offset);
                total = repo.countSearch(qq);
            } else {
                items = service.searchForAuthor(uid, qq, l, offset);
                total = repo.countByAuthorSearch(uid, qq);
            }
        } else {
            if (role == UserType.ADMIN) {
                items = service.listForAdmin(l, offset);
                total = repo.countAll();
            } else {
                items = service.listForAuthor(uid, l, offset);
                total = repo.countByAuthor(uid);
            }
        }

        List<Map<String,Object>> data = items.stream()
                .map(EventsEmsResource::dto)
                .collect(Collectors.toList());

        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("page", p);
        payload.put("limit", l);
        payload.put("total", total);
        payload.put("hasNext", (long) offset + data.size() < total);
        payload.put("items", data);

        return Response.ok(payload).build();
    }


    @GET @Path("/{id}")
    public Response get(@PathParam("id") long id) {
        Event e = repo.findById(id).orElseThrow(NotFoundException::new);
        Map<String,Object> m = dto(e);
        var tagNames = DIContainer.tags().findForEvent(id)
                .stream().map(rs.raf.eventbooker.model.Tag::getName)
                .collect(java.util.stream.Collectors.toList());
        m.put("tags", tagNames);
        return Response.ok(m).build();
    }


    @POST
    public Response create(@Context HttpServletRequest req, EventIn in) {
        try {
            HttpSession s = req.getSession(false);
            long uid = (long) s.getAttribute("uid");
            Event e = service.create(uid, in.title, in.description,
                    parseIso(in.scheduledAt), in.location, in.categoryId, in.maxCapacity, in.tags);
            return Response.status(Response.Status.CREATED).entity(dto(e)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", ex.getMessage())).build();
        } catch (RuntimeException ex) {
            return Response.status(Response.Status.CONFLICT).entity(Map.of("error", ex.getMessage())).build();
        }
    }

    @PUT @Path("/{id}")
    public Response update(@Context HttpServletRequest req, @PathParam("id") long id, EventIn in) {
        try {
            HttpSession s = req.getSession(false);
            long uid = (long) s.getAttribute("uid");
            UserType role = (UserType) s.getAttribute("utype");
            boolean isAdmin = role == UserType.ADMIN;

            Event e = service.update(uid, isAdmin, id, in.title, in.description,
                    parseIso(in.scheduledAt), in.location, in.categoryId, in.maxCapacity, in.tags);
            return Response.ok(dto(e)).build();
        } catch (IllegalStateException ex) {
            return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", ex.getMessage())).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", ex.getMessage())).build();
        } catch (RuntimeException ex) {
            return Response.status(Response.Status.CONFLICT).entity(Map.of("error", ex.getMessage())).build();
        }
    }

    @DELETE @Path("/{id}")
    public Response delete(@Context HttpServletRequest req, @PathParam("id") long id) {
        try {
            HttpSession s = req.getSession(false);
            long uid = (long) s.getAttribute("uid");
            UserType role = (UserType) s.getAttribute("utype");
            boolean isAdmin = role == UserType.ADMIN;

            boolean ok = service.delete(uid, isAdmin, id);
            return Response.ok(Map.of("deleted", ok)).build();
        } catch (IllegalStateException ex) {
            return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", ex.getMessage())).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", ex.getMessage())).build();
        }
    }

    private static Map<String,Object> bookingDto(rs.raf.eventbooker.model.EventBooking b) {
        return Map.of(
                "id", b.getId(),
                "eventId", b.getEventId(),
                "name", b.getVisitorName(),
                "email", b.getVisitorEmail(),
                "seats", b.getSeats(),
                "createdAt", b.getCreatedAt()
        );
    }

    @GET @Path("/{id}/bookings")
    public Response listBookings(@Context HttpServletRequest req,
                                 @PathParam("id") long id,
                                 @QueryParam("page")  @DefaultValue("1")  int page,
                                 @QueryParam("limit") @DefaultValue("20") int limit) {
        try {
            HttpSession s = req.getSession(false);
            long uid = (long) s.getAttribute("uid");
            var role = (rs.raf.eventbooker.model.UserType) s.getAttribute("utype");
            var ev = new rs.raf.eventbooker.repository.impl.EventRepositoryJdbcImpl()
                    .findById(id).orElseThrow(NotFoundException::new);

            boolean isAdmin = role == rs.raf.eventbooker.model.UserType.ADMIN;
            if (!isAdmin && ev.getAuthorId() != uid) return Response.status(Response.Status.FORBIDDEN)
                    .entity(Map.of("error","forbidden")).build();

            int offset = Math.max(0, (Math.max(1,page)-1) * Math.max(1,limit));
            var list = bookingService.list(id, Math.max(1,limit), offset)
                    .stream().map(EventsEmsResource::bookingDto)
                    .collect(java.util.stream.Collectors.toList());
            return Response.ok(list).build();
        } catch (NotFoundException nf) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error","not_found")).build();
        } catch (RuntimeException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error","server_error")).build();
        }
    }

}
