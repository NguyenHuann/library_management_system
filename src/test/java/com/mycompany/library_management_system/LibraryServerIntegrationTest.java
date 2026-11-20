package com.mycompany.library_management_system;

import com.mycompany.library_management_system.client.LibraryClient;
import com.mycompany.library_management_system.model.Book;
import com.mycompany.library_management_system.protocol.Request;
import com.mycompany.library_management_system.protocol.Response;
import com.mycompany.library_management_system.protocol.Wire;
import com.mycompany.library_management_system.protocol.LibraryNotice;
import com.mycompany.library_management_system.model.Loan;
import com.mycompany.library_management_system.server.LibraryServer;
import java.io.DataOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.List;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Year;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;
import java.util.function.BooleanSupplier;
import java.util.stream.Stream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.*;

/** These checks use real sockets and a different on-disk SQLite database for every test. */
@Timeout(15)
class LibraryServerIntegrationTest {
    @TempDir
    Path temporaryDirectory;

    private LibraryServer server;
    private LibraryClient firstClient;
    private LibraryClient secondClient;
    private LibraryClient thirdClient;
    private LibraryClient udpObserver;
    private LibraryClient udpActor;
    private Path database;
    private Path activityLog;

    @BeforeEach
    void startServerWithTwoIndependentClients() throws Exception {
        database = temporaryDirectory.resolve("library.db");
        activityLog = temporaryDirectory.resolve("activity.log");
        startServer();
        firstClient = connectClient();
        secondClient = connectClient();
        await(() -> server.getConnectedClientCount() == 2);
    }

    @AfterEach
    void closeResources() {
        if (firstClient != null) {
            firstClient.close();
        }
        if (secondClient != null) {
            secondClient.close();
        }
        if (thirdClient != null) thirdClient.close();
        if (udpObserver != null) udpObserver.close();
        if (udpActor != null) udpActor.close();
        if (server != null) {
            server.close();
        }
    }

    @Test
    void twoClientsShareCrudAndUnicodeSearchResults() throws Exception {
        Book draft = new Book("TEST001", "Lập trình mạng – Nguyễn Ánh", "Tác giả Đặng", 2024, 7, 0);
        assertSuccess(firstClient.addBook(draft));

        Book created = find(secondClient, draft.code());
        assertEquals(draft.title(), created.title());
        assertEquals(draft.author(), created.author());
        assertEquals(7, created.quantity());

        Response byTitle = secondClient.searchBooks("Nguyễn Ánh");
        assertSuccess(byTitle);
        assertTrue(byTitle.books().stream().anyMatch(book -> book.code().equals(draft.code())));

        Book changed = new Book(created.code(), "Java Socket nâng cao", "Nguyễn Văn An", 2025, 11,
                created.version());
        assertSuccess(secondClient.updateBook(changed));
        Book refreshed = find(firstClient, draft.code());
        assertEquals(changed.title(), refreshed.title());
        assertEquals(changed.author(), refreshed.author());
        assertEquals(2025, refreshed.year());
        assertEquals(11, refreshed.quantity());
        assertTrue(refreshed.version() > created.version());

        assertSuccess(firstClient.deleteBook(refreshed));
        Response afterDelete = secondClient.searchBooks(draft.code());
        assertSuccess(afterDelete);
        assertTrue(afterDelete.books().isEmpty());
    }

    @Test
    void duplicateAndMissingCodesReturnErrorsWithoutBreakingConnection() throws Exception {
        Book book = new Book("TEST002", "Một cuốn sách", "Tác giả", 2023, 4, 0);
        assertSuccess(firstClient.addBook(book));
        assertFailure(secondClient.addBook(book));
        assertFailure(firstClient.updateBook(new Book("MISSING", "Không có", "Tác giả", 2023, 1, 0)));
        assertFailure(firstClient.deleteBook(new Book("MISSING", "Không có", "Tác giả", 2023, 1, 0)));
        assertSuccess(firstClient.listBooks());
        assertEquals(4, find(secondClient, book.code()).quantity());
    }

