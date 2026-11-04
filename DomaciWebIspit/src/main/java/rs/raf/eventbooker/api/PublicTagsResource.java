package rs.raf.eventbooker.api;

import rs.raf.eventbooker.config.DIContainer;
import rs.raf.eventbooker.model.Event;
import rs.raf.eventbooker.model.Tag;
import rs.raf.eventbooker.repository.EventRepository;
import rs.raf.eventbooker.repository.TagRepository;
import rs.raf.eventbooker.repository.impl.EventRepositoryJdbcImpl;
import rs.raf.eventbooker.repository.impl.TagRepositoryJdbcImpl;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/public/tags")
@Produces(MediaType.APPLICATION_JSON)
public class PublicTagsResource {

    private final TagRepository   tags   = DIContainer.tags();
    private final EventRepository events = DIContainer.events();


    private static Map<String,Object> eventDto(Event e) {
        return Map.of(
                "id",          e.getId(),
                "title",       e.getTitle(),
                "description", e.getDescription(),
                "createdAt",   e.getCreatedAt(),
                "scheduledAt", e.getScheduledAt(),
                "location",    e.getLocation(),
                "views",       e.getViews(),
                "authorId",    e.getAuthorId(),
                "categoryId",  e.getCategoryId(),
                "maxCapacity", e.getMaxCapacity()
        );
    }
    private static Map<String,Object> tagDto(Tag t) {
        return Map.of("id", t.getId(), "name", t.getName());
    }


    @GET @Path("/events/{eventId}")
    public Response tagsForEvent(@PathParam("eventId") long eventId) {
        events.findById(eventId).orElseThrow(NotFoundException::new); // 404
        List<Map<String,Object>> out = tags.findForEvent(eventId).stream()
                .map(PublicTagsResource::tagDto)
                .collect(Collectors.toList());
        return Response.ok(out).build();
    }


    @GET @Path("/{name}/events")
    public Response eventsByTag(@PathParam("name") String name,
                                @QueryParam("page")  @DefaultValue("1")  int page,
                                @QueryParam("limit") @DefaultValue("10") int limit) {
        if (name == null || name.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error","tag_required")).build();
        }
        int p = Math.max(1, page);
        int l = Math.max(1, limit);
        int offset = (p - 1) * l;


        List<Event> list = events.findAllByTag(name.trim().toLowerCase(), l, offset);
        var out = list.stream().map(PublicTagsResource::eventDto).collect(Collectors.toList());
        return Response.ok(out).build();
    }
}
