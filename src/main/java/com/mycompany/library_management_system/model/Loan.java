package com.mycompany.library_management_system.model;

/** A loan history entry. Timestamps use ISO-8601 UTC; returnedAt is null while the loan is open. */
public record Loan(long id, String username, String code, String title, String borrowedAt, String returnedAt) { }