    @Test
    void authenticationIsRequiredAndCredentialsAreCheckedOnServer() throws Exception {
        LibraryClient unauthenticated = new LibraryClient();
        try {
            unauthenticated.connect("127.0.0.1", server.getPort());
            Response blocked = unauthenticated.listBooks();
            assertFailureCode(blocked, "AUTH_REQUIRED");
            assertFailureCode(unauthenticated.addBook(new Book("NOAUTH1", "Chưa đăng nhập", "Tác giả", 2024, 1, 0)),
                    "AUTH_REQUIRED");
            assertFailureCode(unauthenticated.login("admin", "wrong-password", notice -> { }), "INVALID_CREDENTIALS");
            await(() -> readActivityLog().contains("LOGIN_FAILURE"));
            assertFalse(readActivityLog().contains("wrong-password"), "Activity logs must never store passwords");
            Response loggedIn = unauthenticated.login("admin", "admin123", notice -> { });
            assertSuccess(loggedIn);
            assertEquals("admin", loggedIn.username());
            assertFailureCode(unauthenticated.login("admin", "admin123", notice -> { }), "ALREADY_AUTHENTICATED");
            assertSuccess(unauthenticated.listBooks());
        } finally {
            unauthenticated.close();
        }
    }

    @Test
    void readerCanSearchBorrowAndReturnButCannotManageCatalogEvenWithRawCommands() throws Exception {
        thirdClient = new LibraryClient();
        thirdClient.connect("127.0.0.1", server.getPort());
        Response login = thirdClient.login("bandoc", "bandoc123", notice -> { });
        assertSuccess(login);
        assertEquals("READER", login.role(), "The server must derive this role from the account record");

        Book original = find(firstClient, "S004");
        Book unauthorized = new Book("RDR001", "Không được thêm", "Bạn đọc", 2024, 2, 0);

        // Send raw authenticated protocol commands so a disabled/hidden UI cannot bypass policy.
        assertFailureCode(thirdClient.request(Request.add(unauthorized)), "FORBIDDEN");
        assertFailureCode(thirdClient.request(Request.update(new Book(original.code(), "Không được sửa",
                original.author(), original.year(), original.quantity(), original.version()))), "FORBIDDEN");
        assertFailureCode(thirdClient.request(Request.delete(original)), "FORBIDDEN");

        assertEquals(original, find(firstClient, original.code()));
        assertTrue(thirdClient.searchBooks("RDR001").books().isEmpty());
        assertSuccess(thirdClient.searchBooks(original.code()));

        int before = find(firstClient, original.code()).quantity();
        assertSuccess(thirdClient.borrowBook(original.code()));
        assertEquals(before - 1, find(firstClient, original.code()).quantity());
        Loan loan = thirdClient.listLoans().loans().stream()
                .filter(item -> item.code().equals(original.code()) && item.returnedAt() == null)
                .findFirst().orElseThrow();
        assertSuccess(thirdClient.returnBook(loan.id()));
        assertEquals(before, find(firstClient, original.code()).quantity());
        assertTrue(thirdClient.listLoans().loans().stream()
                .anyMatch(item -> item.id() == loan.id() && item.returnedAt() != null));

        // The fixture's admin account still has complete catalog-management access.
        assertSuccess(firstClient.addBook(unauthorized));
        assertSuccess(firstClient.updateBook(find(firstClient, unauthorized.code())));
        assertSuccess(firstClient.deleteBook(find(firstClient, unauthorized.code())));
    }

    @Test
    void loansDecrementAndRestoreStockAndKeepHistory() throws Exception {
        Book before = find(firstClient, "S001");
        Response borrowed = firstClient.borrowBook("S001");
        assertSuccess(borrowed);
        assertEquals(before.quantity() - 1, find(secondClient, "S001").quantity());
        List<Loan> loans = firstClient.listLoans().loans();
        Loan active = loans.stream().filter(loan -> loan.code().equals("S001") && loan.returnedAt() == null)
                .findFirst().orElseThrow();
        assertSuccess(firstClient.returnBook(active.id()));
        assertEquals(before.quantity(), find(secondClient, "S001").quantity());
        assertFailureCode(firstClient.returnBook(active.id()), "ALREADY_RETURNED");
        assertTrue(firstClient.listLoans().loans().stream().anyMatch(loan -> loan.id() == active.id()
                && loan.returnedAt() != null));
    }

