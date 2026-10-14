package com.smartlibrary.service;

import com.smartlibrary.dao.FineDAO;
import com.smartlibrary.dao.LoanDAO;
import com.smartlibrary.exception.LibraryException;
import com.smartlibrary.model.Loan;
import com.smartlibrary.util.LibraryConfig;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * All fine math lives here so it's calculated identically everywhere
 * (return processing, dashboards, and the fines report) and can be
 * unit-tested in isolation from the servlets.
 */
public class FineService {

    private final LoanDAO loanDAO = new LoanDAO();
    private final FineDAO fineDAO = new FineDAO();

    /** Fine for a loan that is still out, calculated against today. */
    public double calculateCurrentFine(Loan loan) {
        if (loan.getReturnDate() != null) return loan.getFineAmount();
        long overdueDays = overdueDays(loan.getDueDate(), Date.valueOf(LocalDate.now()));
        return overdueDays * LibraryConfig.FINE_PER_DAY;
    }

    /** Fine for a loan that is being returned right now. */
    public double calculateFineOnReturn(Date dueDate, Date returnDate) {
        long overdueDays = overdueDays(dueDate, returnDate);
        return overdueDays * LibraryConfig.FINE_PER_DAY;
    }

    public long overdueDays(Date dueDate, Date onDate) {
        long days = ChronoUnit.DAYS.between(dueDate.toLocalDate(), onDate.toLocalDate());
        return Math.max(0, days);
    }

    /**
     * Records a fine payment for a loan. Librarian-only operation.
     * Rejects overpayment and marks the loan's fine as fully paid
     * once the outstanding balance reaches zero. The original fine
     * record is never deleted — payment history is preserved in
     * fine_payments.
     */
    public void payFine(int loanId, double amountPaid, int collectedByLibrarianId) throws SQLException, LibraryException {
        Loan loan = loanDAO.findById(loanId);
        if (loan == null) throw new LibraryException("Loan not found.");

        double baseFine = loan.getFineAmount();
        if (baseFine <= 0 && ("OVERDUE".equals(loan.getStatus()) || (loan.getReturnDate() == null && "ACTIVE".equals(loan.getStatus())))) {
            baseFine = calculateCurrentFine(loan);
        }

        if (baseFine <= 0) throw new LibraryException("This loan has no fine outstanding.");
        if (loan.isFinePaid()) throw new LibraryException("This fine has already been paid in full.");
        if (amountPaid <= 0) throw new LibraryException("Payment amount must be greater than zero.");

        double alreadyPaid = fineDAO.getTotalPaidForLoan(loanId);
        double remaining = baseFine - alreadyPaid;
        if (remaining <= 0.001) {
            loanDAO.markFinePaid(loanId);
            throw new LibraryException("This fine has already been paid in full.");
        }
        if (amountPaid > remaining + 0.001) {
            throw new LibraryException(String.format("Payment of Rs. %.2f exceeds the outstanding balance of Rs. %.2f.", amountPaid, remaining));
        }

        if (loan.getFineAmount() <= 0 && baseFine > 0) {
            loanDAO.updateFineAmount(loanId, baseFine);
        }

        fineDAO.recordPayment(loanId, loan.getStudentId(), amountPaid, collectedByLibrarianId);

        if (alreadyPaid + amountPaid >= baseFine - 0.001) {
            loanDAO.markFinePaid(loanId);
        }
    }
}
