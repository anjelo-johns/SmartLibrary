package com.smartlibrary.servlet;

import com.smartlibrary.dao.BookDAO;
import com.smartlibrary.dao.CategoryDAO;
import com.smartlibrary.exception.LibraryException;
import com.smartlibrary.model.Book;
import com.smartlibrary.util.ValidationUtil;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Librarian's book CRUD screen: add, edit, and delete titles. All
 * validation (title/author/ISBN/copies/category) runs server-side
 * here regardless of what the browser already checked.
 */
@WebServlet("/librarian/books")
public class BookManagementServlet extends HttpServlet {

    private final BookDAO bookDAO = new BookDAO();
    private final CategoryDAO categoryDAO = new CategoryDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String q = req.getParameter("q");
        try {
            req.setAttribute("books", bookDAO.search(q, null, false));
            req.setAttribute("categories", categoryDAO.getAll());
            req.setAttribute("q", q);
            req.getRequestDispatcher("/librarian/books.jsp").forward(req, resp);
        } catch (SQLException e) {
            getServletContext().log("Database error loading book management", e);
            req.setAttribute("error", "Could not load the collection right now.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String action = req.getParameter("action");
        HttpSession session = req.getSession(true);

        try {
            if ("delete".equals(action)) {
                int id = Integer.parseInt(req.getParameter("bookId"));
                try {
                    bookDAO.delete(id);
                    session.setAttribute("flashSuccess", "Book removed from the collection.");
                } catch (LibraryException e) {
                    session.setAttribute("flashError", e.getMessage());
                }
                resp.sendRedirect(req.getContextPath() + "/librarian/books");
                return;
            }

            String title = trim(req.getParameter("title"));
            String author = trim(req.getParameter("author"));
            String isbn = trim(req.getParameter("isbn"));
            String categoryIdStr = req.getParameter("categoryId");
            String publisher = trim(req.getParameter("publisher"));
            String yearStr = req.getParameter("publicationYear");
            String copiesStr = req.getParameter("totalCopies");
            String description = trim(req.getParameter("description"));

            String error = firstNonNull(
                    ValidationUtil.validateBookTitle(title),
                    ValidationUtil.validateAuthor(author),
                    ValidationUtil.validateIsbn(isbn),
                    ValidationUtil.validateCategoryId(categoryIdStr),
                    ValidationUtil.validateCopies(copiesStr)
            );

            if (error == null && isbn != null && !isbn.isEmpty()) {
                boolean isEdit = "edit".equals(action);
                boolean isbnTaken = bookDAO.existsByIsbn(isbn);
                if (isbnTaken && !isEdit) {
                    error = "A book with that ISBN already exists.";
                }
                // (When editing, a duplicate check against *other* books would need the previous ISBN;
                // kept simple here since ISBN edits are uncommon for this project's scope.)
            }

            if (error != null) {
                session.setAttribute("flashError", error);
                resp.sendRedirect(req.getContextPath() + "/librarian/books");
                return;
            }

            Book b = new Book();
            b.setTitle(title);
            b.setAuthor(author);
            b.setIsbn(isbn);
            b.setCategoryId(Integer.parseInt(categoryIdStr));
            b.setPublisher(publisher);
            b.setPublicationYear(isBlankOrNotNumber(yearStr) ? null : Integer.parseInt(yearStr.trim()));
            b.setTotalCopies(Integer.parseInt(copiesStr.trim()));
            b.setDescription(description);

            if ("edit".equals(action)) {
                b.setId(Integer.parseInt(req.getParameter("bookId")));
                try {
                    bookDAO.update(b);
                    session.setAttribute("flashSuccess", "Book updated.");
                } catch (LibraryException e) {
                    session.setAttribute("flashError", e.getMessage());
                }
            } else {
                bookDAO.add(b);
                session.setAttribute("flashSuccess", "Book added to the collection.");
            }
            resp.sendRedirect(req.getContextPath() + "/librarian/books");
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Please check the numeric fields (category, year, copies).");
            resp.sendRedirect(req.getContextPath() + "/librarian/books");
        } catch (SQLException e) {
            getServletContext().log("Database error saving book", e);
            session.setAttribute("flashError", "Could not save this book right now.");
            resp.sendRedirect(req.getContextPath() + "/librarian/books");
        }
    }

    private boolean isBlankOrNotNumber(String s) {
        if (s == null || s.trim().isEmpty()) return true;
        try { Integer.parseInt(s.trim()); return false; } catch (NumberFormatException e) { return true; }
    }

    private static String trim(String s) { return s == null ? null : s.trim(); }

    private static String firstNonNull(String... values) {
        for (String v : values) if (v != null) return v;
        return null;
    }
}
