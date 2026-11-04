package rs.raf.eventbooker.api;

import javax.ws.rs.*;
import javax.ws.rs.core.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;

@Path("/public")
@Produces(MediaType.APPLICATION_JSON)
public class PublicResource {

    private static final String VISITOR_COOKIE = "evb_vid";


    private String ensureVisitor(@Context HttpServletRequest req,
                                 @Context HttpServletResponse resp) {
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if (VISITOR_COOKIE.equals(c.getName())) return c.getValue();
            }
        }
        String vid = java.util.UUID.randomUUID().toString();
        Cookie c = new Cookie(VISITOR_COOKIE, vid);
        c.setPath("/");
        c.setMaxAge(60 * 60 * 24 * 365); // 1year
        resp.addCookie(c);
        return vid;
    }

    @GET @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping() {
        return "pong";
    }
}
