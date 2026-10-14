package com.smartlibrary.service;

import com.smartlibrary.dao.BookDAO;
import com.smartlibrary.dao.ReservationDAO;
import com.smartlibrary.exception.LibraryException;
import com.smartlibrary.model.Book;
import com.smartlibrary.model.Reservation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReservationService {

    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final BookDAO bookDAO = new BookDAO();
    private final com.smartlibrary.dao.LoanDAO loanDAO = new com.smartlibrary.dao.LoanDAO();

    /**
     * Places a hold for a student on a book that currently has no
     * available copies. Assigns the next queue position and rejects
     * duplicate reservations for the same student/book.
     */
    public void reserve(int studentId, int bookId) throws SQLException, LibraryException {
        Book book = bookDAO.getById(bookId);
        if (book == null) throw new LibraryException("Book not found.");
        if (book.getAvailableCopies() > 0) {
            throw new LibraryException("A copy is available right now — issue it instead of reserving.");
        }
        if (loanDAO.hasActiveLoanForBook(studentId, bookId)) {
            throw new LibraryException("You currently have this book checked out on loan.");
        }
        if (reservationDAO.hasActiveReservation(studentId, bookId)) {
            throw new LibraryException("You already have an active reservation for this book.");
        }
        int nextPosition = reservationDAO.countQueueForBook(bookId) + 1;
        reservationDAO.create(studentId, bookId, Date.valueOf(LocalDate.now()), nextPosition);
    }

    public void cancel(int reservationId, int studentId) throws SQLException, LibraryException {
        Reservation r = reservationDAO.findById(reservationId);
        if (r == null || r.getStudentId() != studentId) {
            throw new LibraryException("Reservation not found.");
        }
        reservationDAO.cancel(reservationId, studentId);
    }

    /**
     * Called right after a copy becomes available (a return). If
     * anyone is waiting, marks the earliest reservation in the queue
     * as READY so that student can now come and issue the book.
     * Must be called with the SAME connection/transaction used to
     * increment available_copies, so the two updates are atomic.
     */
    public void promoteNextInQueue(Connection conn, int bookId) throws SQLException {
        Reservation next = reservationDAO.getNextWaiting(conn, bookId);
        if (next != null) {
            reservationDAO.markReady(conn, next.getId());
        }
    }

    public List<Reservation> getQueueForStudent(int studentId) throws SQLException {
        return reservationDAO.getByStudent(studentId);
    }

    public List<Reservation> getAllActive() throws SQLException {
        return reservationDAO.getAllActive();
    }
}
