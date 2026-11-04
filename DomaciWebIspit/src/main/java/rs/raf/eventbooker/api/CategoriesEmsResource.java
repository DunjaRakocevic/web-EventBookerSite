package rs.raf.eventbooker.api;

import rs.raf.eventbooker.config.DIContainer;
import rs.raf.eventbooker.dto.PagedResponse;
import rs.raf.eventbooker.model.Category;
import rs.raf.eventbooker.repository.EventRepository;
import rs.raf.eventbooker.service.CategoryService;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.*;
import java.util.stream.Collectors;

@Path("/ems/categories")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class CategoriesEmsResource {

    private final CategoryService categoryService = DIContainer.categoryService();
    private final EventRepository  eventRepo      = DIContainer.events();

    @GET
    public Response list(@QueryParam("page")  @DefaultValue("1")  int page,
                         @QueryParam("limit") @DefaultValue("10") int limit,
                         @QueryParam("q")     @DefaultValue("")   String q) {

        PagedResponse<Category> paged = categoryService.listPaged(q, page, limit);


        List<Map<String,Object>> items = paged.getItems().stream().map(c -> {
            int cnt = eventRepo.countByCategory(c.getId());
            Map<String,Object> m = new LinkedHashMap<>();
            m.put("id", c.getId());
            m.put("name", c.getName());
            m.put("description", c.getDescription());
            m.put("eventsCount", cnt);
            return m;
        }).collect(Collectors.toList());

        PagedResponse<Map<String,Object>> out =
                new PagedResponse<>(items, paged.getPage(), paged.getLimit(), paged.getTotal());
        return Response.ok(out).build();
    }

    @POST
    public Response create(Map<String, Object> body) {
        String name = (String) body.get("name");
        String desc = (String) body.get("description");
        Category created = categoryService.create(name, desc);

        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("created", Map.of(
                "id", created.getId(),
                "name", created.getName(),
                "description", created.getDescription(),
                "eventsCount", 0
        ));
        return Response.status(Response.Status.CREATED).entity(payload).build();
    }

    @PUT @Path("/{id}")
    public Response update(@PathParam("id") Long id, Map<String, Object> body) {
        String name = (String) body.get("name");
        String desc = (String) body.get("description");
        Category updated = categoryService.update(id, name, desc);

        int cnt = eventRepo.countByCategory(updated.getId());
        return Response.ok(Map.of("updated", Map.of(
                "id", updated.getId(),
                "name", updated.getName(),
                "description", updated.getDescription(),
                "eventsCount", cnt
        ))).build();
    }

    @DELETE @Path("/{id}")
    public Response delete(@PathParam("id") Long id) {
        boolean ok = categoryService.delete(id);
        return Response.ok(Map.of("deleted", ok)).build();
    }
}
