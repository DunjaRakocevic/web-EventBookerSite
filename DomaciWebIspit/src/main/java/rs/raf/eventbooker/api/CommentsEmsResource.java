package rs.raf.eventbooker.api;

import rs.raf.eventbooker.config.DIContainer;
import rs.raf.eventbooker.model.UserType;
import rs.raf.eventbooker.repository.CommentRepository;
import rs.raf.eventbooker.repository.EventRepository;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/ems")
@Produces(MediaType.APPLICATION_JSON)
public class CommentsEmsResource {

    private final CommentRepository comments = DIContainer.comments();
    private final EventRepository   events   = DIContainer.events();


    @GET
    @Path("/events/{eventId}/comments")
    public Response listForEvent(@PathParam("eventId") long eventId,
                                 @QueryParam("page")  @DefaultValue("1")  int page,
                                 @QueryParam("limit") @DefaultValue("20") int limit,
                                 @javax.ws.rs.core.Context HttpServletRequest req) {

        var s = req.getSession(false);
        if (s == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "unauthorized")).build();
        }

        long uid = (long) s.getAttribute("uid");
        UserType role = (UserType) s.getAttribute("utype");

        var e = events.findById(eventId).orElseThrow(javax.ws.rs.NotFoundException::new);
        if (role != UserType.ADMIN && e.getAuthorId() != uid) {
            return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", "forbidden")).build();
        }

        final int p = Math.max(1, page);
        final int l = Math.max(1, limit);
        final int offset = (p - 1) * l;

        var items = comments.listForEvent(eventId, l, offset);
        int total = comments.countByEventId(eventId);

        var payload = new rs.raf.eventbooker.dto.PagedResponse<java.util.Map<String, Object>>();
        payload.setPage(p);
        payload.setLimit(l);
        payload.setTotal(total);
        payload.setTotalPages((int) Math.max(1, Math.ceil((double) total / Math.max(1, l))));
        payload.setItems(items);

        return Response.ok(payload).build();
    }


    @DELETE
    @Path("/comments/{id}")
    public Response delete(@PathParam("id") long id,
                           @javax.ws.rs.core.Context HttpServletRequest req) {

        var s = req.getSession(false);
        if (s == null) {
            return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error", "unauthorized")).build();
        }

        long uid = (long) s.getAttribute("uid");
        UserType role = (UserType) s.getAttribute("utype");

        long owner = comments.findEventAuthorIdForComment(id)
                .orElseThrow(javax.ws.rs.NotFoundException::new);

        if (role != UserType.ADMIN && owner != uid) {
            return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error", "forbidden")).build();
        }

        comments.delete(id);
        return Response.noContent().build(); // 204
    }
}