    @Test
    void emptyInventoryCannotBeBorrowed() throws Exception {
        assertSuccess(firstClient.addBook(new Book("EMPTY001", "Sách hết", "Tác giả", 2024, 0, 0)));
        assertFailureCode(firstClient.borrowBook("EMPTY001"), "OUT_OF_STOCK");
        assertEquals(0, find(secondClient, "EMPTY001").quantity());
    }

    @Test
    void activeLoanBlocksDeleteUntilReturned() throws Exception {
        assertSuccess(firstClient.borrowBook("S002"));
        Loan active = firstClient.listLoans().loans().stream()
                .filter(loan -> loan.code().equals("S002") && loan.returnedAt() == null).findFirst().orElseThrow();
        assertFailureCode(secondClient.deleteBook(find(secondClient, "S002")), "ACTIVE_LOANS");
        assertSuccess(firstClient.returnBook(active.id()));
        assertSuccess(secondClient.deleteBook(find(secondClient, "S002")));
    }

    @Test
    void loanHistoryIsPrivateToAuthenticatedAccount() throws Exception {
        thirdClient = new LibraryClient();
        thirdClient.connect("127.0.0.1", server.getPort());
        assertSuccess(thirdClient.login("bandoc", "bandoc123", notice -> { }));
        assertTrue(thirdClient.listLoans().loans().isEmpty());
        assertSuccess(firstClient.borrowBook("S003"));
        assertTrue(thirdClient.listLoans().loans().isEmpty());
        Loan active = firstClient.listLoans().loans().stream().filter(loan -> loan.code().equals("S003")
                && loan.returnedAt() == null).findFirst().orElseThrow();
        assertFailure(thirdClient.returnBook(active.id()));
        assertSuccess(firstClient.returnBook(active.id()));
    }

