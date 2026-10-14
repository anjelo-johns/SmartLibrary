package com.smartlibrary.util;

/**
 * Central place for business rules that would otherwise be scattered
 * (and hard-coded) across servlets and JSPs. Change a value here and
 * it takes effect everywhere it's used.
 */
public final class LibraryConfig {

    /** Standard loan period in days. */
    public static final int LOAN_PERIOD_DAYS = 14;

    /** Fine charged per day overdue, in rupees. */
    public static final double FINE_PER_DAY = 5.00;

    /** Maximum number of books a student may have on loan at once. */
    public static final int MAX_ACTIVE_LOANS_PER_STUDENT = 3;

    /** Session timeout in minutes (also set in web.xml). */
    public static final int SESSION_TIMEOUT_MINUTES = 30;

    private LibraryConfig() {
    }
}
