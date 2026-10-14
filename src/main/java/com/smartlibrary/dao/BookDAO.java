package com.smartlibrary.dao;

import com.smartlibrary.exception.LibraryException;
import com.smartlibrary.model.Book;
import com.smartlibrary.util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    private static final String SELECT_BASE =
            "SELECT b.*, c.category_name FROM books b JOIN categories c ON b.category_id = c.id";

    public List<Book> search(String query, Integer categoryId, boolean onlyAvailable) throws SQLException {
        StringBuilder sql = new StringBuilder(SELECT_BASE + " WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (query != null && !query.trim().isEmpty()) {
            sql.append(" AND (b.title LIKE ? OR b.author LIKE ? OR b.isbn LIKE ? OR c.category_name LIKE ?)");
            String like = "%" + query.trim() + "%";
            params.add(like); params.add(like); params.add(like); params.add(like);
        }
        if (categoryId != null && categoryId > 0) {
            sql.append(" AND b.category_id = ?");
            params.add(categoryId);
        }
        if (onlyAvailable) {
            sql.append(" AND b.available_copies > 0");
        }
        sql.append(" ORDER BY b.title");

        List<Book> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(map(rs));
            }
        }
        return list;
    }

    public List<Book> getAll() throws SQLException {
        return search(null, null, false);
    }

    public Book getById(int id) throws SQLException {
        String sql = SELECT_BASE + " WHERE b.id = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    /** Locks the book row for update — used inside a transaction only. */
    public Book getByIdForUpdate(Connection conn, int id) throws SQLException {
        String sql = "SELECT b.*, c.category_name FROM books b JOIN categories c ON b.category_id = c.id "
                + "WHERE b.id = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    public boolean existsByIsbn(String isbn) throws SQLException {
        if (isbn == null || isbn.trim().isEmpty()) return false;
        String sql = "SELECT id FROM books WHERE isbn = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, isbn);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int add(Book b) throws SQLException {
        String sql = "INSERT INTO books (title, author, isbn, category_id, publisher, publication_year, "
                + "total_copies, available_copies, description) VALUES (?,?,?,?,?,?,?,?,?)";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, b.getTitle());
            ps.setString(2, b.getAuthor());
            ps.setString(3, (b.getIsbn() == null || b.getIsbn().isEmpty()) ? null : b.getIsbn());
            ps.setInt(4, b.getCategoryId());
            ps.setString(5, b.getPublisher());
            if (b.getPublicationYear() != null) ps.setInt(6, b.getPublicationYear()); else ps.setNull(6, java.sql.Types.INTEGER);
            ps.setInt(7, b.getTotalCopies());
            ps.setInt(8, b.getTotalCopies()); // all copies start available
            ps.setString(9, b.getDescription());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    /**
     * Updates a book's details. Rejects the change if the new total
     * copy count would drop below the number of copies currently
     * issued (active loans) — the available_copies figure is adjusted
     * to keep issuedCopies constant.
     */
    public void update(Book b) throws SQLException, LibraryException {
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                Book existing = getByIdForUpdate(conn, b.getId());
                if (existing == null) {
                    throw new LibraryException("Book not found.");
                }
                int issuedCopies = existing.getTotalCopies() - existing.getAvailableCopies();
                if (b.getTotalCopies() < issuedCopies) {
                    throw new LibraryException("Cannot set total copies to " + b.getTotalCopies()
                            + " — " + issuedCopies + " copies are currently issued to students.");
                }
                int newAvailable = b.getTotalCopies() - issuedCopies;
                String sql = "UPDATE books SET title=?, author=?, isbn=?, category_id=?, publisher=?, "
                        + "publication_year=?, total_copies=?, available_copies=?, description=? WHERE id=?";
                try (PreparedStatement ps = conn.prepareStatement(sql)) {
                    ps.setString(1, b.getTitle());
                    ps.setString(2, b.getAuthor());
                    ps.setString(3, (b.getIsbn() == null || b.getIsbn().isEmpty()) ? null : b.getIsbn());
                    ps.setInt(4, b.getCategoryId());
                    ps.setString(5, b.getPublisher());
                    if (b.getPublicationYear() != null) ps.setInt(6, b.getPublicationYear()); else ps.setNull(6, java.sql.Types.INTEGER);
                    ps.setInt(7, b.getTotalCopies());
                    ps.setInt(8, newAvailable);
                    ps.setString(9, b.getDescription());
                    ps.setInt(10, b.getId());
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (LibraryException | SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }

    public void delete(int bookId) throws SQLException, LibraryException {
        try (Connection conn = DBConnection.getConnection()) {
            int activeLoans = countRows(conn, "SELECT COUNT(*) FROM loans WHERE book_id=? AND status IN ('ACTIVE','OVERDUE')", bookId);
            if (activeLoans > 0) {
                throw new LibraryException("Cannot delete — this book has " + activeLoans + " active loan(s).");
            }
            int activeReservations = countRows(conn, "SELECT COUNT(*) FROM reservations WHERE book_id=? AND status IN ('WAITING','READY')", bookId);
            if (activeReservations > 0) {
                throw new LibraryException("Cannot delete — this book has " + activeReservations + " active reservation(s).");
            }
            int totalLoans = countRows(conn, "SELECT COUNT(*) FROM loans WHERE book_id=?", bookId);
            if (totalLoans > 0) {
                throw new LibraryException("Cannot delete — this book has " + totalLoans + " historical borrowing record(s) on file.");
            }
            int totalReservations = countRows(conn, "SELECT COUNT(*) FROM reservations WHERE book_id=?", bookId);
            if (totalReservations > 0) {
                throw new LibraryException("Cannot delete — this book has " + totalReservations + " past reservation record(s) on file.");
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM books WHERE id = ?")) {
                ps.setInt(1, bookId);
                ps.executeUpdate();
            }
        }
    }

    private int countRows(Connection conn, String sql, int param) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, param);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) : 0;
            }
        }
    }

    /** Decrements available_copies by 1 — call only inside a transaction. */
    public void decrementAvailable(Connection conn, int bookId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE books SET available_copies = available_copies - 1 WHERE id = ? AND available_copies > 0")) {
            ps.setInt(1, bookId);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("No available copies to issue.");
        }
    }

    /** Increments available_copies by 1 — call only inside a transaction. */
    public void incrementAvailable(Connection conn, int bookId) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE books SET available_copies = available_copies + 1 WHERE id = ?")) {
            ps.setInt(1, bookId);
            ps.executeUpdate();
        }
    }

    public int countTotalBooks() throws SQLException {
        return scalar("SELECT COUNT(*) FROM books");
    }

    public int countTotalCopies() throws SQLException {
        return scalar("SELECT COALESCE(SUM(total_copies),0) FROM books");
    }

    public int countAvailableCopies() throws SQLException {
        return scalar("SELECT COALESCE(SUM(available_copies),0) FROM books");
    }

    private int scalar(String sql) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            return rs.next() ? rs.getInt(1) : 0;
        }
    }

    private Book map(ResultSet rs) throws SQLException {
        Book b = new Book();
        b.setId(rs.getInt("id"));
        b.setTitle(rs.getString("title"));
        b.setAuthor(rs.getString("author"));
        b.setIsbn(rs.getString("isbn"));
        b.setCategoryId(rs.getInt("category_id"));
        b.setCategoryName(rs.getString("category_name"));
        b.setPublisher(rs.getString("publisher"));
        int year = rs.getInt("publication_year");
        b.setPublicationYear(rs.wasNull() ? null : year);
        b.setTotalCopies(rs.getInt("total_copies"));
        b.setAvailableCopies(rs.getInt("available_copies"));
        b.setDescription(rs.getString("description"));
        b.setCreatedAt(rs.getTimestamp("created_at"));
        Timestamp updated = rs.getTimestamp("updated_at");
        b.setUpdatedAt(updated);
        return b;
    }
}
