package rs.raf.eventbooker.api;

import rs.raf.eventbooker.config.DIContainer;
import rs.raf.eventbooker.service.CommentService;
import rs.raf.eventbooker.util.VisitorUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Map;

@Path("/public/comments")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PublicCommentsResource {

    private final CommentService comments = DIContainer.commentService();

    public static class ReactIn { public Integer reaction; } // 1 ili -1

    @POST
    @Path("/{id}/react")
    public Response react(@PathParam("id") long id,
                          ReactIn in,
                          @Context HttpServletRequest req,
                          @Context HttpServletResponse resp) {
        if (in == null || in.reaction == null) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "reaction_required")).build();
        }

        String vkey = VisitorUtil.getOrSet(req, resp);
        try {
            comments.react(id, vkey, in.reaction);
            return Response.ok(comments.reactionCounts(id)).build();
        } catch (IllegalArgumentException iae) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", iae.getMessage())).build();
        } catch (NotFoundException nf) {
            throw nf; // 404
        } catch (RuntimeException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "server_error")).build();
        }
    }

    @GET
    @Path("/{id}/reactions")
    public Response counts(@PathParam("id") long id) {
        try {
            return Response.ok(comments.reactionCounts(id)).build();
        } catch (NotFoundException nf) {
            throw nf; // 404
        } catch (RuntimeException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "server_error")).build();
        }
    }
}
