package com.smartlibrary.servlet;

import com.smartlibrary.dao.FineDAO;
import com.smartlibrary.dao.LoanDAO;
import com.smartlibrary.dao.ReservationDAO;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Librarian reports: most-borrowed books, currently issued books,
 * overdue books, fine collection totals, and active reservations —
 * all read directly from the database rather than recomputed in
 * JavaScript.
 */
@WebServlet("/librarian/reports")
public class ReportServlet extends HttpServlet {

    private final LoanDAO loanDAO = new LoanDAO();
    private final FineDAO fineDAO = new FineDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("mostBorrowed", loanDAO.mostBorrowedBooks(10));
            req.setAttribute("activeLoans", loanDAO.getAllActiveLoans());
            req.setAttribute("overdueLoans", loanDAO.getOverdueLoans());
            req.setAttribute("outstandingFines", loanDAO.totalOutstandingFines());
            req.setAttribute("totalCollected", fineDAO.getTotalCollected());
            req.setAttribute("activeReservations", reservationDAO.getAllActive());
            req.getRequestDispatcher("/librarian/reports.jsp").forward(req, resp);
        } catch (SQLException e) {
            getServletContext().log("Database error building reports", e);
            req.setAttribute("error", "Could not build reports right now.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }
}
