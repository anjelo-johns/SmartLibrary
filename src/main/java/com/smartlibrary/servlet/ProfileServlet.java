package com.smartlibrary.servlet;

import com.smartlibrary.dao.UserDAO;
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

/** User profile: view details, change password for students and librarians. */
@WebServlet({"/student/profile", "/librarian/profile"})
public class ProfileServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User current = (User) req.getSession(false).getAttribute("user");
        String jsp = (current != null && current.isLibrarian()) ? "/librarian/profile.jsp" : "/student/profile.jsp";
        req.getRequestDispatcher(jsp).forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User current = (User) req.getSession(false).getAttribute("user");
        String jsp = (current != null && current.isLibrarian()) ? "/librarian/profile.jsp" : "/student/profile.jsp";
        String redirectTarget = (current != null && current.isLibrarian()) ? "/librarian/profile" : "/student/profile";

        String currentPassword = req.getParameter("currentPassword");
        String newPassword = req.getParameter("newPassword");
        String confirmPassword = req.getParameter("confirmPassword");
        HttpSession session = req.getSession(true);

        try {
            User fresh = userDAO.findById(current.getId());
            if (fresh == null || !PasswordUtil.verify(currentPassword == null ? "" : currentPassword, fresh.getPasswordHash())) {
                req.setAttribute("error", "Current password is incorrect.");
                req.getRequestDispatcher(jsp).forward(req, resp);
                return;
            }
            String error = ValidationUtil.validatePassword(newPassword);
            if (error == null) error = ValidationUtil.validateConfirmPassword(newPassword, confirmPassword);
            if (error != null) {
                req.setAttribute("error", error);
                req.getRequestDispatcher(jsp).forward(req, resp);
                return;
            }
            userDAO.updatePassword(current.getId(), PasswordUtil.hash(newPassword));
            session.setAttribute("flashSuccess", "Password updated successfully.");
            resp.sendRedirect(req.getContextPath() + redirectTarget);
        } catch (SQLException e) {
            getServletContext().log("Database error changing password", e);
            req.setAttribute("error", "Could not update your password right now.");
            req.getRequestDispatcher(jsp).forward(req, resp);
        }
    }
}
