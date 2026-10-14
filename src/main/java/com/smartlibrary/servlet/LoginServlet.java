package com.smartlibrary.servlet;

import com.smartlibrary.dao.UserDAO;
import com.smartlibrary.model.Role;
import com.smartlibrary.model.User;
import com.smartlibrary.util.PasswordUtil;
import com.smartlibrary.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Handles both student and librarian login from the same form (a
 * "loginAs" hidden field tells us which). On success it stores the
 * User object in the session — every filter and JSP downstream reads
 * session.getAttribute("user") to know who's logged in.
 */
@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/login.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String loginAs = req.getParameter("loginAs"); // "student" or "librarian"
        String identifier = trim(req.getParameter("identifier")); // register number OR username
        String password = req.getParameter("password");

        req.setAttribute("loginAs", loginAs);
        req.setAttribute("identifier", identifier);

        if (ValidationUtil.isBlank(identifier) || ValidationUtil.isBlank(password)) {
            req.setAttribute("error", "Please fill in both fields.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
            return;
        }

        try {
            User user = "librarian".equals(loginAs)
                    ? userDAO.findByUsername(identifier)
                    : userDAO.findByRegisterNumber(identifier);

            boolean roleMatches = user != null
                    && ((("librarian".equals(loginAs) && user.getRole() == Role.LIBRARIAN))
                        || ("student".equals(loginAs) && user.getRole() == Role.STUDENT));

            if (user == null || !roleMatches || !PasswordUtil.verify(password, user.getPasswordHash())) {
                req.setAttribute("error", "librarian".equals(loginAs)
                        ? "Incorrect staff username or password."
                        : "Incorrect register number or password.");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }

            if ("INACTIVE".equals(user.getStatus())) {
                req.setAttribute("error", "This account has been deactivated. Contact the library desk.");
                req.getRequestDispatcher("/login.jsp").forward(req, resp);
                return;
            }

            // New session on every login to prevent session fixation
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) oldSession.invalidate();
            HttpSession session = req.getSession(true);
            session.setAttribute("user", user);
            session.setMaxInactiveInterval(com.smartlibrary.util.LibraryConfig.SESSION_TIMEOUT_MINUTES * 60);

            String contextPath = req.getContextPath();
            resp.sendRedirect(contextPath + (user.getRole() == Role.LIBRARIAN
                    ? "/librarian/dashboard" : "/student/dashboard"));
        } catch (SQLException e) {
            getServletContext().log("Database error during login", e);
            req.setAttribute("error", "We couldn't reach the database right now. Please try again in a moment.");
            req.getRequestDispatcher("/login.jsp").forward(req, resp);
        }
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }
}
