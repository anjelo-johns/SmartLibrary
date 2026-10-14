package com.smartlibrary.dao;

import com.smartlibrary.model.Loan;
import com.smartlibrary.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

public class LoanDAO {

    private static final String SELECT_BASE =
            "SELECT l.*, b.title AS book_title, u.full_name AS student_name, u.register_number AS student_reg "
          + "FROM loans l JOIN books b ON l.book_id = b.id JOIN users u ON l.student_id = u.id";

    /** Creates a new ACTIVE loan row — call only inside a transaction. */
    public int createLoan(Connection conn, Loan loan) throws SQLException {
        String sql = "INSERT INTO loans (book_id, student_id, issued_by, issue_date, due_date, status) "
                + "VALUES (?,?,?,?,?,'ACTIVE')";
        try (PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loan.getBookId());
            ps.setInt(2, loan.getStudentId());
            if (loan.getIssuedBy() != null) ps.setInt(3, loan.getIssuedBy()); else ps.setNull(3, Types.INTEGER);
            ps.setDate(4, loan.getIssueDate());
            ps.setDate(5, loan.getDueDate());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public boolean hasActiveLoanForBook(Connection conn, int studentId, int bookId) throws SQLException {
        String sql = "SELECT id FROM loans WHERE student_id=? AND book_id=? AND status IN ('ACTIVE','OVERDUE')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean hasActiveLoanForBook(int studentId, int bookId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return hasActiveLoanForBook(conn, studentId, bookId);
        }
    }

    public int countActiveLoansForStudent(Connection conn, int studentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM loans WHERE student_id=? AND status IN ('ACTIVE','OVERDUE')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public Loan findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE l.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public Loan findByIdForUpdate(Connection conn, int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE l.id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public List<Loan> getActiveLoansByStudent(int studentId) throws SQLException {
        String sql = SELECT_BASE + " WHERE l.student_id=? AND l.status IN ('ACTIVE','OVERDUE') ORDER BY l.due_date";
        return queryList(sql, studentId);
    }

    public List<Loan> getHistoryByStudent(int studentId) throws SQLException {
        String sql = SELECT_BASE + " WHERE l.student_id=? AND l.status='RETURNED' ORDER BY l.return_date DESC";
        return queryList(sql, studentId);
    }

    public List<Loan> getAllActiveLoans() throws SQLException {
        String sql = SELECT_BASE + " WHERE l.status IN ('ACTIVE','OVERDUE') ORDER BY l.due_date";
        List<Loan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Loan> getOverdueLoans() throws SQLException {
        String sql = SELECT_BASE + " WHERE l.status IN ('ACTIVE','OVERDUE') AND l.due_date < CURDATE() ORDER BY l.due_date";
        List<Loan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public List<Loan> getLoansByStudentId(int studentId) throws SQLException {
        String sql = SELECT_BASE + " WHERE l.student_id=? ORDER BY l.issue_date DESC";
        return queryList(sql, studentId);
    }

    /** Records a return — call only inside a transaction. */
    public void recordReturn(Connection conn, int loanId, Date returnDate, double fineAmount, Integer returnedBy) throws SQLException {
        String sql = "UPDATE loans SET return_date=?, status='RETURNED', fine_amount=?, returned_by=? WHERE id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, returnDate);
            ps.setDouble(2, fineAmount);
            if (returnedBy != null) ps.setInt(3, returnedBy); else ps.setNull(3, Types.INTEGER);
            ps.setInt(4, loanId);
            ps.executeUpdate();
        }
    }

    public void markFinePaid(int loanId) throws SQLException {
        String sql = "UPDATE loans SET fine_paid = TRUE WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loanId);
            ps.executeUpdate();
        }
    }

    public void updateFineAmount(int loanId, double fineAmount) throws SQLException {
        String sql = "UPDATE loans SET fine_amount = ? WHERE id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDouble(1, fineAmount);
            ps.setInt(2, loanId);
            ps.executeUpdate();
        }
    }

    public List<Loan> getUnpaidReturnedLoans() throws SQLException {
        String sql = SELECT_BASE + " WHERE l.status='RETURNED' AND l.fine_amount > 0 AND l.fine_paid = FALSE ORDER BY l.return_date DESC";
        List<Loan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    /** Flags any ACTIVE loan whose due date has passed as OVERDUE (housekeeping). */
    public void refreshOverdueStatuses() throws SQLException {
        String sql = "UPDATE loans SET status='OVERDUE' WHERE status='ACTIVE' AND due_date < CURDATE()";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    public List<Object[]> mostBorrowedBooks(int limit) throws SQLException {
        String sql = "SELECT b.title, COUNT(*) AS times_borrowed FROM loans l JOIN books b ON l.book_id=b.id "
                + "GROUP BY b.id, b.title ORDER BY times_borrowed DESC LIMIT ?";
        List<Object[]> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, limit);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new Object[]{rs.getString("title"), rs.getInt("times_borrowed")});
                }
            }
        }
        return list;
    }

    public double totalOutstandingFines() throws SQLException {
        String sql = "SELECT COALESCE(SUM(fine_amount),0) FROM loans WHERE fine_paid = FALSE AND fine_amount > 0";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    private List<Loan> queryList(String sql, int studentId) throws SQLException {
        List<Loan> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    private Loan map(ResultSet rs) throws SQLException {
        Loan l = new Loan();
        l.setId(rs.getInt("id"));
        l.setBookId(rs.getInt("book_id"));
        l.setBookTitle(rs.getString("book_title"));
        l.setStudentId(rs.getInt("student_id"));
        l.setStudentName(rs.getString("student_name"));
        l.setStudentRegNo(rs.getString("student_reg"));
        int issuedBy = rs.getInt("issued_by");
        l.setIssuedBy(rs.wasNull() ? null : issuedBy);
        l.setIssueDate(rs.getDate("issue_date"));
        l.setDueDate(rs.getDate("due_date"));
        l.setReturnDate(rs.getDate("return_date"));
        l.setStatus(rs.getString("status"));
        l.setFineAmount(rs.getDouble("fine_amount"));
        l.setFinePaid(rs.getBoolean("fine_paid"));
        int returnedBy = rs.getInt("returned_by");
        l.setReturnedBy(rs.wasNull() ? null : returnedBy);
        return l;
    }
}
