package com.mycompany.library_management_system.server;

import java.nio.file.Path;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/** Verifies passwords from the account table created by BookRepository. */
final class AccountRepository {
    record AuthenticatedUser(String username, String role) { }

    private static final int ITERATIONS = 600_000;
    private static final int HASH_BYTES = 32;
    private static final int SALT_BYTES = 16;
    private final String jdbcUrl;

    AccountRepository(Path database) {
        jdbcUrl = BookRepository.jdbcUrl(database);
    }

    AuthenticatedUser authenticate(String username, String password) throws SQLException {
        if (username == null || username.isBlank() || username.length() > 64
                || password == null || password.isEmpty() || password.length() > 256) {
            return null;
        }

        String account = null;
        String role = null;
        byte[] salt = new byte[SALT_BYTES];
        byte[] storedHash = new byte[HASH_BYTES];
        int iterations = ITERATIONS;
        try (Connection connection = BookRepository.open(jdbcUrl);
                PreparedStatement statement = connection.prepareStatement(
                        "SELECT TenDangNhap, Salt, MatKhauBam, SoVongLap, Role FROM TaiKhoan WHERE TenDangNhap=?")) {
            statement.setString(1, username.strip());
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    account = result.getString("TenDangNhap");
                    role = result.getString("Role");
                    salt = result.getBytes("Salt");
                    storedHash = result.getBytes("MatKhauBam");
                    iterations = result.getInt("SoVongLap");
                }
            }
        }
        // Work on a dummy hash as well, so unknown accounts follow the same verification path.
        byte[] calculatedHash = hash(password, salt, iterations);
        try {
            return MessageDigest.isEqual(storedHash, calculatedHash) && account != null && role != null
                    ? new AuthenticatedUser(account, role) : null;
        } finally {
            Arrays.fill(calculatedHash, (byte) 0);
        }
    }

    static void seedDemoAccounts(Connection connection) throws SQLException {
        SecureRandom random = new SecureRandom();
        try (PreparedStatement insert = connection.prepareStatement(
                "INSERT OR IGNORE INTO TaiKhoan (TenDangNhap, Salt, MatKhauBam, SoVongLap, Role) VALUES (?, ?, ?, ?, ?)")) {
            insertAccount(insert, random, "admin", "admin123", "ADMIN");
            insertAccount(insert, random, "bandoc", "bandoc123", "READER");
        }
    }

    private static void insertAccount(PreparedStatement insert, SecureRandom random, String username, String password,
            String role)
            throws SQLException {
        byte[] salt = new byte[SALT_BYTES];
        random.nextBytes(salt);
        byte[] digest = hash(password, salt, ITERATIONS);
        try {
            insert.setString(1, username);
            insert.setBytes(2, salt);
            insert.setBytes(3, digest);
            insert.setInt(4, ITERATIONS);
            insert.setString(5, role);
            insert.executeUpdate();
        } finally {
            Arrays.fill(digest, (byte) 0);
        }
    }

    private static byte[] hash(String password, byte[] salt, int iterations) throws SQLException {
        char[] passwordChars = password.toCharArray();
        PBEKeySpec spec = new PBEKeySpec(passwordChars, salt, iterations, HASH_BYTES * Byte.SIZE);
        Arrays.fill(passwordChars, '\0');
        try {
            return SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256").generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException ex) {
            throw new SQLException("Không thể xử lý mật khẩu.", ex);
        } finally {
            spec.clearPassword();
        }
    }
}
