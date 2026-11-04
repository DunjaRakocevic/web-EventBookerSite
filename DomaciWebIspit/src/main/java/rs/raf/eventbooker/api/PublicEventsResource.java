package rs.raf.eventbooker.api;

import rs.raf.eventbooker.config.DIContainer;
import rs.raf.eventbooker.dto.PagedResponse;
import rs.raf.eventbooker.model.Event;
import rs.raf.eventbooker.repository.EventRepository;
import rs.raf.eventbooker.repository.TagRepository;
import rs.raf.eventbooker.repository.impl.EventBookingRepositoryJdbcImpl;
import rs.raf.eventbooker.service.BookingService;
import rs.raf.eventbooker.util.VisitorUtil;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.servlet.http.*;
import java.util.*;
import java.util.stream.Collectors;

@Path("/public/events")
@Produces(MediaType.APPLICATION_JSON)
public class PublicEventsResource {

    private final EventRepository events = DIContainer.events();
    private final TagRepository   tags   = DIContainer.tags();

    private static Map<String,Object> dto(Event e, EventRepository eventsRepo) {
        Map<String,Integer> rc = eventsRepo.reactionCounts(e.getId());
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
        m.put("likes", rc.getOrDefault("likes", 0));
        m.put("dislikes", rc.getOrDefault("dislikes", 0));


        var uOpt = DIContainer.users().findById(e.getAuthorId());

        uOpt.ifPresent(u -> {
            m.put("authorFirstName", u.getFirstName());
            m.put("authorLastName",  u.getLastName());
            m.put("authorName", (u.getFirstName() + " " + u.getLastName()).trim());
        });

        return m;

    }

    private Map<String,Object> dto(Event e) {
        return dto(e, events);
    }


    @GET @Path("/{id}")
    public Response getOne(@PathParam("id") long id,
                           @Context HttpServletRequest req,
                           @Context HttpServletResponse resp) {
        var e = events.findById(id).orElse(null);
        if (e == null) return Response.status(Response.Status.NOT_FOUND)
                .entity(Map.of("error","not_found")).build();

        String vkey = rs.raf.eventbooker.util.VisitorUtil.getOrSet(req, resp);
        try {
            int inserted = events.recordViewOnce(id, vkey);
            if (inserted == 1) {
                e.setViews(e.getViews() + 1);
            }
        } catch (RuntimeException ignore) { }

        return Response.ok(dto(e)).build();
    }


    @GET @Path("/most-visited")
    public Response mostVisited(@QueryParam("days") @DefaultValue("30") int days) {
        try {
            final int l = 10;
            var ids = events.mostVisitedLastDaysIds(Math.max(1, days), l);

            var list = ids.stream()
                    .map(events::findById).filter(java.util.Optional::isPresent).map(java.util.Optional::get)
                    .map(this::dto)
                    .collect(java.util.stream.Collectors.toList());

            return Response.ok(list).build();
        } catch (RuntimeException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(java.util.Map.of("error", "server_error")).build();
        }
    }





    public static class ReactIn { public int reaction; } // 1 ili -1

    @POST @Path("/{id}/react")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response react(@PathParam("id") long id,
                          ReactIn in,
                          @Context HttpServletRequest req,
                          @Context HttpServletResponse resp) {
        if (in == null || (in.reaction != 1 && in.reaction != -1)) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error","reaction_invalid")).build();
        }
        events.findById(id).orElseThrow(NotFoundException::new);

        var vid = VisitorUtil.getOrSet(req, resp);
        events.upsertReaction(id, vid, in.reaction);

