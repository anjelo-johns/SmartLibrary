package com.smartlibrary.service;

import com.smartlibrary.dao.BookDAO;
import com.smartlibrary.dao.LoanDAO;
import com.smartlibrary.dao.ReservationDAO;
import com.smartlibrary.exception.LibraryException;
import com.smartlibrary.model.Book;
import com.smartlibrary.model.Loan;
import com.smartlibrary.model.Reservation;
import com.smartlibrary.util.DBConnection;
import com.smartlibrary.util.LibraryConfig;

import java.sql.Connection;
import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;

/**
 * Orchestrates the issue and return workflows. Both run inside a
 * single JDBC transaction (manual commit/rollback) so that the loan
 * row and the book's available_copies count can never drift apart,
 * even under concurrent requests.
 */
public class LibraryService {

    private final BookDAO bookDAO = new BookDAO();
    private final LoanDAO loanDAO = new LoanDAO();
    private final ReservationDAO reservationDAO = new ReservationDAO();
    private final FineService fineService = new FineService();
    private final ReservationService reservationService = new ReservationService();

    /**
     * Issues a book to a student.
     *
     * @param issuedByLibrarianId null when the student self-issues from the catalog,
     *                            otherwise the librarian's user id.
     * @return the new loan's id.
     */
    public int issueBook(int studentId, int bookId, Integer issuedByLibrarianId) throws SQLException, LibraryException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Book book = bookDAO.getByIdForUpdate(conn, bookId);
                if (book == null) {
                    throw new LibraryException("Book not found.");
                }
                if (book.getAvailableCopies() <= 0) {
                    throw new LibraryException("\"" + book.getTitle() + "\" has no available copies right now. You can reserve it instead.");
                }
                if (loanDAO.hasActiveLoanForBook(conn, studentId, bookId)) {
                    throw new LibraryException("This student already has an active loan for \"" + book.getTitle() + "\".");
                }
                int activeCount = loanDAO.countActiveLoansForStudent(conn, studentId);
                if (activeCount >= LibraryConfig.MAX_ACTIVE_LOANS_PER_STUDENT) {
                    throw new LibraryException("Borrowing limit reached — a student may have at most "
                            + LibraryConfig.MAX_ACTIVE_LOANS_PER_STUDENT + " books out at once.");
                }

                Reservation userRes = reservationDAO.findActiveReservation(conn, studentId, bookId);
                int reservedForOthers = reservationDAO.countReadyReservationsForOtherStudents(conn, bookId, studentId);
                if (book.getAvailableCopies() <= reservedForOthers && userRes == null) {
                    throw new LibraryException("The available copy of \"" + book.getTitle() + "\" is currently held for a student with a ready reservation.");
                }

                LocalDate today = LocalDate.now();
                Loan loan = new Loan();
                loan.setBookId(bookId);
                loan.setStudentId(studentId);
                loan.setIssuedBy(issuedByLibrarianId);
                loan.setIssueDate(Date.valueOf(today));
                loan.setDueDate(Date.valueOf(today.plusDays(LibraryConfig.LOAN_PERIOD_DAYS)));

                int loanId = loanDAO.createLoan(conn, loan);
                bookDAO.decrementAvailable(conn, bookId);

                if (userRes != null) {
                    reservationDAO.markFulfilled(conn, userRes.getId());
                }

                conn.commit();
                return loanId;
            } catch (LibraryException | SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    /**
     * Returns a book, calculates the fine (if any), frees up the
     * copy, and promotes the next reservation in the queue for that
     * book so it's ready for the next student.
     *
     * @param returnedByLibrarianId null when the student self-returns, otherwise the librarian's user id.
     * @return the fine amount charged for this return (0 if on time).
     */
    public double returnBook(int loanId, Integer returnedByLibrarianId) throws SQLException, LibraryException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Loan loan = loanDAO.findByIdForUpdate(conn, loanId);
                if (loan == null) {
                    throw new LibraryException("Loan record not found.");
                }
                if (loan.getReturnDate() != null) {
                    throw new LibraryException("This book has already been returned.");
                }
                Date today = Date.valueOf(LocalDate.now());
                double fine = fineService.calculateFineOnReturn(loan.getDueDate(), today);

                loanDAO.recordReturn(conn, loanId, today, fine, returnedByLibrarianId);
                bookDAO.incrementAvailable(conn, loan.getBookId());
                reservationService.promoteNextInQueue(conn, loan.getBookId());

                conn.commit();
                return fine;
            } catch (LibraryException | SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
}
