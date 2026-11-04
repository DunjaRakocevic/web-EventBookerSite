package rs.raf.eventbooker.api;

import rs.raf.eventbooker.config.DIContainer;
import rs.raf.eventbooker.model.User;
import rs.raf.eventbooker.repository.UserRepository;
import rs.raf.eventbooker.repository.impl.UserRepositoryJdbcImpl;
import org.mindrot.jbcrypt.BCrypt;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Path("/auth")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class AuthResource {

    private final UserRepository users = DIContainer.users();


    public static class LoginReq {
        public String email;
        public String password;
    }

    @POST
    @Path("/login")
    public Response login(LoginReq in, @Context HttpServletRequest req) {
        if (in == null || in.email == null || in.password == null
                || in.email.isBlank() || in.password.isBlank()) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(Map.of("error", "bad_credentials")).build();
        }

        User u = users.findByEmail(in.email).orElse(null);
        if (u == null || !u.isActive()) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "bad_credentials")).build();
        }

        String hash = u.getPasswordHash();

        if (hash == null || !(hash.startsWith("$2a$") || hash.startsWith("$2b$") || hash.startsWith("$2y$"))
                || !BCrypt.checkpw(in.password, hash)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(Map.of("error", "bad_credentials")).build();
        }

        HttpSession session = req.getSession(true);
        session.setAttribute("uid", u.getId());
        session.setAttribute("utype", u.getType());

        return Response.ok(Map.of(
                "id", u.getId(),
                "email", u.getEmail(),
                "firstName", u.getFirstName(),
                "lastName", u.getLastName(),
                "type", u.getType()
        )).build();
    }

    @GET
    @Path("/me")
    public Response me(@Context HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s == null) return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "unauthorized")).build();
        Long uid = (Long) s.getAttribute("uid");
        if (uid == null) return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "unauthorized")).build();

        User u = users.findById(uid).orElse(null);
        if (u == null) return Response.status(Response.Status.UNAUTHORIZED)
                .entity(Map.of("error", "unauthorized")).build();

        return Response.ok(Map.of(
                "id", u.getId(),
                "email", u.getEmail(),
                "firstName", u.getFirstName(),
                "lastName", u.getLastName(),
                "type", u.getType()
        )).build();
    }

    @POST
    @Path("/logout")
    public Response logout(@Context HttpServletRequest req) {
        HttpSession s = req.getSession(false);
        if (s != null) s.invalidate();
        return Response.noContent().build();
    }
}
