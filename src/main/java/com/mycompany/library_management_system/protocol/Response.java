package com.mycompany.library_management_system.protocol;

import com.mycompany.library_management_system.model.Book;
import com.mycompany.library_management_system.model.Loan;
import java.util.List;

public record Response(boolean success, String message, List<Book> books, String errorCode,
        String username, List<Loan> loans, String role) {
    public Response {
        loans = loans == null ? List.of() : List.copyOf(loans);
    }

    public Response(boolean success, String message, List<Book> books, String errorCode) {
        this(success, message, books, errorCode, null, List.of(), null);
    }

    public Response(boolean success, String message, List<Book> books, String errorCode,
            String username, List<Loan> loans) {
        this(success, message, books, errorCode, username, loans, null);
    }

    public static Response ok(String message, List<Book> books) {
        return new Response(true, message, List.copyOf(books), null);
    }

    public static Response error(String code, String message) {
        return new Response(false, message, List.of(), code);
    }

    public static Response signedIn(String username, String role) {
        return new Response(true, "Đăng nhập thành công.", List.of(), null, username, List.of(), role);
    }

    public static Response loanHistory(List<Loan> loans) {
        return new Response(true, "Đã tải lịch sử mượn sách.", List.of(), null, null, loans);
    }
}
