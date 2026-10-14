package com.smartlibrary.dao;

import com.smartlibrary.model.FinePayment;
import com.smartlibrary.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FineDAO {

    public int recordPayment(int loanId, int studentId, double amount, int collectedBy) throws SQLException {
        String sql = "INSERT INTO fine_payments (loan_id, student_id, amount, collected_by) VALUES (?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, loanId);
            ps.setInt(2, studentId);
            ps.setDouble(3, amount);
            ps.setInt(4, collectedBy);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public List<FinePayment> getPaymentsByStudent(int studentId) throws SQLException {
        String sql = "SELECT * FROM fine_payments WHERE student_id=? ORDER BY payment_date DESC";
        List<FinePayment> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, studentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public double getTotalCollected() throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount),0) FROM fine_payments";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getDouble(1) : 0;
        }
    }

    public double getTotalPaidForLoan(int loanId) throws SQLException {
        String sql = "SELECT COALESCE(SUM(amount), 0) FROM fine_payments WHERE loan_id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, loanId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getDouble(1) : 0.0;
            }
        }
    }

    private FinePayment map(ResultSet rs) throws SQLException {
        FinePayment p = new FinePayment();
        p.setId(rs.getInt("id"));
        p.setLoanId(rs.getInt("loan_id"));
        p.setStudentId(rs.getInt("student_id"));
        p.setAmount(rs.getDouble("amount"));
        p.setPaymentDate(rs.getTimestamp("payment_date"));
        p.setCollectedBy(rs.getInt("collected_by"));
        return p;
    }
}
