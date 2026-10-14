package com.smartlibrary.listener;

import com.smartlibrary.util.DBConnection;
import com.smartlibrary.util.PasswordUtil;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Runs once when the application starts. Seeds the librarian account
 * and a couple of demo students with properly hashed passwords (SQL
 * INSERT statements can't safely contain a real PBKDF2 hash without
 * duplicating this exact algorithm, so the accounts are created here
 * instead of in smart_library.sql). It also seeds a small set of
 * sample loans and a sample reservation so the dashboards and fines
 * report have something to show immediately.
 *
 * Idempotent: if the librarian account already exists, this listener
 * does nothing, so it's always safe for Tomcat to run on every restart.
 */
@WebListener
public class AppInitListener implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        try (Connection conn = DBConnection.getConnection()) {
            if (librarianAlreadyExists(conn)) {
                sce.getServletContext().log("SmartLibrary: sample data already present, skipping seed.");
                return;
            }
            sce.getServletContext().log("SmartLibrary: seeding librarian, demo students, and sample data...");

            int librarianId = insertUser(conn,
                    "Library Staff", null, "librarian", "librarian@smartlibrary.local", "9999999999",
                    PasswordUtil.hash("library123"), null, null, "LIBRARIAN");

            int student1Id = insertUser(conn,
                    "Aditi Sharma", "STU001", null, "aditi.sharma@example.com", "9876543210",
                    PasswordUtil.hash("Student@123"), "Computer Science", "3rd Year", "STUDENT");

            int student2Id = insertUser(conn,
                    "Rohan Verma", "STU002", null, "rohan.verma@example.com", "9123456789",
                    PasswordUtil.hash("Student@123"), "Electronics", "2nd Year", "STUDENT");

            Integer dbConceptsId = findBookIdByTitle(conn, "Database System Concepts");
            Integer alchemistId = findBookIdByTitle(conn, "The Alchemist");
            Integer structuralId = findBookIdByTitle(conn, "Structural Analysis");

            if (dbConceptsId != null) {
                // Loan A: currently overdue, so the fines report has an example on first login.
                insertLoan(conn, dbConceptsId, student1Id, librarianId, -20, -6, null, 0, false, null);
                decrementAvailable(conn, dbConceptsId);
            }

            if (alchemistId != null) {
                // Loan B: already returned, one day late — demonstrates borrowing history + a paid fine.
                int loanBId = insertLoan(conn, alchemistId, student2Id, null, -30, -16, -15, 5.00, true, null);
                recordFinePayment(conn, loanBId, student2Id, 5.00, librarianId);
            }

            if (structuralId != null) {
                // Loan C: takes the single available copy so a reservation can be demonstrated.
                insertLoan(conn, structuralId, student1Id, librarianId, 0, 14, null, 0, false, null);
                decrementAvailable(conn, structuralId);
                insertReservation(conn, structuralId, student2Id, 1);
            }

            sce.getServletContext().log("SmartLibrary: seed complete. Librarian login: librarian / library123. "
                    + "Demo student logins: STU001 / Student@123 and STU002 / Student@123.");
        } catch (SQLException e) {
            sce.getServletContext().log("SmartLibrary: failed to seed sample data — " + e.getMessage(), e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // nothing to clean up
    }

    private boolean librarianAlreadyExists(Connection conn) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM users WHERE username = 'librarian'");
             ResultSet rs = ps.executeQuery()) {
            return rs.next();
        }
    }

    private int insertUser(Connection conn, String fullName, String regNo, String username, String email,
                            String phone, String passwordHash, String department, String yearSem, String role)
            throws SQLException {
        String sql = "INSERT INTO users (full_name, register_number, username, email, phone, password_hash, "
                + "department, year_sem, role, status) VALUES (?,?,?,?,?,?,?,?,?, 'ACTIVE')";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, fullName);
            ps.setString(2, regNo);
            ps.setString(3, username);
            ps.setString(4, email);
            ps.setString(5, phone);
            ps.setString(6, passwordHash);
            ps.setString(7, department);
            ps.setString(8, yearSem);
            ps.setString(9, role);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private Integer findBookIdByTitle(Connection conn, String title) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("SELECT id FROM books WHERE title = ? LIMIT 1")) {
            ps.setString(1, title);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : null;
            }
        }
    }

    private int insertLoan(Connection conn, int bookId, int studentId, Integer issuedBy,
                            int issueOffsetDays, int dueOffsetDays, Integer returnOffsetDays,
                            double fineAmount, boolean finePaid, Integer returnedBy) throws SQLException {
        String status = returnOffsetDays != null ? "RETURNED" : (dueOffsetDays < 0 ? "OVERDUE" : "ACTIVE");
        String sql = "INSERT INTO loans (book_id, student_id, issued_by, issue_date, due_date, return_date, "
                + "status, fine_amount, fine_paid, returned_by) VALUES "
                + "(?,?,?, DATE_ADD(CURDATE(), INTERVAL ? DAY), DATE_ADD(CURDATE(), INTERVAL ? DAY), "
                + (returnOffsetDays != null ? "DATE_ADD(CURDATE(), INTERVAL ? DAY)" : "NULL") + ", ?,?,?,?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            int idx = 1;
            ps.setInt(idx++, bookId);
            ps.setInt(idx++, studentId);
            if (issuedBy != null) ps.setInt(idx++, issuedBy); else ps.setNull(idx++, java.sql.Types.INTEGER);
            ps.setInt(idx++, issueOffsetDays);
            ps.setInt(idx++, dueOffsetDays);
            if (returnOffsetDays != null) ps.setInt(idx++, returnOffsetDays);
            ps.setString(idx++, status);
            ps.setDouble(idx++, fineAmount);
            ps.setBoolean(idx++, finePaid);
            if (returnedBy != null) ps.setInt(idx++, returnedBy); else ps.setNull(idx++, java.sql.Types.INTEGER);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                keys.next();
                return keys.getInt(1);
            }
        }
    }

    private void decrementAvailable(Connection conn, int bookId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE books SET available_copies = available_copies - 1 WHERE id = ? AND available_copies > 0")) {
            ps.setInt(1, bookId);
            ps.executeUpdate();
        }
    }

    private void insertReservation(Connection conn, int bookId, int studentId, int queuePosition) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO reservations (book_id, student_id, reservation_date, queue_position, status) "
                        + "VALUES (?,?,CURDATE(),?,'WAITING')")) {
            ps.setInt(1, bookId);
            ps.setInt(2, studentId);
            ps.setInt(3, queuePosition);
            ps.executeUpdate();
        }
    }

    private void recordFinePayment(Connection conn, int loanId, int studentId, double amount, int collectedBy) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO fine_payments (loan_id, student_id, amount, collected_by) VALUES (?,?,?,?)")) {
            ps.setInt(1, loanId);
            ps.setInt(2, studentId);
            ps.setDouble(3, amount);
            ps.setInt(4, collectedBy);
            ps.executeUpdate();
        }
    }
}
