package rs.raf.eventbooker.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public final class JsonUtil {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private JsonUtil() {}
    public static void writeJson(HttpServletResponse resp, Object body) throws IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.getWriter().write(GSON.toJson(body));
    }
}
