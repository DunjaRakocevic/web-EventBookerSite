package rs.raf.eventbooker.filter;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class EncodingFilter implements Filter {
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest  req  = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;


        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");


        String uri = req.getRequestURI();
        String ct  = resp.getContentType();

        if (uri.endsWith(".js")) {
            if (ct == null || !ct.startsWith("application/javascript")) {
                resp.setContentType("application/javascript; charset=UTF-8");
            }
        } else if (uri.endsWith(".css")) {
            if (ct == null || !ct.startsWith("text/css")) {
                resp.setContentType("text/css; charset=UTF-8");
            }
        } else if (uri.endsWith(".html") || uri.endsWith(".jsp") || uri.endsWith("/")) {
            if (ct == null || !ct.startsWith("text/html")) {
                resp.setContentType("text/html; charset=UTF-8");
            }
        }

        chain.doFilter(request, response);
    }
}
