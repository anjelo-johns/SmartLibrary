package com.smartlibrary.servlet;

import com.smartlibrary.dao.BookDAO;
import com.smartlibrary.dao.LoanDAO;
import com.smartlibrary.dao.ReservationDAO;
import com.smartlibrary.dao.UserDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

@WebServlet("/librarian/dashboard")
public class LibrarianDashboardServlet extends HttpServlet {

    private final BookDAO bookDAO = new BookDAO();
    private final LoanDAO loanDAO = new LoanDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final UserDAO userDAO = new UserDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            loanDAO.refreshOverdueStatuses(); // housekeeping: flag anything now overdue
            req.setAttribute("totalBooks", bookDAO.countTotalBooks());
            req.setAttribute("totalCopies", bookDAO.countTotalCopies());
            req.setAttribute("availableCopies", bookDAO.countAvailableCopies());
            req.setAttribute("activeLoans", loanDAO.getAllActiveLoans());
            req.setAttribute("overdueLoans", loanDAO.getOverdueLoans());
            req.setAttribute("totalStudents", userDAO.countStudents());
            req.setAttribute("activeReservations", reservationDAO.countActive());
            req.setAttribute("outstandingFines", loanDAO.totalOutstandingFines());
            req.setAttribute("mostBorrowed", loanDAO.mostBorrowedBooks(5));
            req.getRequestDispatcher("/librarian/dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            getServletContext().log("Database error loading librarian dashboard", e);
            req.setAttribute("error", "Could not load the dashboard right now.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }
}
