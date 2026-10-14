package com.smartlibrary.filter;

import com.smartlibrary.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Blocks access to every /student/* and /librarian/* page unless the
 * request has a valid, logged-in session. Anonymous requests are
 * redirected to the login page — this is what stops someone from
 * reaching a dashboard just by typing its URL.
 */
@WebFilter(urlPatterns = {"/student/*", "/librarian/*"})
public class AuthenticationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user == null) {
            String contextPath = request.getContextPath();
            response.sendRedirect(contextPath + "/login.jsp?timeout=1");
            return;
        }
        chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
    }
}
