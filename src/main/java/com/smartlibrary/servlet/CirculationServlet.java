package com.smartlibrary.servlet;

import com.smartlibrary.dao.BookDAO;
import com.smartlibrary.dao.LoanDAO;
import com.smartlibrary.service.FineService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/**
 * The librarian's circulation desk page: a form to issue a book to a
 * student by register number, and a live table of every active loan
 * with its computed fine (if overdue) and a return button.
 */
@WebServlet("/librarian/circulation")
public class CirculationServlet extends HttpServlet {

    private final BookDAO bookDAO = new BookDAO();
    private final LoanDAO loanDAO = new LoanDAO();
    private final FineService fineService = new FineService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("books", bookDAO.getAll());
            var activeLoans = loanDAO.getAllActiveLoans();
            for (var l : activeLoans) {
                l.setFineAmount(fineService.calculateCurrentFine(l));
            }
            req.setAttribute("activeLoans", activeLoans);
            req.getRequestDispatcher("/librarian/circulation.jsp").forward(req, resp);
        } catch (SQLException e) {
            getServletContext().log("Database error loading circulation desk", e);
            req.setAttribute("error", "Could not load the circulation desk right now.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }
}
