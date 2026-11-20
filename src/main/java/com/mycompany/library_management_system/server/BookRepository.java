package com.mycompany.library_management_system.server;

import com.mycompany.library_management_system.model.Book;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.Normalizer;
import java.time.Year;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** SQLite belongs to the server only. Every operation owns and closes its connection. */
public final class BookRepository {
    private final String jdbcUrl;
    private final Path database;

    public BookRepository(Path database) throws IOException, SQLException {
        Path absolute = database.toAbsolutePath().normalize();
        if (absolute.getParent() != null) {
            Files.createDirectories(absolute.getParent());
        }
        jdbcUrl = jdbcUrl(absolute);
        this.database = absolute;
        initialize();
    }

    private Connection connect() throws SQLException {
        return open(jdbcUrl);
    }

    static String jdbcUrl(Path database) {
        return "jdbc:sqlite:" + database.toAbsolutePath().normalize();
    }

    Path databasePath() { return database; }

    static Connection open(String jdbcUrl) throws SQLException {
        Connection connection = DriverManager.getConnection(jdbcUrl);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA busy_timeout = 5000");
            statement.execute("PRAGMA foreign_keys = ON");
        } catch (SQLException ex) {
            connection.close();
            throw ex;
        }
        return connection;
    }

    private void initialize() throws SQLException, IOException {
        try (Connection connection = connect(); Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA journal_mode = WAL");
            statement.execute("BEGIN IMMEDIATE");
            try {
                if (!tableExists(connection, "Sach")) {
                    applySchema(statement, "/database.sql");
                } else if (!hasVersionColumn(statement)) {
                    statement.executeUpdate("ALTER TABLE Sach ADD COLUMN Version INTEGER NOT NULL DEFAULT 0");
                }
                boolean newAccountTable = !tableExists(connection, "TaiKhoan");
                applySchema(statement, "/advanced.sql");
                if (!newAccountTable && !hasAccountRoleColumn(statement)) {
                    statement.executeUpdate("ALTER TABLE TaiKhoan ADD COLUMN Role TEXT NOT NULL DEFAULT 'READER' CHECK (Role IN ('ADMIN', 'READER'))");
                    statement.executeUpdate("UPDATE TaiKhoan SET Role='ADMIN' WHERE TenDangNhap='admin' COLLATE NOCASE");
                }
                if (newAccountTable) {
                    AccountRepository.seedDemoAccounts(connection);
                }
                statement.execute("COMMIT");
            } catch (SQLException | IOException | RuntimeException ex) {
                try { statement.execute("ROLLBACK"); }
                catch (SQLException rollbackFailure) { ex.addSuppressed(rollbackFailure); }
                throw ex;
            }
        }
    }

    private static boolean tableExists(Connection connection, String name) throws SQLException {
        try (PreparedStatement check = connection.prepareStatement(
                "SELECT 1 FROM sqlite_master WHERE type='table' AND name=? COLLATE NOCASE")) {
            check.setString(1, name);
            try (ResultSet result = check.executeQuery()) { return result.next(); }
        }
    }

    private static boolean hasVersionColumn(Statement statement) throws SQLException {
        try (ResultSet columns = statement.executeQuery("PRAGMA table_info(Sach)")) {
            while (columns.next()) {
                if ("Version".equalsIgnoreCase(columns.getString("name"))) { return true; }
            }
            return false;
        }
    }

    private static boolean hasAccountRoleColumn(Statement statement) throws SQLException {
        try (ResultSet columns = statement.executeQuery("PRAGMA table_info(TaiKhoan)")) {
            while (columns.next()) {
                if ("Role".equalsIgnoreCase(columns.getString("name"))) { return true; }
            }
            return false;
        }
    }

    private static void applySchema(Statement statement, String resource) throws IOException, SQLException {
        try (InputStream input = BookRepository.class.getResourceAsStream(resource)) {
            if (input == null) { throw new IOException("Không tìm thấy tài nguyên " + resource + "."); }
            String script = new String(input.readAllBytes(), StandardCharsets.UTF_8).replaceAll("(?m)^\\s*--.*$", "");
            for (String sql : script.split(";")) {
                if (!sql.isBlank()) { statement.executeUpdate(sql); }
            }
        }
    }

    static void rollback(Connection connection, Exception cause) {
        try { connection.rollback(); }
        catch (SQLException ex) { cause.addSuppressed(ex); }
    }

    public List<Book> list() throws SQLException {
        try (Connection connection = connect();
                PreparedStatement statement = connection.prepareStatement("SELECT * FROM Sach ORDER BY MaSach");
                ResultSet result = statement.executeQuery()) {
            List<Book> books = new ArrayList<>();
            while (result.next()) { books.add(readBook(result)); }
            return books;
        }
    }

    /** Vietnamese search is case- and accent-insensitive; SQL LIKE alone does not offer this in SQLite. */
    public List<Book> search(String query) throws SQLException, LibraryException {
        if (query == null || query.strip().isEmpty()) { return list(); }
        if (query.length() > 100) {
            throw new LibraryException("VALIDATION", "Từ khóa tìm kiếm tối đa 100 ký tự.");
        }
        String normalized = normalize(query.strip());
        return list().stream().filter(book -> normalize(book.code()).contains(normalized)
                || normalize(book.title()).contains(normalized)).toList();
    }

    public Book add(Book input) throws SQLException, LibraryException {
        Book book = validate(input);
        try (Connection connection = connect(); PreparedStatement statement = connection.prepareStatement(
                "INSERT INTO Sach (MaSach, TenSach, TacGia, NamXB, SoLuong, Version) VALUES (?, ?, ?, ?, ?, 0)")) {
            bind(statement, book);
            try {
                statement.executeUpdate();
            } catch (SQLException ex) {
                if (ex.getErrorCode() == 19) {
                    throw new LibraryException("DUPLICATE", "Mã sách '" + book.code() + "' đã tồn tại.");
                }
                throw ex;
            }
            return new Book(book.code(), book.title(), book.author(), book.year(), book.quantity(), 0);
        }
    }

    public Book update(Book input) throws SQLException, LibraryException {
        Book book = validate(input);
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE Sach SET TenSach=?, TacGia=?, NamXB=?, SoLuong=?, Version=Version+1 "
                        + "WHERE MaSach=? AND Version=?")) {
                    statement.setString(1, book.title());
                    statement.setString(2, book.author());
                    statement.setInt(3, book.year());
                    statement.setInt(4, book.quantity());
                    statement.setString(5, book.code());
                    statement.setLong(6, book.version());
                    if (statement.executeUpdate() == 0) { throwMissingOrConflict(connection, book.code()); }
                }
                Book updated = find(connection, book.code());
                connection.commit();
                return updated;
            } catch (SQLException | LibraryException ex) {
                rollback(connection, ex);
                throw ex;
            }
        }
    }

    public void delete(String inputCode, Long version) throws SQLException, LibraryException {
        String code = validateText(inputCode, "Mã sách", 10);
        if (version == null || version < 0) {
            throw new LibraryException("VALIDATION", "Cần phiên bản sách hợp lệ để xóa. Hãy tải lại danh sách.");
        }
        try (Connection connection = connect()) {
            connection.setAutoCommit(false);
            try (PreparedStatement statement = connection.prepareStatement("""
                    DELETE FROM Sach WHERE MaSach=? AND Version=?
                    AND NOT EXISTS (SELECT 1 FROM PhieuMuon WHERE MaSach=Sach.MaSach AND TraLuc IS NULL)
                    """)) {
                statement.setString(1, code);
                statement.setLong(2, version);
                if (statement.executeUpdate() == 0) {
                    if (LoanRepository.hasActiveLoan(connection, code)) {
                        throw new LibraryException("ACTIVE_LOANS", "Không thể xóa sách đang có phiếu mượn chưa trả.");
                    }
                    throwMissingOrConflict(connection, code);
                }
                connection.commit();
            } catch (SQLException | LibraryException ex) {
                rollback(connection, ex);
                throw ex;
            }
        }
    }

    private static void throwMissingOrConflict(Connection connection, String code)
            throws SQLException, LibraryException {
        if (find(connection, code) == null) {
            throw new LibraryException("NOT_FOUND", "Không tìm thấy sách có mã '" + code + "'.");
        }
        throw new LibraryException("CONFLICT", "Sách đã được một client khác thay đổi. Hãy tải lại danh sách trước khi lưu.");
    }

    private static Book find(Connection connection, String code) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("SELECT * FROM Sach WHERE MaSach=?")) {
            statement.setString(1, code);
            try (ResultSet result = statement.executeQuery()) {
                return result.next() ? readBook(result) : null;
            }
        }
    }

    private static Book readBook(ResultSet result) throws SQLException {
        return new Book(result.getString("MaSach"), result.getString("TenSach"), result.getString("TacGia"),
                result.getInt("NamXB"), result.getInt("SoLuong"), result.getLong("Version"));
    }

    private static void bind(PreparedStatement statement, Book book) throws SQLException {
        statement.setString(1, book.code());
        statement.setString(2, book.title());
        statement.setString(3, book.author());
        statement.setInt(4, book.year());
        statement.setInt(5, book.quantity());
    }

    private static Book validate(Book book) throws LibraryException {
        if (book == null) { throw new LibraryException("VALIDATION", "Chưa có dữ liệu sách."); }
        String code = validateText(book.code(), "Mã sách", 10);
        String title = validateText(book.title(), "Tên sách", 100);
        String author = validateText(book.author(), "Tác giả", 100);
        if (book.year() < 1 || book.year() > Year.now().getValue() + 1) {
            throw new LibraryException("VALIDATION", "Năm xuất bản phải từ 1 đến " + (Year.now().getValue() + 1) + ".");
        }
        if (book.quantity() < 0) {
            throw new LibraryException("VALIDATION", "Số lượng sách không được âm.");
        }
        if (book.version() < 0) {
            throw new LibraryException("VALIDATION", "Phiên bản sách không hợp lệ.");
        }
        return new Book(code, title, author, book.year(), book.quantity(), book.version());
    }

    static String validateText(String text, String label, int maxLength) throws LibraryException {
        if (text == null || text.strip().isEmpty() || text.strip().length() > maxLength
                || text.chars().anyMatch(Character::isISOControl)) {
            throw new LibraryException("VALIDATION", label + " phải có từ 1 đến " + maxLength + " ký tự, không chứa ký tự điều khiển.");
        }
        return text.strip();
    }

    private static String normalize(String text) {
        return Normalizer.normalize(text.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").replace('đ', 'd');
    }
}
