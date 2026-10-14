package com.smartlibrary.servlet;

import com.smartlibrary.dao.LoanDAO;
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

/** A student's currently-borrowed books, with live fine amounts for anything overdue. */
@WebServlet("/student/my-books")
public class MyBooksServlet extends HttpServlet {

    private final LoanDAO loanDAO = new LoanDAO();
    private final FineService fineService = new FineService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User current = (User) req.getSession(false).getAttribute("user");
        try {
            List<Loan> active = loanDAO.getActiveLoansByStudent(current.getId());
            for (Loan l : active) {
                l.setFineAmount(fineService.calculateCurrentFine(l));
            }
            req.setAttribute("activeLoans", active);
            req.getRequestDispatcher("/student/my-books.jsp").forward(req, resp);
        } catch (SQLException e) {
            getServletContext().log("Database error loading my-books", e);
            req.setAttribute("error", "Could not load your books right now.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }
}
