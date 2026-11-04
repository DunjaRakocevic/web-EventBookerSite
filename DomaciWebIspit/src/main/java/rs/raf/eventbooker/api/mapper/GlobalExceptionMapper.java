package rs.raf.eventbooker.api.mapper;

import javax.ws.rs.core.*;
import javax.ws.rs.ext.*;
import java.util.Map;

@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {
    @Override
    public Response toResponse(Throwable ex) {
        String msg = ex.getMessage() == null ? "error" : ex.getMessage();

        if (ex instanceof IllegalArgumentException) {
            if ("not_found".equals(msg)) {
                return Response.status(Response.Status.NOT_FOUND).entity(Map.of("error", msg)).build();
            }
            return Response.status(Response.Status.BAD_REQUEST).entity(Map.of("error", msg)).build();
        }
        if (ex instanceof IllegalStateException) {
            return Response.status(Response.Status.CONFLICT).entity(Map.of("error", msg)).build();
        }
        ex.printStackTrace();
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(Map.of("error", "server_error")).build();
    }
}
