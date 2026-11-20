package com.mycompany.library_management_system.protocol;

import com.mycompany.library_management_system.model.Book;

/** One command per frame. Version is required when deleting an existing book. */
public record Request(String command, Book book, String query, String code, Long version,
        String username, String password, Integer udpPort, String udpToken, Long loanId) {
    public Request(String command, Book book, String query, String code, Long version) {
        this(command, book, query, code, version, null, null, null, null, null);
    }

    public static Request login(String username, String password, int udpPort, String udpToken) {
        return new Request("LOGIN", null, null, null, null, username, password, udpPort, udpToken, null);
    }

    public static Request borrow(String code) { return new Request("BORROW", null, null, code, null); }

    public static Request returnLoan(long loanId) {
        return new Request("RETURN", null, null, null, null, null, null, null, null, loanId);
    }

    public static Request loans() { return new Request("LOANS", null, null, null, null); }

    /** Never include credentials or the UDP session token in diagnostic output. */
    @Override public String toString() { return "Request[command=" + command + "]"; }

    public static Request list() { return new Request("LIST", null, null, null, null); }
    public static Request search(String query) { return new Request("SEARCH", null, query, null, null); }
    public static Request add(Book book) { return new Request("ADD", book, null, null, null); }
    public static Request update(Book book) { return new Request("UPDATE", book, null, null, null); }
    public static Request delete(Book book) {
        return new Request("DELETE", null, null, book.code(), book.version());
    }
}
