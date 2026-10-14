package com.smartlibrary.filter;

import com.smartlibrary.model.Role;
import com.smartlibrary.model.User;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Runs after AuthenticationFilter has already confirmed the user is
 * logged in. Makes sure a STUDENT can never reach a /librarian/* page
 * and a LIBRARIAN can never reach a /student/* page, even by typing
 * the URL directly.
 */
@WebFilter(urlPatterns = {"/student/*", "/librarian/*"})
public class AuthorizationFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest req, ServletResponse resp, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) resp;
        String path = request.getRequestURI().substring(request.getContextPath().length());

        HttpSession session = request.getSession(false);
        User user = (session != null) ? (User) session.getAttribute("user") : null;

        if (user != null) {
            boolean wantsLibrarianArea = path.startsWith("/librarian/");
            boolean wantsStudentArea = path.startsWith("/student/");
            boolean allowed = (wantsLibrarianArea && user.getRole() == Role.LIBRARIAN)
                    || (wantsStudentArea && user.getRole() == Role.STUDENT);
            if (!allowed) {
                request.getRequestDispatcher("/unauthorized.jsp").forward(req, resp);
                return;
            }
        }
        chain.doFilter(req, resp);
    }

    @Override
    public void destroy() {
    }
}
