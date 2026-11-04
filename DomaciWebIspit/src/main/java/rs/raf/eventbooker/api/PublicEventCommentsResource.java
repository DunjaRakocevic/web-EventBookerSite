package rs.raf.eventbooker.api;

import rs.raf.eventbooker.config.DIContainer;
import rs.raf.eventbooker.service.CommentService;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;


@Path("/public/events/{id}/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicEventCommentsResource {

    private final CommentService comments = DIContainer.commentService();

    public static class In { public String name; public String text; }

    @POST
    public Response create(@PathParam("id") long eventId, In in) {
        try {
            if (in == null) return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error","payload_required")).build();

            var created = comments.create(eventId, in.name, in.text);
            return Response.status(Response.Status.CREATED).entity(created).build();
        } catch (NotFoundException nf) {
            throw nf;
        } catch (IllegalArgumentException iae) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", iae.getMessage())).build();
        } catch (RuntimeException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error","server_error")).build();
        }
    }

    @GET
    public Response list(@PathParam("id") long eventId,
                         @QueryParam("page") @DefaultValue("1") int page,
                         @QueryParam("limit") @DefaultValue("10") int limit) {
        try {
            var payload = comments.list(eventId, page, limit);
            return Response.ok(payload).build();
        } catch (NotFoundException nf) { throw nf; }
        catch (RuntimeException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error","server_error")).build();
        }
    }
}
