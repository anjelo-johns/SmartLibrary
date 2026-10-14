package com.smartlibrary.servlet;

import com.smartlibrary.dao.BookDAO;
import com.smartlibrary.dao.CategoryDAO;
import com.smartlibrary.model.Book;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

/**
 * Student-facing catalog: search + category/availability filters.
 * Supports a plain HTML page (default) and a JSON response
 * (?format=json) for the live-search AJAX call used in catalog.jsp,
 * demonstrating JSON-based client/server communication as requested.
 */
@WebServlet("/student/catalog")
public class CatalogServlet extends HttpServlet {

    private final BookDAO bookDAO = new BookDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String q = req.getParameter("q");
        String categoryIdStr = req.getParameter("categoryId");
        boolean onlyAvailable = "1".equals(req.getParameter("onlyAvailable"));

        Integer categoryId = null;
        try {
            if (categoryIdStr != null && !categoryIdStr.isEmpty()) categoryId = Integer.parseInt(categoryIdStr);
        } catch (NumberFormatException ignored) {
            categoryId = null;
        }

        try {
            List<Book> books = bookDAO.search(q, categoryId, onlyAvailable);

            if ("json".equals(req.getParameter("format"))) {
                writeJson(resp, books);
                return;
            }

            req.setAttribute("books", books);
            req.setAttribute("categories", categoryDAO.getAll());
            req.setAttribute("q", q);
            req.setAttribute("categoryId", categoryId);
            req.setAttribute("onlyAvailable", onlyAvailable);
            req.getRequestDispatcher("/student/catalog.jsp").forward(req, resp);
        } catch (SQLException e) {
            getServletContext().log("Database error loading catalog", e);
            req.setAttribute("error", "Could not load the catalog right now. Please try again shortly.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }

    private void writeJson(HttpServletResponse resp, List<Book> books) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        StringBuilder json = new StringBuilder("[");
        for (int i = 0; i < books.size(); i++) {
            Book b = books.get(i);
            if (i > 0) json.append(",");
            json.append("{")
                .append("\"id\":").append(b.getId()).append(",")
                .append("\"title\":\"").append(esc(b.getTitle())).append("\",")
                .append("\"author\":\"").append(esc(b.getAuthor())).append("\",")
                .append("\"category\":\"").append(esc(b.getCategoryName())).append("\",")
                .append("\"available\":").append(b.getAvailableCopies()).append(",")
                .append("\"totalCopies\":").append(b.getTotalCopies())
                .append("}");
        }
        json.append("]");
        try (PrintWriter out = resp.getWriter()) {
            out.print(json);
        }
    }

    private String esc(String s) {
        return s == null ? "" : s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
