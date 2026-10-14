package com.smartlibrary.service;

import com.smartlibrary.model.Loan;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FineServiceTest {

    private FineService fineService;
    private static final double FINE_PER_DAY = 5.0;

    @BeforeEach
    void setUp() {
        fineService = new FineService();
    }

    @Test
    void noOverdueWhenReturnedOnDueDate() {
        Date due = Date.valueOf(LocalDate.now());
        Date ret = Date.valueOf(LocalDate.now());
        assertEquals(0, fineService.overdueDays(due, ret));
    }

    @Test
    void overdueDaysWhenReturnedLate() {
        Date due = Date.valueOf(LocalDate.now().minusDays(5));
        Date ret = Date.valueOf(LocalDate.now());
        assertEquals(5, fineService.overdueDays(due, ret));
    }

    @Test
    void noNegativeOverdueDays() {
        Date due = Date.valueOf(LocalDate.now().plusDays(3));
        Date ret = Date.valueOf(LocalDate.now());
        assertEquals(0, fineService.overdueDays(due, ret), "Days must not go negative for early returns");
    }

    @Test
    void zeroFineForOnTimeReturn() {
        Date due = Date.valueOf(LocalDate.now());
        Date ret = Date.valueOf(LocalDate.now());
        assertEquals(0.0, fineService.calculateFineOnReturn(due, ret), 0.001);
    }

    @Test
    void fineCalculatedForLateReturn() {
        Date due = Date.valueOf(LocalDate.now().minusDays(3));
        Date ret = Date.valueOf(LocalDate.now());
        double expected = 3 * FINE_PER_DAY;
        assertEquals(expected, fineService.calculateFineOnReturn(due, ret), 0.001);
    }

    @Test
    void currentFineIsZeroForLoanNotYetDue() {
        Loan loan = new Loan();
        loan.setDueDate(Date.valueOf(LocalDate.now().plusDays(5)));
        assertEquals(0.0, fineService.calculateCurrentFine(loan), 0.001);
    }

    @Test
    void currentFineIsPositiveForOverdueLoan() {
        Loan loan = new Loan();
        loan.setDueDate(Date.valueOf(LocalDate.now().minusDays(4)));
        double expected = 4 * FINE_PER_DAY;
        assertEquals(expected, fineService.calculateCurrentFine(loan), 0.001);
    }

    @Test
    void currentFineUsesStoredAmountWhenAlreadyReturned() {
        Loan loan = new Loan();
        loan.setDueDate(Date.valueOf(LocalDate.now().minusDays(10)));
        loan.setReturnDate(Date.valueOf(LocalDate.now().minusDays(7)));
        loan.setFineAmount(15.0);
        assertEquals(15.0, fineService.calculateCurrentFine(loan), 0.001,
                "Should return stored fine, not recalculate");
    }
}