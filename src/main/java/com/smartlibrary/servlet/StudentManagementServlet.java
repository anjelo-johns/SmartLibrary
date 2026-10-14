package com.smartlibrary.servlet;

import com.smartlibrary.dao.LoanDAO;
import com.smartlibrary.dao.UserDAO;
import com.smartlibrary.model.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * Librarian's student directory: search students, and drill into one
 * student's full borrowing history.
 */
@WebServlet("/librarian/students")
public class StudentManagementServlet extends HttpServlet {

    private final UserDAO userDAO = new UserDAO();
    private final LoanDAO loanDAO = new LoanDAO();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            String viewId = req.getParameter("id");
            if (viewId != null && !viewId.isEmpty()) {
                User student = userDAO.findById(Integer.parseInt(viewId));
                if (student == null || student.getRole() != com.smartlibrary.model.Role.STUDENT) {
                    req.setAttribute("error", "Student not found.");
                    req.getRequestDispatcher("/error.jsp").forward(req, resp);
                    return;
                }
                req.setAttribute("student", student);
                req.setAttribute("loans", loanDAO.getLoansByStudentId(student.getId()));
                req.getRequestDispatcher("/librarian/student-history.jsp").forward(req, resp);
                return;
            }

            String q = req.getParameter("q");
            List<User> students = (q == null || q.trim().isEmpty()) ? userDAO.listAllStudents() : userDAO.searchStudents(q.trim());
            req.setAttribute("students", students);
            req.setAttribute("q", q);
            req.getRequestDispatcher("/librarian/students.jsp").forward(req, resp);
        } catch (SQLException e) {
            getServletContext().log("Database error loading students", e);
            req.setAttribute("error", "Could not load student records right now.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        } catch (NumberFormatException e) {
            req.setAttribute("error", "Invalid student reference.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }
}
