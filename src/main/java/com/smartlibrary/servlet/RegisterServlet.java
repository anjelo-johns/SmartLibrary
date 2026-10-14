package com.smartlibrary.servlet;

import com.smartlibrary.dao.UserDAO;
import com.smartlibrary.model.User;
import com.smartlibrary.util.PasswordUtil;
import com.smartlibrary.util.ValidationUtil;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Handles new student sign-up. Every field is validated server-side
 * even though the same rules also run in the browser via
 * js/validate.js — client-side validation is only a convenience.
 */
@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String fullName = trim(req.getParameter("fullName"));
        String registerNumber = trim(req.getParameter("registerNumber"));
        String email = trim(req.getParameter("email"));
        String phone = trim(req.getParameter("phone"));
        String password = req.getParameter("password");
        String confirmPassword = req.getParameter("confirmPassword");
        String department = trim(req.getParameter("department"));
        String yearSem = trim(req.getParameter("yearSem"));

        // Preserve submitted values so the form doesn't clear on error
        req.setAttribute("fullName", fullName);
        req.setAttribute("registerNumber", registerNumber);
        req.setAttribute("email", email);
        req.setAttribute("phone", phone);
        req.setAttribute("department", department);
        req.setAttribute("yearSem", yearSem);

        String error = firstNonNull(
                ValidationUtil.validateName(fullName),
                ValidationUtil.validateRegisterNumber(registerNumber),
                ValidationUtil.validateEmail(email),
                ValidationUtil.validatePhone(phone),
                ValidationUtil.validatePassword(password),
                ValidationUtil.validateConfirmPassword(password == null ? "" : password, confirmPassword)
        );

        try {
            if (error == null && userDAO.isRegisterNumberTaken(registerNumber)) {
                error = "That register number is already registered. Try logging in instead.";
            }
            if (error == null && userDAO.isEmailTaken(email)) {
                error = "That email address is already registered.";
            }
        } catch (SQLException e) {
            forwardWithDbError(req, resp, e);
            return;
        }

        if (error != null) {
            req.setAttribute("error", error);
            req.getRequestDispatcher("/register.jsp").forward(req, resp);
            return;
        }

        User u = new User();
        u.setFullName(fullName);
        u.setRegisterNumber(registerNumber);
        u.setEmail(email);
        u.setPhone(phone);
        u.setPasswordHash(PasswordUtil.hash(password));
        u.setDepartment(department);
        u.setYearSem(yearSem);

        try {
            userDAO.insertStudent(u);
        } catch (SQLException e) {
            forwardWithDbError(req, resp, e);
            return;
        }

        req.setAttribute("registered", true);
        resp.sendRedirect(req.getContextPath() + "/login?registered=1");
    }

    private void forwardWithDbError(HttpServletRequest req, HttpServletResponse resp, SQLException e)
            throws ServletException, IOException {
        getServletContext().log("Database error during registration", e);
        req.setAttribute("error", "We couldn't reach the database right now. Please try again in a moment.");
        req.getRequestDispatcher("/register.jsp").forward(req, resp);
    }

    private static String trim(String s) {
        return s == null ? null : s.trim();
    }

    private static String firstNonNull(String... values) {
        for (String v : values) if (v != null) return v;
        return null;
    }
}
