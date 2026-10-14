package com.smartlibrary.servlet;

import com.smartlibrary.dao.LoanDAO;
import com.smartlibrary.dao.ReservationDAO;
import com.smartlibrary.model.Loan;
import com.smartlibrary.model.User;
import com.smartlibrary.service.FineService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/student/dashboard")
public class StudentDashboardServlet extends HttpServlet {

    private final LoanDAO loanDAO = new LoanDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final FineService fineService = new FineService();
    private final com.smartlibrary.dao.FineDAO fineDAO = new com.smartlibrary.dao.FineDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User current = (User) req.getSession(false).getAttribute("user");
        try {
            List<Loan> active = loanDAO.getActiveLoansByStudent(current.getId());
            int dueSoon = 0, overdue = 0;
            double outstandingFine = 0;
            java.time.LocalDate today = java.time.LocalDate.now();
            for (Loan l : active) {
                long daysLeft = java.time.temporal.ChronoUnit.DAYS.between(today, l.getDueDate().toLocalDate());
                if (daysLeft < 0) {
                    overdue++;
                    outstandingFine += fineService.calculateCurrentFine(l);
                } else if (daysLeft <= 3) {
                    dueSoon++;
                }
            }
            List<Loan> history = loanDAO.getHistoryByStudent(current.getId());
            for (Loan l : history) {
                if (l.getFineAmount() > 0 && !l.isFinePaid()) {
                    double alreadyPaid = fineDAO.getTotalPaidForLoan(l.getId());
                    double remaining = l.getFineAmount() - alreadyPaid;
                    if (remaining > 0.001) {
                        outstandingFine += remaining;
                    }
                }
            }
            req.setAttribute("activeLoans", active);
            req.setAttribute("totalBorrowed", active.size());
            req.setAttribute("dueSoon", dueSoon);
            req.setAttribute("overdueCount", overdue);
            req.setAttribute("outstandingFine", outstandingFine);
            req.setAttribute("activeReservations", reservationDAO.getByStudent(current.getId()).size());
            req.setAttribute("recentHistory", history);
            req.getRequestDispatcher("/student/dashboard.jsp").forward(req, resp);
        } catch (SQLException e) {
            getServletContext().log("Database error loading student dashboard", e);
            req.setAttribute("error", "Could not load your dashboard right now.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }
}
