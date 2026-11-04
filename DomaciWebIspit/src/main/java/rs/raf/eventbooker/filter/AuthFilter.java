package rs.raf.eventbooker.filter;

import rs.raf.eventbooker.model.UserType;

import javax.servlet.*;
import javax.servlet.http.*;
import java.io.IOException;
import java.util.Set;

public class AuthFilter implements Filter {


    private static final Set<String> ADMIN_PREFIXES = Set.of("/api/ems/users");

    @Override public void init(FilterConfig filterConfig) { }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) { // preflight
            chain.doFilter(request, response);
            return;
        }

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("uid") == null) {
            unauthorized(resp);
            return;
        }


        Object attr = s.getAttribute("utype");
        UserType role = null;
        if (attr instanceof UserType) {
            role = (UserType) attr;
        } else if (attr instanceof String) {
            try {
                role = UserType.valueOf(((String) attr).toUpperCase());
                s.setAttribute("utype", role); // normalizuj u enum
            } catch (Exception ignore) {
                role = null;
            }
        }

        if (role == null) role = UserType.EVENT_CREATOR;

        String context = req.getContextPath();
        String path    = req.getRequestURI().substring(context.length());


        System.out.println("[AuthFilter] sid=" + s.getId()
                + " uid=" + s.getAttribute("uid")
                + " role=" + role
                + " path=" + path);


        if (requiresAdmin(path) && role != UserType.ADMIN) {
            forbidden(resp);
            return;
        }

        chain.doFilter(request, response);
    }

    private boolean requiresAdmin(String path) {
        for (String p : ADMIN_PREFIXES) {
            if (path.startsWith(p)) return true;
        }
        return false;
    }

    private void unauthorized(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"error\":\"unauthorized\"}");
    }

    private void forbidden(HttpServletResponse resp) throws IOException {
        resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
        resp.setContentType("application/json");
        resp.getWriter().write("{\"error\":\"forbidden\"}");
    }

    @Override public void destroy() { }
}
