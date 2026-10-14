package com.smartlibrary.servlet;

import com.smartlibrary.dao.FineDAO;
import com.smartlibrary.dao.LoanDAO;
import com.smartlibrary.exception.LibraryException;
import com.smartlibrary.model.Loan;
import com.smartlibrary.model.Role;
import com.smartlibrary.model.User;
import com.smartlibrary.service.FineService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mapped to both /student/fines (a student's own fine + payment
 * history) and /librarian/fines (every outstanding fine, with the
 * ability to record a payment).
 */
@WebServlet({"/student/fines", "/librarian/fines"})
public class FineServlet extends HttpServlet {

    private final LoanDAO loanDAO = new LoanDAO();
    private final FineDAO fineDAO = new FineDAO();
    private final FineService fineService = new FineService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User current = (User) req.getSession(false).getAttribute("user");
        try {
            if (current.getRole() == Role.STUDENT) {
                List<Loan> loans = loanDAO.getLoansByStudentId(current.getId());
                for (Loan l : loans) {
                    if (l.getReturnDate() == null) {
                        double currentFine = fineService.calculateCurrentFine(l);
                        if (currentFine > 0) {
                            l.setFineAmount(currentFine);
                        }
                    }
                }
                req.setAttribute("loans", loans);
                req.setAttribute("payments", fineDAO.getPaymentsByStudent(current.getId()));
                req.getRequestDispatcher("/student/fines.jsp").forward(req, resp);
            } else {
                List<Loan> allLoans = loanDAO.getAllActiveLoans();
                List<Loan> returnedUnpaid = loanDAO.getUnpaidReturnedLoans();
                List<Loan> loansWithFines = new ArrayList<>();
                double totalOutstanding = 0;

                for (Loan l : returnedUnpaid) {
                    double alreadyPaid = fineDAO.getTotalPaidForLoan(l.getId());
                    double remaining = l.getFineAmount() - alreadyPaid;
                    if (remaining > 0.001) {
                        l.setFineAmount(remaining);
                        loansWithFines.add(l);
                        totalOutstanding += remaining;
                    }
                }

                for (Loan l : allLoans) {
                    double fine = fineService.calculateCurrentFine(l);
                    if (fine > 0) {
                        double alreadyPaid = fineDAO.getTotalPaidForLoan(l.getId());
                        double remaining = fine - alreadyPaid;
                        if (remaining > 0.001) {
                            l.setFineAmount(remaining);
                            loansWithFines.add(l);
                            totalOutstanding += remaining;
                        }
                    }
                }

                req.setAttribute("overdueLoans", loansWithFines);
                req.setAttribute("totalOutstanding", totalOutstanding);
                req.setAttribute("totalCollected", fineDAO.getTotalCollected());
                req.getRequestDispatcher("/librarian/fines.jsp").forward(req, resp);
            }
        } catch (SQLException e) {
            getServletContext().log("Database error loading fines", e);
            req.setAttribute("error", "Could not load fine information right now.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User current = (User) req.getSession(false).getAttribute("user");
        HttpSession session = req.getSession(true);

        if (current.getRole() != Role.LIBRARIAN) {
            session.setAttribute("flashError", "Only library staff can record a fine payment.");
            resp.sendRedirect(req.getContextPath() + "/student/fines");
            return;
        }

        try {
            int loanId = Integer.parseInt(req.getParameter("loanId"));
            double amount = Double.parseDouble(req.getParameter("amount"));
            fineService.payFine(loanId, amount, current.getId());
            session.setAttribute("flashSuccess", "Payment recorded.");
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Enter a valid payment amount.");
        } catch (LibraryException e) {
            session.setAttribute("flashError", e.getMessage());
        } catch (SQLException e) {
            getServletContext().log("Database error recording payment", e);
            session.setAttribute("flashError", "Could not record this payment right now.");
        }
        resp.sendRedirect(req.getContextPath() + "/librarian/fines");
    }
}
