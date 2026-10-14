package com.smartlibrary.servlet;

import com.smartlibrary.exception.LibraryException;
import com.smartlibrary.model.User;
import com.smartlibrary.service.ReservationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;

/**
 * Student-facing reservation actions: place a hold on an unavailable
 * book, or cancel one of your own. Librarian-side reservation
 * viewing lives in LibrarianReservationsServlet.
 */
@WebServlet("/student/reservations")
public class ReservationServlet extends HttpServlet {

    private final ReservationService reservationService = new ReservationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User current = (User) req.getSession(false).getAttribute("user");
        try {
            req.setAttribute("reservations", reservationService.getQueueForStudent(current.getId()));
            req.getRequestDispatcher("/student/reservations.jsp").forward(req, resp);
        } catch (SQLException e) {
            getServletContext().log("Database error loading reservations", e);
            req.setAttribute("error", "Could not load your reservations right now.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        User current = (User) req.getSession(false).getAttribute("user");
        String action = req.getParameter("action");
        HttpSession session = req.getSession(true);

        try {
            if ("create".equals(action)) {
                int bookId = Integer.parseInt(req.getParameter("bookId"));
                reservationService.reserve(current.getId(), bookId);
                session.setAttribute("flashSuccess", "Reserved. You'll be notified once a copy is free.");
            } else if ("cancel".equals(action)) {
                int reservationId = Integer.parseInt(req.getParameter("reservationId"));
                reservationService.cancel(reservationId, current.getId());
                session.setAttribute("flashSuccess", "Reservation cancelled.");
            } else {
                session.setAttribute("flashError", "Unknown action.");
            }
        } catch (NumberFormatException e) {
            session.setAttribute("flashError", "Invalid request.");
        } catch (LibraryException e) {
            session.setAttribute("flashError", e.getMessage());
        } catch (SQLException e) {
            getServletContext().log("Database error handling reservation", e);
            session.setAttribute("flashError", "Could not process this right now. Please try again.");
        }
        resp.sendRedirect(req.getContextPath() + "/student/reservations");
    }
}
