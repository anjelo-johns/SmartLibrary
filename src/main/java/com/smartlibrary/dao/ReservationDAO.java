package com.smartlibrary.dao;

import com.smartlibrary.model.Reservation;
import com.smartlibrary.util.DBConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {

    private static final String SELECT_BASE =
            "SELECT r.*, b.title AS book_title, u.full_name AS student_name "
          + "FROM reservations r JOIN books b ON r.book_id = b.id JOIN users u ON r.student_id = u.id";

    public boolean hasActiveReservation(int studentId, int bookId) throws SQLException {
        String sql = "SELECT id FROM reservations WHERE student_id=? AND book_id=? AND status IN ('WAITING','READY')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int countQueueForBook(int bookId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE book_id=? AND status IN ('WAITING','READY')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int create(int studentId, int bookId, Date reservationDate, int queuePosition) throws SQLException {
        String sql = "INSERT INTO reservations (book_id, student_id, reservation_date, queue_position, status) "
                + "VALUES (?,?,?,?,'WAITING')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, bookId);
            ps.setInt(2, studentId);
            ps.setDate(3, reservationDate);
            ps.setInt(4, queuePosition);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public List<Reservation> getByStudent(int studentId) throws SQLException {
        String sql = SELECT_BASE + " WHERE r.student_id=? AND r.status IN ('WAITING','READY') ORDER BY r.reservation_date";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Reservation> getAllActive() throws SQLException {
        String sql = SELECT_BASE + " WHERE r.status IN ('WAITING','READY') ORDER BY r.book_id, r.queue_position";
        List<Reservation> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) list.add(map(rs));
        }
        return list;
    }

    public Reservation findById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE r.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void cancel(int reservationId, int studentId) throws SQLException {
        String sql = "UPDATE reservations SET status='CANCELLED' WHERE id=? AND student_id=?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reservationId);
            ps.setInt(2, studentId);
            ps.executeUpdate();
        }
    }

    /** Returns the earliest WAITING reservation for a book, if any — call inside a transaction. */
    public Reservation getNextWaiting(Connection conn, int bookId) throws SQLException {
        String sql = SELECT_BASE + " WHERE r.book_id=? AND r.status='WAITING' ORDER BY r.queue_position LIMIT 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public void markReady(Connection conn, int reservationId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE reservations SET status='READY' WHERE id=?")) {
            ps.setInt(1, reservationId);
            ps.executeUpdate();
        }
    }

    public void markFulfilled(Connection conn, int reservationId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("UPDATE reservations SET status='FULFILLED' WHERE id=?")) {
            ps.setInt(1, reservationId);
            ps.executeUpdate();
        }
    }

    public void markFulfilled(int reservationId) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            markFulfilled(conn, reservationId);
        }
    }

    public Reservation findActiveReservation(Connection conn, int studentId, int bookId) throws SQLException {
        String sql = SELECT_BASE + " WHERE r.student_id=? AND r.book_id=? AND r.status IN ('WAITING','READY') LIMIT 1 FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            ps.setInt(2, bookId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public int countReadyReservationsForOtherStudents(Connection conn, int bookId, int currentStudentId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE book_id=? AND status='READY' AND student_id != ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, currentStudentId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    public int countActive() throws SQLException {
        String sql = "SELECT COUNT(*) FROM reservations WHERE status IN ('WAITING','READY')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Reservation map(ResultSet rs) throws SQLException {
        Reservation r = new Reservation();
        r.setId(rs.getInt("id"));
        r.setBookId(rs.getInt("book_id"));
        r.setBookTitle(rs.getString("book_title"));
        r.setStudentId(rs.getInt("student_id"));
        r.setStudentName(rs.getString("student_name"));
        r.setReservationDate(rs.getDate("reservation_date"));
        r.setQueuePosition(rs.getInt("queue_position"));
        r.setStatus(rs.getString("status"));
        return r;
    }
}
