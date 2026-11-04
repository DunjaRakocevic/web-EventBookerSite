package rs.raf.eventbooker.api;

import rs.raf.eventbooker.config.DIContainer;
import rs.raf.eventbooker.model.Event;
import rs.raf.eventbooker.repository.EventRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

@Path("/search")
@Produces(MediaType.APPLICATION_JSON)
public class SearchResource {

    private final EventRepository events = DIContainer.events();

    private static Map<String,Object> dto(Event e) {
        Map<String,Object> m = new LinkedHashMap<>();
        m.put("id",          e.getId());
        m.put("title",       e.getTitle());
        m.put("description", e.getDescription());
        m.put("createdAt",   e.getCreatedAt());
        m.put("scheduledAt", e.getScheduledAt());
        m.put("location",    e.getLocation());
        m.put("views",       e.getViews());
        m.put("authorId",    e.getAuthorId());
        m.put("categoryId",  e.getCategoryId());
        m.put("maxCapacity", e.getMaxCapacity());
        return m;
    }

    @GET
    public Response search(@QueryParam("q") String q,
                           @QueryParam("page")  @DefaultValue("1")  int page,
                           @QueryParam("limit") @DefaultValue("10") int limit) {
        try {
            if (q == null || q.isBlank()) {
                return Response.status(Response.Status.BAD_REQUEST)
                        .entity(Map.of("error", "q_required")).build();
            }
            int p = Math.max(1, page);
            int l = Math.max(1, limit);
            int offset = (p - 1) * l;

            List<Event> items = events.findAllSearch(q.trim(), l, offset);
            int total = events.countSearch(q.trim());

            List<Map<String,Object>> data = items.stream()
                    .map(SearchResource::dto)
                    .collect(Collectors.toList());

            Map<String,Object> payload = new LinkedHashMap<>();
            payload.put("page", p);
            payload.put("limit", l);
            payload.put("total", total);
            payload.put("hasNext", (long)offset + data.size() < total);
            payload.put("items", data);

            return Response.ok(payload).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", ex.getMessage())).build();
        } catch (RuntimeException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity(Map.of("error", "server_error")).build();
        }
    }
}
