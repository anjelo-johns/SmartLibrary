package com.smartlibrary.model;

import java.sql.Timestamp;

public class FinePayment {
    private int id;
    private int loanId;
    private int studentId;
    private double amount;
    private Timestamp paymentDate;
    private int collectedBy;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getLoanId() { return loanId; }
    public void setLoanId(int loanId) { this.loanId = loanId; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public double getAmount() { return amount; }
    public void setAmount(double amount) { this.amount = amount; }

    public Timestamp getPaymentDate() { return paymentDate; }
    public void setPaymentDate(Timestamp paymentDate) { this.paymentDate = paymentDate; }

    public int getCollectedBy() { return collectedBy; }
    public void setCollectedBy(int collectedBy) { this.collectedBy = collectedBy; }
}
