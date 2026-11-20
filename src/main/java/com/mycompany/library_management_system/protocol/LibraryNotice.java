package com.mycompany.library_management_system.protocol;

/** A catalog change sent over UDP. The catalog itself is always read over TCP. */
public record LibraryNotice(String type, String code, String message) { }
