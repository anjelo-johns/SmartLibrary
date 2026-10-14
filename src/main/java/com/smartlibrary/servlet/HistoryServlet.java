package com.smartlibrary.servlet;

import com.smartlibrary.dao.LoanDAO;
import com.smartlibrary.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** A student's history of already-returned books. */
@WebServlet("/student/history")
public class HistoryServlet extends HttpServlet {

    private final LoanDAO loanDAO = new LoanDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User current = (User) req.getSession(false).getAttribute("user");
        try {
            req.setAttribute("history", loanDAO.getHistoryByStudent(current.getId()));
            req.getRequestDispatcher("/student/history.jsp").forward(req, resp);
        } catch (SQLException e) {
            getServletContext().log("Database error loading history", e);
            req.setAttribute("error", "Could not load your history right now.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }
}
