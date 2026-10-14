package com.smartlibrary.servlet;

import com.smartlibrary.dao.LoanDAO;
import com.smartlibrary.exception.LibraryException;
import com.smartlibrary.model.Loan;
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
 * Handles returning a book, mapped to both /student/return (self
 * return) and /librarian/return (desk return). A student may only
 * return their own loan — this is checked explicitly so tampering
 * with the loanId in the form can't return someone else's book.
 */
@WebServlet({"/student/return", "/librarian/return"})
public class ReturnBookServlet extends HttpServlet {

    private final LibraryService libraryService = new LibraryService();
    private final LoanDAO loanDAO = new LoanDAO();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        HttpSession session = req.getSession(false);
        User current = (User) session.getAttribute("user");

        int loanId;
        try {
            loanId = Integer.parseInt(req.getParameter("loanId"));
        } catch (NumberFormatException e) {
            redirectWithFlash(req, resp, current, "Invalid loan reference.", true);
            return;
        }

        try {
            if (current.getRole() == Role.STUDENT) {
                Loan loan = loanDAO.findById(loanId);
                if (loan == null || loan.getStudentId() != current.getId()) {
                    redirectWithFlash(req, resp, current, "That loan does not belong to you.", true);
                    return;
                }
                double fine = libraryService.returnBook(loanId, null);
                redirectWithFlash(req, resp, current,
                        fine > 0 ? String.format("Returned — a fine of Rs. %.2f applies.", fine) : "Returned, no fine due.",
                        false);
            } else {
                double fine = libraryService.returnBook(loanId, current.getId());
                redirectWithFlash(req, resp, current,
                        fine > 0 ? String.format("Returned — collect Rs. %.2f from the student.", fine) : "Returned, no fine due.",
                        false);
            }
        } catch (LibraryException e) {
            redirectWithFlash(req, resp, current, e.getMessage(), true);
        } catch (SQLException e) {
            getServletContext().log("Database error returning book", e);
            redirectWithFlash(req, resp, current, "Could not process this right now. Please try again.", true);
        }
    }

    private void redirectWithFlash(HttpServletRequest req, HttpServletResponse resp, User current,
                                    String message, boolean isError) throws IOException {
        HttpSession session = req.getSession(true);
        session.setAttribute(isError ? "flashError" : "flashSuccess", message);
        String target = current.getRole() == Role.LIBRARIAN ? "/librarian/circulation" : "/student/my-books";
        resp.sendRedirect(req.getContextPath() + target);
    }
}
