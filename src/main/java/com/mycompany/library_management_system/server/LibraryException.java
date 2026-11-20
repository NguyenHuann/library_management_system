package com.mycompany.library_management_system.server;

/** An expected validation or business-rule failure, safe to display on a client. */
public final class LibraryException extends Exception {
    private final String code;

    public LibraryException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String code() { return code; }
}
