package com.mycompany.library_management_system.model;

/** Immutable data exchanged between the server and its clients. */
public record Book(String code, String title, String author, int year, int quantity, long version) {
    public Book(String code, String title, String author, int year, int quantity) {
        this(code, title, author, year, quantity, 0);
    }
}
