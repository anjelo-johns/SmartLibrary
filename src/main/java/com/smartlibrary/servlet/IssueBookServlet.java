package com.smartlibrary.servlet;

import com.smartlibrary.dao.UserDAO;
import com.smartlibrary.exception.LibraryException;
import com.smartlibrary.model.Role;
import com.smartlibrary.model.User;
import com.smartlibrary.service.LibraryService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Handles issuing a book. Mapped to two URLs:
 *  - /student/issue   : a student issuing a copy to themselves from the catalog
 *  - /librarian/issue : a librarian issuing a copy to a student at the circulation desk
 * Both paths funnel through the same LibraryService so the business
 * rules (availability, duplicate loan, borrowing limit) are enforced
 * identically either way.
 */
@WebServlet({"/student/issue", "/librarian/issue"})
public class IssueBookServlet extends HttpServlet {

    private final LibraryService libraryService = new LibraryService();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User current = (User) session.getAttribute("user");

        int bookId;
        try {
            bookId = Integer.parseInt(req.getParameter("bookId"));
        } catch (NumberFormatException e) {
            redirectWithFlash(req, resp, current, "Invalid book selected.", true);
            return;
        }

        try {
            if (current.getRole() == Role.LIBRARIAN) {
                String regNo = req.getParameter("registerNumber");
                User student = userDAO.findByRegisterNumber(regNo == null ? "" : regNo.trim());
                if (student == null || student.getRole() != Role.STUDENT) {
                    redirectWithFlash(req, resp, current, "No student found with register number \"" + regNo + "\".", true);
                    return;
                }
                libraryService.issueBook(student.getId(), bookId, current.getId());
                redirectWithFlash(req, resp, current,
                        "Book issued to " + student.getFullName() + " (" + student.getRegisterNumber() + ").", false);
            } else {
                libraryService.issueBook(current.getId(), bookId, null);
                redirectWithFlash(req, resp, current, "Book issued to you — check My Books for the due date.", false);
            }
        } catch (LibraryException e) {
            redirectWithFlash(req, resp, current, e.getMessage(), true);
        } catch (SQLException e) {
            getServletContext().log("Database error issuing book", e);
            redirectWithFlash(req, resp, current, "Could not process this right now. Please try again.", true);
        }
    }

    private void redirectWithFlash(HttpServletRequest req, HttpServletResponse resp, User current,
                                    String message, boolean isError) throws IOException {
        HttpSession session = req.getSession(true);
        session.setAttribute(isError ? "flashError" : "flashSuccess", message);
        String target = current.getRole() == Role.LIBRARIAN ? "/librarian/circulation" : "/student/catalog";
        resp.sendRedirect(req.getContextPath() + target);
    }
}
