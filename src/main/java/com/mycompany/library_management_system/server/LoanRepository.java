package com.mycompany.library_management_system.server;

import com.mycompany.library_management_system.model.Loan;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/** Loan and stock updates share a transaction on the server's SQLite database. */
final class LoanRepository {
    private final String jdbcUrl;

    LoanRepository(Path database) {
        jdbcUrl = BookRepository.jdbcUrl(database);
    }

    long borrow(String username, String inputCode) throws SQLException, LibraryException {
        String code = BookRepository.validateText(inputCode, "Mã sách", 10);
        try (Connection connection = BookRepository.open(jdbcUrl)) {
            connection.setAutoCommit(false);
            try {
                // Acquire the write lock before reading, so simultaneous requests serialize at the stock check.
                try (PreparedStatement stock = connection.prepareStatement(
                        "UPDATE Sach SET SoLuong=SoLuong-1, Version=Version+1 WHERE MaSach=? AND SoLuong>0")) {
                    stock.setString(1, code);
                    if (stock.executeUpdate() == 0) {
                        if (!bookExists(connection, code)) {
                            throw new LibraryException("NOT_FOUND", "Không tìm thấy sách có mã '" + code + "'.");
                        }
                        throw new LibraryException("OUT_OF_STOCK", "Sách này đã hết bản có thể mượn.");
                    }
                }

                long id;
                try (PreparedStatement insert = connection.prepareStatement("""
                        INSERT INTO PhieuMuon (TenDangNhap, MaSach, TenSach, MuonLuc)
                        SELECT ?, MaSach, TenSach, ? FROM Sach WHERE MaSach=? RETURNING Id
                        """)) {
                    insert.setString(1, username);
                    insert.setString(2, Instant.now().toString());
                    insert.setString(3, code);
                    try (ResultSet result = insert.executeQuery()) {
                        if (!result.next()) throw new SQLException("Không thể tạo phiếu mượn.");
                        id = result.getLong("Id");
                    }
                }
                connection.commit();
                return id;
            } catch (SQLException | LibraryException ex) {
                BookRepository.rollback(connection, ex);
                throw ex;
            }
        }
    }

    String returnBook(String username, long loanId) throws SQLException, LibraryException {
        if (loanId <= 0) throw new LibraryException("VALIDATION", "Mã phiếu mượn không hợp lệ.");
        try (Connection connection = BookRepository.open(jdbcUrl)) {
            connection.setAutoCommit(false);
            try {
                String code = markReturned(connection, username, loanId);
                try (PreparedStatement stock = connection.prepareStatement("""
                        UPDATE Sach SET SoLuong=SoLuong+1, Version=Version+1 WHERE MaSach=? AND SoLuong<?
                        """)) {
                    stock.setString(1, code);
                    stock.setInt(2, Integer.MAX_VALUE);
                    if (stock.executeUpdate() == 0) {
                        if (!bookExists(connection, code)) throw new SQLException("Không tìm thấy sách của phiếu mượn.");
                        throw new LibraryException("STOCK_LIMIT", "Số lượng sách đã đạt giới hạn. Giảm số lượng trước khi trả.");
                    }
                }
                connection.commit();
                return code;
            } catch (SQLException | LibraryException ex) {
                BookRepository.rollback(connection, ex);
                throw ex;
            }
        }
    }

    List<Loan> list(String username) throws SQLException {
        try (Connection connection = BookRepository.open(jdbcUrl);
                PreparedStatement statement = connection.prepareStatement("""
                        SELECT Id, TenDangNhap, MaSach, TenSach, MuonLuc, TraLuc
                        FROM PhieuMuon WHERE TenDangNhap=? ORDER BY Id DESC
                        """)) {
            statement.setString(1, username);
            try (ResultSet result = statement.executeQuery()) {
                List<Loan> loans = new ArrayList<>();
                while (result.next()) {
                    loans.add(new Loan(result.getLong("Id"), result.getString("TenDangNhap"),
                            result.getString("MaSach"), result.getString("TenSach"),
                            result.getString("MuonLuc"), result.getString("TraLuc")));
                }
                return loans;
            }
        }
    }

    static boolean hasActiveLoan(Connection connection, String code) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM PhieuMuon WHERE MaSach=? AND TraLuc IS NULL LIMIT 1")) {
            statement.setString(1, code);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static boolean bookExists(Connection connection, String code) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM Sach WHERE MaSach=?")) {
            statement.setString(1, code);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        }
    }

    private static String markReturned(Connection connection, String username, long loanId)
            throws SQLException, LibraryException {
        try (PreparedStatement statement = connection.prepareStatement("""
                UPDATE PhieuMuon SET TraLuc=?
                WHERE Id=? AND TenDangNhap=? AND TraLuc IS NULL RETURNING MaSach
                """)) {
            statement.setString(1, Instant.now().toString());
            statement.setLong(2, loanId);
            statement.setString(3, username);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) return result.getString("MaSach");
            }
        }

        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT 1 FROM PhieuMuon WHERE Id=? AND TenDangNhap=?")) {
            statement.setLong(1, loanId);
            statement.setString(2, username);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) throw new LibraryException("ALREADY_RETURNED", "Phiếu này đã được trả sách.");
            }
        }
        throw new LibraryException("NOT_FOUND", "Không tìm thấy phiếu mượn trong tài khoản của bạn.");
    }
}