    @Test
    void concurrentBorrowsOfFinalCopyDoNotOversell() throws Exception {
        assertSuccess(firstClient.addBook(new Book("LAST001", "Chỉ còn một bản", "Tác giả", 2024, 1, 0)));
        ExecutorService workers = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Response> first = workers.submit(() -> {
                assertTrue(start.await(3, TimeUnit.SECONDS));
                return firstClient.borrowBook("LAST001");
            });
            Future<Response> second = workers.submit(() -> {
                assertTrue(start.await(3, TimeUnit.SECONDS));
                return secondClient.borrowBook("LAST001");
            });
            start.countDown();
            Response resultOne = first.get(5, TimeUnit.SECONDS);
            Response resultTwo = second.get(5, TimeUnit.SECONDS);
            assertNotEquals(resultOne.success(), resultTwo.success(),
                    "Exactly one borrower can receive the last copy");
            assertEquals(0, find(firstClient, "LAST001").quantity());
            assertEquals(1, firstClient.listLoans().loans().stream()
                    .filter(loan -> loan.code().equals("LAST001") && loan.returnedAt() == null).count());
        } finally {
            start.countDown();
            workers.shutdownNow();
        }
    }

    @Test
    void activeLoansAndHistoryPersistAcrossServerRestart() throws Exception {
        assertSuccess(firstClient.borrowBook("S005"));
        Loan active = firstClient.listLoans().loans().stream().filter(loan -> loan.code().equals("S005")
                && loan.returnedAt() == null).findFirst().orElseThrow();
        int reducedQuantity = find(firstClient, "S005").quantity();
        restartServerAndClients();
        assertEquals(reducedQuantity, find(firstClient, "S005").quantity());
        Loan afterRestart = firstClient.listLoans().loans().stream().filter(loan -> loan.id() == active.id())
                .findFirst().orElseThrow();
        assertNull(afterRestart.returnedAt());
        assertSuccess(firstClient.returnBook(afterRestart.id()));
        assertEquals(reducedQuantity + 1, find(firstClient, "S005").quantity());
        restartServerAndClients();
        Loan finalRecord = firstClient.listLoans().loans().stream().filter(loan -> loan.id() == active.id())
                .findFirst().orElseThrow();
        assertNotNull(finalRecord.returnedAt());
    }

    @Test
    void serverBroadcastsUdpNoticesToAuthenticatedClients() throws Exception {
        BlockingQueue<LibraryNotice> notices = new LinkedBlockingQueue<>();
        udpObserver = new LibraryClient();
        udpObserver.connect("127.0.0.1", server.getPort());
        assertSuccess(udpObserver.login("bandoc", "bandoc123", notices::offer));
        udpActor = new LibraryClient();
        udpActor.connect("127.0.0.1", server.getPort());
        assertSuccess(udpActor.login("admin", "admin123", notice -> { }));
        assertSuccess(udpActor.addBook(new Book("UDP001", "Thông báo mạng", "Tác giả", 2024, 1, 0)));
        assertTrue(awaitNotice(notices, "UDP001", "BOOK_ADDED"));
        assertSuccess(udpActor.borrowBook("UDP001"));
        assertTrue(awaitNotice(notices, "UDP001", "BOOK_BORROWED"));
        Loan active = udpActor.listLoans().loans().stream().filter(loan -> loan.code().equals("UDP001")
                && loan.returnedAt() == null).findFirst().orElseThrow();
        assertSuccess(udpActor.returnBook(active.id()));
        assertTrue(awaitNotice(notices, "UDP001", "BOOK_RETURNED"));
    }

    @Test
    void activityLogContainsUserAndNeverPasswords() throws Exception {
        // Fixture clients are authenticated by connectClient; verify authenticated actions are attributed.
        assertSuccess(firstClient.borrowBook("S004"));
        Loan active = firstClient.listLoans().loans().stream().filter(loan -> loan.code().equals("S004")
                && loan.returnedAt() == null).findFirst().orElseThrow();
        assertSuccess(firstClient.returnBook(active.id()));
        await(() -> Files.exists(activityLog) && readActivityLog().contains("user=admin"));
        String contents = readActivityLog();
        assertTrue(contents.contains("user=admin"));
        assertFalse(contents.contains("admin123"));
        assertTrue(contents.contains("BORROW") && contents.contains("RETURN"));
    }

    static Stream<Book> invalidBooks() {
        return Stream.of(
                new Book("", "Tên sách", "Tác giả", 2024, 1, 0),
                new Book("           ", "Tên sách", "Tác giả", 2024, 1, 0),
                new Book("TOO_LONG_11", "Tên sách", "Tác giả", 2024, 1, 0),
                new Book("BAD001", "", "Tác giả", 2024, 1, 0),
                new Book("BAD002", " ", "Tác giả", 2024, 1, 0),
                new Book("BAD003", "x".repeat(101), "Tác giả", 2024, 1, 0),
                new Book("BAD004", "Tên sách", "", 2024, 1, 0),
                new Book("BAD005", "Tên sách", "x".repeat(101), 2024, 1, 0),
                new Book("BAD006", "Tên sách", "Tác giả", 0, 1, 0),
                new Book("BAD007", "Tên sách", "Tác giả", Year.now().getValue() + 2, 1, 0),
                new Book("BAD008", "Tên sách", "Tác giả", 2024, -1, 0),
                new Book(null, "Tên sách", "Tác giả", 2024, 1, 0),
                new Book("BAD009", null, "Tác giả", 2024, 1, 0),
                new Book("BAD010", "Tên sách", null, 2024, 1, 0));
    }

    @ParameterizedTest(name = "invalid book {index} is rejected by server")
    @MethodSource("invalidBooks")
    void validationRunsOnServerAndDoesNotMutateDatabase(Book invalid) throws Exception {
        int before = firstClient.listBooks().books().size();
        assertFailure(firstClient.addBook(invalid));
        Response after = secondClient.listBooks();
        assertSuccess(after);
        assertEquals(before, after.books().size());
    }

    @Test
    void zeroStockIsAccepted() throws Exception {
        assertSuccess(firstClient.addBook(new Book("ZERO001", "Sách đã hết", "Tác giả", 2024, 0, 0)));
        assertEquals(0, find(secondClient, "ZERO001").quantity());
    }

    @Test
    void simultaneousUpdatesDoNotSilentlyLoseAChange() throws Exception {
        assertSuccess(firstClient.addBook(new Book("RACE001", "Sách đồng thời", "Tác giả", 2024, 10, 0)));
        Book snapshotOne = find(firstClient, "RACE001");
        Book snapshotTwo = find(secondClient, "RACE001");
        assertEquals(snapshotOne.version(), snapshotTwo.version());

        ExecutorService workers = Executors.newFixedThreadPool(2);
        CountDownLatch start = new CountDownLatch(1);
        try {
            Future<Response> first = workers.submit(() -> {
                assertTrue(start.await(3, TimeUnit.SECONDS));
                return firstClient.updateBook(withQuantity(snapshotOne, 31));
            });
            Future<Response> second = workers.submit(() -> {
                assertTrue(start.await(3, TimeUnit.SECONDS));
                return secondClient.updateBook(withQuantity(snapshotTwo, 42));
            });
            start.countDown();
            Response firstResult = first.get(5, TimeUnit.SECONDS);
            Response secondResult = second.get(5, TimeUnit.SECONDS);
            assertNotEquals(firstResult.success(), secondResult.success(),
                    "Exactly one update based on the same version must succeed");
            Book stored = find(firstClient, "RACE001");
            assertEquals(firstResult.success() ? 31 : 42, stored.quantity());
            assertEquals(snapshotOne.version() + 1, stored.version());
            assertFailure(firstClient.deleteBook(snapshotOne));
            assertSuccess(secondClient.deleteBook(stored));
        } finally {
            start.countDown();
            workers.shutdownNow();
        }
    }

    @Test
    void invalidUpdatesLeaveExistingBookIntact() throws Exception {
        assertSuccess(firstClient.addBook(new Book("KEEP001", "Tên ban đầu", "Tác giả", 2024, 6, 0)));
        Book original = find(firstClient, "KEEP001");
        assertFailure(secondClient.updateBook(withQuantity(original, -9)));
        assertEquals(original, find(firstClient, original.code()));
    }

    @Test
    void quotedTextIsStoredAsDataAndSearchDoesNotAlterDatabase() throws Exception {
        Book book = new Book("SQL001", "O'Reilly và Java", "D'Angelo", 2024, 3, 0);
        assertSuccess(firstClient.addBook(book));
        assertEquals(book.title(), find(secondClient, book.code()).title());
        int before = firstClient.listBooks().books().size();
        assertSuccess(secondClient.searchBooks("' OR 1=1; DROP TABLE Sach; --"));
        assertEquals(before, firstClient.listBooks().books().size());
        assertEquals(book.author(), find(firstClient, book.code()).author());
    }

    @Test
    void booksPersistAfterServerRestart() throws Exception {
        assertSuccess(firstClient.addBook(new Book("DISK001", "Lưu SQLite", "Tác giả", 2024, 17, 0)));
        Book saved = find(secondClient, "DISK001");
        restartServerAndClients();
        assertEquals(saved, find(firstClient, "DISK001"));
        assertSuccess(secondClient.listBooks());
    }

    @Test
    void deletingAllBooksDoesNotReseedOnRestart() throws Exception {
        Response initial = firstClient.listBooks();
        assertSuccess(initial);
        assertFalse(initial.books().isEmpty(), "New databases include demonstration data");
        for (Book book : initial.books()) {
            assertSuccess(firstClient.deleteBook(book));
        }
        assertTrue(secondClient.listBooks().books().isEmpty());
        restartServerAndClients();
        assertTrue(firstClient.listBooks().books().isEmpty(),
                "User deletions must survive restarts, including deleted sample books");
    }

    @Test
    void unknownOrIncompleteCommandsReceiveAnErrorAndConnectionRemainsUsable() throws Exception {
        assertFailure(firstClient.request(new Request("UNKNOWN", null, null, null, null)));
        assertFailure(firstClient.request(new Request(null, null, null, null, null)));
        assertFailure(firstClient.request(new Request("ADD", null, null, null, null)));
        assertFailure(firstClient.request(new Request("UPDATE", null, null, null, null)));
        assertFailure(firstClient.request(new Request("DELETE", null, null, "S001", null)));
        assertSuccess(firstClient.listBooks());
        assertSuccess(secondClient.listBooks());
    }

    @Test
    void malformedJsonAndOversizedFramesCannotTakeDownOtherClients() throws Exception {
        byte[] invalidJson = "{\"command\":".getBytes(StandardCharsets.UTF_8);
        assertBadFrameRejected(invalidJson.length, invalidJson);
        assertBadFrameRejected(1024 * 1024 + 1, new byte[0]);
        assertBadFrameRejected(-1, new byte[0]);
        assertSuccess(firstClient.listBooks());
        assertSuccess(secondClient.listBooks());
    }

    static Stream<String> invalidNumericRequests() {
        String valid = "{\"command\":\"ADD\",\"book\":{\"code\":\"RAW001\","
                + "\"title\":\"Sách mạng\",\"author\":\"Tác giả\","
                + "\"year\":2024,\"quantity\":2,\"version\":0}}";
        return Stream.of(
                valid.replace("\"quantity\":2", "\"quantity\":1.5"),
                valid.replace("\"year\":2024", "\"year\":2024.5"),
                valid.replace("\"quantity\":2", "\"quantity\":\"2\""),
                valid.replace("\"year\":2024", "\"year\":\"2024\""),
                valid.replace("\"version\":0", "\"version\":\"0\""),
                valid.replace("\"quantity\":2,", ""),
                valid.replace("\"year\":2024,", ""),
                valid.replace("\"quantity\":2", "\"quantity\":null"),
                valid.replace("\"year\":2024", "\"year\":null"),
                valid.replace("\"quantity\":2", "\"quantity\":2147483648"),
                valid.replace("\"quantity\":2", "\"quantity\":4294967296"),
                valid.replace("\"year\":2024", "\"year\":4294969316"),
                valid.replace("\"version\":0", "\"version\":9223372036854775808"));
    }

    @ParameterizedTest(name = "invalid numeric JSON {index} is rejected without coercion")
    @MethodSource("invalidNumericRequests")
    void rawJsonCannotCoerceOrDefaultNumericBookFields(String json) throws Exception {
        int before = firstClient.listBooks().books().size();
        byte[] bytes = json.getBytes(StandardCharsets.UTF_8);
        assertBadFrameRejected(bytes.length, bytes);
        Response after = secondClient.listBooks();
        assertSuccess(after);
        assertEquals(before, after.books().size());
        assertTrue(firstClient.searchBooks("RAW001").books().isEmpty());
    }

    @Test
    void malformedUtf8AndTrailingJsonAreRejected() throws Exception {
        ByteArrayOutputStream invalidUtf8 = new ByteArrayOutputStream();
        invalidUtf8.write("{\"command\":\"SEARCH\",\"query\":\"".getBytes(StandardCharsets.UTF_8));
        invalidUtf8.write(new byte[] {(byte) 0xC3, (byte) 0x28});
        invalidUtf8.write("\"}".getBytes(StandardCharsets.UTF_8));
        assertBadFrameRejected(invalidUtf8.size(), invalidUtf8.toByteArray());

        byte[] trailingDocument = "{\"command\":\"LIST\"} {}".getBytes(StandardCharsets.UTF_8);
        assertBadFrameRejected(trailingDocument.length, trailingDocument);
        assertSuccess(firstClient.listBooks());
        assertSuccess(secondClient.listBooks());
    }

    @Test
    void tcpFramingSupportsFragmentedAndBackToBackRequests() throws Exception {
        try (Socket rawClient = new Socket("127.0.0.1", server.getPort())) {
            rawClient.setSoTimeout(3000);
            ByteArrayOutputStream framed = new ByteArrayOutputStream();
            Wire.write(framed, Request.login("admin", "admin123", 0, null));
            for (byte singleByte : framed.toByteArray()) {
                rawClient.getOutputStream().write(singleByte & 0xff);
                rawClient.getOutputStream().flush();
            }
            assertSuccess(Wire.read(rawClient.getInputStream(), Response.class));

            ByteArrayOutputStream combined = new ByteArrayOutputStream();
            Wire.write(combined, new Request("LIST", null, null, null, null));
            Wire.write(combined, new Request("SEARCH", null, "S001", null, null));
            rawClient.getOutputStream().write(combined.toByteArray());
            rawClient.getOutputStream().flush();
            Response list = Wire.read(rawClient.getInputStream(), Response.class);
            Response search = Wire.read(rawClient.getInputStream(), Response.class);
            assertSuccess(list);
            assertFalse(list.books().isEmpty());
            assertSuccess(search);
            assertTrue(search.books().stream().anyMatch(book -> book.code().equals("S001")));
        }
    }

    @Test
    void clientDisconnectAndServerStopReleaseConnections() throws Exception {
        firstClient.close();
        await(() -> server.getConnectedClientCount() == 1);
        assertSuccess(secondClient.listBooks());
        server.close();
        await(() -> server.getConnectedClientCount() == 0);
        assertFalse(server.isRunning());
        assertThrows(IOException.class, () -> secondClient.listBooks());
    }

    private void assertBadFrameRejected(int advertisedSize, byte[] bytes) throws Exception {
        try (Socket invalidClient = new Socket("127.0.0.1", server.getPort())) {
            invalidClient.setSoTimeout(2500);
            DataOutputStream output = new DataOutputStream(invalidClient.getOutputStream());
            output.writeInt(advertisedSize);
            output.write(bytes);
            output.flush();
            try {
                Response reply = Wire.read(invalidClient.getInputStream(), Response.class);
                assertFailure(reply);
            } catch (SocketTimeoutException timeout) {
                fail("The server left a malformed request unanswered", timeout);
            } catch (IOException closedByServer) {
                // Closing the invalid stream is also a valid protocol rejection.
            }
        }
    }

    private void restartServerAndClients() throws Exception {
        firstClient.close();
        secondClient.close();
        server.close();
        startServer();
        firstClient = connectClient();
        secondClient = connectClient();
        await(() -> server.getConnectedClientCount() == 2);
    }

    private void startServer() throws Exception {
        server = new LibraryServer(database, 0, ignored -> { }, ignored -> { }, activityLog);
        server.start();
        assertTrue(server.isRunning());
        assertTrue(server.getPort() > 0);
    }

    private LibraryClient connectClient() throws IOException {
        LibraryClient client = new LibraryClient();
        client.connect("127.0.0.1", server.getPort());
        try {
            Response login = client.login("admin", "admin123", notice -> { });
            assertSuccess(login);
            assertEquals("ADMIN", login.role(), "The server must derive this role from the account record");
        } catch (AssertionError | IOException error) {
            client.close();
            throw error;
        }
        return client;
    }

    private String readActivityLog() {
        try { return java.nio.file.Files.exists(activityLog)
                ? java.nio.file.Files.readString(activityLog, StandardCharsets.UTF_8) : ""; }
        catch (IOException error) { throw new AssertionError(error); }
    }

    private static boolean awaitNotice(BlockingQueue<LibraryNotice> notices, String code, String type)
            throws InterruptedException {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (System.nanoTime() < deadline) {
            LibraryNotice notice = notices.poll(100, TimeUnit.MILLISECONDS);
            if (notice != null && code.equals(notice.code()) && type.equals(notice.type())) return true;
        }
        return false;
    }

    private static Book find(LibraryClient client, String code) throws IOException {
        Response response = client.searchBooks(code);
        assertSuccess(response);
        return response.books().stream().filter(book -> book.code().equals(code)).findFirst()
                .orElseThrow(() -> new AssertionError("Book not found: " + code));
    }

    private static Book withQuantity(Book book, int quantity) {
        return new Book(book.code(), book.title(), book.author(), book.year(), quantity, book.version());
    }

    private static void assertSuccess(Response response) {
        assertNotNull(response);
        assertTrue(response.success(), response.message());
        assertNotNull(response.books());
    }

    private static void assertFailure(Response response) {
        assertNotNull(response);
        assertFalse(response.success(), response.message());
        assertNotNull(response.message());
        assertFalse(response.message().isBlank());
    }

    private static void assertFailureCode(Response response, String expectedCode) {
        assertFailure(response);
        assertEquals(expectedCode, response.errorCode(), response.message());
    }

    private static void await(BooleanSupplier condition) {
        long deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(3);
        while (!condition.getAsBoolean() && System.nanoTime() < deadline) {
            LockSupport.parkNanos(TimeUnit.MILLISECONDS.toNanos(10));
        }
        assertTrue(condition.getAsBoolean(), "Expected connection state was not reached within 3 seconds");
    }
}
