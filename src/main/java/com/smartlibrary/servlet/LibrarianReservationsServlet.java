package com.smartlibrary.servlet;

import com.smartlibrary.service.ReservationService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;

/** Read-only view for the librarian of every active reservation, across all books. */
@WebServlet("/librarian/reservations")
public class LibrarianReservationsServlet extends HttpServlet {

    private final ReservationService reservationService = new ReservationService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try {
            req.setAttribute("reservations", reservationService.getAllActive());
            req.getRequestDispatcher("/librarian/reservations.jsp").forward(req, resp);
        } catch (SQLException e) {
            getServletContext().log("Database error loading reservations", e);
            req.setAttribute("error", "Could not load reservations right now.");
            req.getRequestDispatcher("/error.jsp").forward(req, resp);
        }
    }
}
