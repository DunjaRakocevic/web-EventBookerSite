package rs.raf.eventbooker.api;

import rs.raf.eventbooker.config.DIContainer;
import rs.raf.eventbooker.model.Category;
import rs.raf.eventbooker.repository.CategoryRepository;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.util.Map;
import java.util.stream.Collectors;

@Path("/public/categories")
@Produces(MediaType.APPLICATION_JSON)
public class PublicCategoriesResource {

    private final CategoryRepository cats = DIContainer.categories();

    private static Map<String,Object> dto(Category c) {
        return Map.of(
                "id", c.getId(),
                "name", c.getName(),
                "description", c.getDescription()
        );
    }

    @GET
    public Response list(@QueryParam("page")  @DefaultValue("1")  int page,
                         @QueryParam("limit") @DefaultValue("50") int limit) {
        int p = Math.max(1, page);
        int l = Math.max(1, limit);
        var out = cats.list(p, l).stream().map(PublicCategoriesResource::dto).collect(Collectors.toList());
        return Response.ok(out).build();
    }

    @GET @Path("/{id}")
    public Response one(@PathParam("id") long id) {
        var c = cats.findById(id).orElseThrow(NotFoundException::new);
        return Response.ok(dto(c)).build();
    }
}
