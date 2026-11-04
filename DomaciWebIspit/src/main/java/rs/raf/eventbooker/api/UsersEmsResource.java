package rs.raf.eventbooker.api;

import rs.raf.eventbooker.config.DIContainer;
import rs.raf.eventbooker.model.User;
import rs.raf.eventbooker.model.UserType;
import rs.raf.eventbooker.repository.impl.UserRepositoryJdbcImpl;
import rs.raf.eventbooker.service.UserService;
import rs.raf.eventbooker.service.impl.UserServiceImpl;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Path("/ems/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UsersEmsResource {

    private final UserService service = DIContainer.userService();

    public static class CreateIn {
        public String email;
        public String firstName;
        public String lastName;
        public String password;
        public String type;
        public Boolean active;
    }
    public static class UpdateIn {
        public String email;
        public String firstName;
        public String lastName;
        public String type;
    }

    public static class PasswordIn { public String password; }

    private static Map<String,Object> dto(User u) {
        return Map.of(
                "id", u.getId(),
                "email", u.getEmail(),
                "firstName", u.getFirstName(),
                "lastName", u.getLastName(),
                "active", u.isActive(),
                "type", u.getType()
        );
    }


    private boolean ensureAdmin(HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return false;
        Object t = s.getAttribute("utype");
        return (t instanceof UserType) && t == UserType.ADMIN;
    }

    private Response unauthorized() {
        return Response.status(Response.Status.UNAUTHORIZED).entity(Map.of("error","unauthorized")).build();
    }
    private Response forbidden() {
        return Response.status(Response.Status.FORBIDDEN).entity(Map.of("error","forbidden")).build();
    }



    @GET
    public Response list(@Context HttpServletRequest req,
                         @QueryParam("q") String q,
                         @QueryParam("page") @DefaultValue("1") int page,
                         @QueryParam("limit") @DefaultValue("10") int limit) {
        if (!ensureAdmin(req)) return unauthorized();
        int offset = Math.max(0, (page - 1) * limit);
        List<User> data = service.list(q, limit, offset);
        long total = service.count(q);
        List<Map<String,Object>> items = data.stream().map(UsersEmsResource::dto).collect(Collectors.toList());
        Map<String,Object> payload = new LinkedHashMap<>();
        payload.put("items", items);
        payload.put("page", page);
        payload.put("limit", limit);
        payload.put("total", total);
        payload.put("totalPages", (int)Math.max(1, Math.ceil((double)total/Math.max(1,limit))));
        return Response.ok(payload).build();
    }



    @POST
    public Response create(@Context HttpServletRequest req, CreateIn in) {
        if (!ensureAdmin(req)) return unauthorized();
        try {
            UserType t = (in.type == null || in.type.isBlank()) ? UserType.EVENT_CREATOR
                    : UserType.valueOf(in.type.trim().toUpperCase());
            boolean act = in.active == null ? true : in.active;
            User u = service.create(in.email, in.firstName, in.lastName, in.password, t, act);
            return Response.status(Response.Status.CREATED).entity(dto(u)).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", ex.getMessage())).build();
        } catch (IllegalStateException ex) {
            return Response.status(Response.Status.CONFLICT).entity(Map.of("error", ex.getMessage())).build();
        }
    }

    @PUT @Path("/{id}")
    public Response update(@Context HttpServletRequest req, @PathParam("id") long id, UpdateIn in) {
        if (!ensureAdmin(req)) return unauthorized();
        try {
            UserType type = null;
            if (in.type != null && !in.type.isBlank()) type = UserType.valueOf(in.type.trim().toUpperCase());
            boolean ok = service.update(id, in.email, in.firstName, in.lastName, type);
            return ok ? Response.ok(Map.of("updated", true)).build()
                    : Response.status(Response.Status.NOT_FOUND).entity(Map.of("error","not_found")).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", ex.getMessage())).build();
        } catch (RuntimeException ex) {
            String m = String.valueOf(ex.getMessage());
            if (m.contains("email_taken")) return Response.status(Response.Status.CONFLICT).entity(Map.of("error","email_taken")).build();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error","server_error")).build();
        }
    }


    @PUT @Path("/{id}/activate")
    public Response activate(@Context HttpServletRequest req, @PathParam("id") long id) {
        if (!ensureAdmin(req)) return unauthorized();
        boolean ok = service.setActive(id, true);
        return ok ? Response.ok(Map.of("updated", true)).build()
                : Response.status(Response.Status.NOT_FOUND).entity(Map.of("error","not_found")).build();
    }

    @PUT @Path("/{id}/deactivate")
    public Response deactivate(@Context HttpServletRequest req, @PathParam("id") long id) {
        if (!ensureAdmin(req)) return unauthorized();
        boolean ok = service.setActive(id, false);
        return ok ? Response.ok(Map.of("updated", true)).build()
                : Response.status(Response.Status.NOT_FOUND).entity(Map.of("error","not_found")).build();
    }

    @PUT @Path("/{id}/password")
    public Response resetPassword(@Context HttpServletRequest req, @PathParam("id") long id, PasswordIn in) {
        if (!ensureAdmin(req)) return unauthorized();
        try {
            boolean ok = service.resetPassword(id, in == null ? null : in.password);
            return ok ? Response.ok(Map.of("updated", true)).build()
                    : Response.status(Response.Status.NOT_FOUND).entity(Map.of("error","not_found")).build();
        } catch (IllegalArgumentException ex) {
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", ex.getMessage())).build();
        }
    }

}