        var counts = events.reactionCounts(id);
        return Response.ok(counts).build();
    }

    @GET @Path("/{id}/reactions")
    public Response counts(@PathParam("id") long id) {
        events.findById(id).orElseThrow(NotFoundException::new);
        return Response.ok(events.reactionCounts(id)).build();
    }

    @GET @Path("/most-reacted")
    public Response mostReacted(@QueryParam("limit") @DefaultValue("5") int limit) {
        var ids = events.listMostReactedIds(limit);
        var list = ids.stream()
                .map(events::findById).filter(Optional::isPresent).map(Optional::get)
                .map(this::dto)
                .collect(Collectors.toList());
        return Response.ok(list).build();
    }

    @GET @Path("/{id}/related")
    public Response related(@PathParam("id") long id,
                            @QueryParam("limit") @DefaultValue("5") int limit) {
        events.findById(id).orElseThrow(NotFoundException::new);
        var ids = tags.findRelatedEventIds(id, limit);
        var list = ids.stream()
                .map(events::findById).filter(Optional::isPresent).map(Optional::get)
                .map(this::dto)
                .collect(Collectors.toList());
        return Response.ok(list).build();
    }


    public static class BookingIn { public String name; public String email; public Integer seats; }

    private final BookingService bookingService = DIContainer.bookingService();

    @POST @Path("/{id}/bookings")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response book(@PathParam("id") long id, BookingIn in) {
        try {
            if (in == null)
                return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error","payload_required")).build();

            var b = bookingService.book(id, in.name, in.email, in.seats == null ? 0 : in.seats);

            var e = events.findById(id).orElseThrow();
            Integer remaining = (e.getMaxCapacity()==null) ? null
                    : (e.getMaxCapacity() - new EventBookingRepositoryJdbcImpl().sumSeatsByEvent(id));

            return Response.status(Response.Status.CREATED).entity(Map.of(
                    "id", b.getId(), "eventId", b.getEventId(), "name", b.getVisitorName(),
                    "email", b.getVisitorEmail(), "seats", b.getSeats(), "remaining", remaining
            )).build();

        } catch (NotFoundException nf) {
            return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error","event_not_found")).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", ex.getMessage())).build();
        } catch (IllegalStateException ex) {
            return Response.status(Response.Status.CONFLICT).entity(Map.of("error", ex.getMessage())).build();
        } catch (RuntimeException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error","server_error")).build();
        }
    }


    @GET
    public Response list(@QueryParam("tag") String tag,
                         @QueryParam("categoryId") Long categoryId,
                         @QueryParam("page")  @DefaultValue("1")  int page,
                         @QueryParam("limit") @DefaultValue("10") int limit) {
        try {
            final int p = Math.max(1, page);
            final int l = Math.max(1, limit);
            final int offset = (p - 1) * l;

            List<Event> items;
            int total;

            if (tag != null && !tag.isBlank()) {
                final String t = tag.trim();
                items = events.findAllByTag(t, l, offset);
                total = events.countByTag(t);
            } else if (categoryId != null) {
                items = events.findAllByCategory(categoryId, l, offset);
                total = events.countByCategory(categoryId);
            } else {
                items = events.findAll(l, offset);
                total = events.countAll();
            }

            final java.util.List<java.util.Map<String,Object>> data = items.stream()
                    .map(this::dto)
                    .collect(java.util.stream.Collectors.toList());

            PagedResponse<java.util.Map<String,Object>> payload =
                    new PagedResponse<>();
            payload.setPage(p);
            payload.setLimit(l);
            payload.setTotal(total);
            payload.setTotalPages((int) Math.max(1, Math.ceil((double) total / Math.max(1, l))));
            payload.setItems(data);

            return Response.ok(payload).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(java.util.Map.of("error", ex.getMessage())).build();
        } catch (RuntimeException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(java.util.Map.of("error","server_error")).build();
        }
    }



    @GET @Path("/{id}/bookings/count")
    public Response bookingCount(@PathParam("id") long id) {
        events.findById(id).orElseThrow(NotFoundException::new);
        int seats = new rs.raf.eventbooker.repository.impl.EventBookingRepositoryJdbcImpl()
                .sumSeatsByEvent(id);
        return Response.ok(java.util.Map.of("count", seats)).build();
    }




}
