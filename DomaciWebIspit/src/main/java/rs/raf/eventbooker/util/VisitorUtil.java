package rs.raf.eventbooker.util;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

public final class VisitorUtil {
    private VisitorUtil(){}

    public static final String COOKIE_NAME = "eb_vid";
    private static final int MAX_AGE = 60*60*24*365*3; // 3 godine

    public static String getOrSet(HttpServletRequest req, HttpServletResponse resp){
        String key = null;

        Cookie[] cs = req.getCookies();
        if (cs != null) {
            for (Cookie c : cs) {
                if (COOKIE_NAME.equals(c.getName())) {
                    key = c.getValue();
                    break;
                }
            }
        }
        if (key == null || key.isBlank()) {
            key = UUID.randomUUID().toString().replace("-", "");
        }

        // path = root aplikacije
        String path = req.getContextPath();
        if (path == null || path.isEmpty()) path = "/";

        Cookie out = new Cookie(COOKIE_NAME, key);
        out.setHttpOnly(true);
        out.setPath(path);
        out.setMaxAge(MAX_AGE);
        if (req.isSecure()) out.setSecure(true); // ne forsiramo na localhost/http
        resp.addCookie(out);

        return key;
    }
}
