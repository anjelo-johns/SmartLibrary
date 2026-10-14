package com.smartlibrary.model;

import java.sql.Date;

public class Loan {
    private int id;
    private int bookId;
    private String bookTitle;      // joined, read-only
    private int studentId;
    private String studentName;    // joined, read-only
    private String studentRegNo;   // joined, read-only
    private Integer issuedBy;
    private Date issueDate;
    private Date dueDate;
    private Date returnDate;
    private String status; // ACTIVE, RETURNED, OVERDUE
    private double fineAmount;
    private boolean finePaid;
    private Integer returnedBy;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getBookId() { return bookId; }
    public void setBookId(int bookId) { this.bookId = bookId; }

    public String getBookTitle() { return bookTitle; }
    public void setBookTitle(String bookTitle) { this.bookTitle = bookTitle; }

    public int getStudentId() { return studentId; }
    public void setStudentId(int studentId) { this.studentId = studentId; }

    public String getStudentName() { return studentName; }
    public void setStudentName(String studentName) { this.studentName = studentName; }

    public String getStudentRegNo() { return studentRegNo; }
    public void setStudentRegNo(String studentRegNo) { this.studentRegNo = studentRegNo; }

    public Integer getIssuedBy() { return issuedBy; }
    public void setIssuedBy(Integer issuedBy) { this.issuedBy = issuedBy; }

    public Date getIssueDate() { return issueDate; }
    public void setIssueDate(Date issueDate) { this.issueDate = issueDate; }

    public Date getDueDate() { return dueDate; }
    public void setDueDate(Date dueDate) { this.dueDate = dueDate; }

    public Date getReturnDate() { return returnDate; }
    public void setReturnDate(Date returnDate) { this.returnDate = returnDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public double getFineAmount() { return fineAmount; }
    public void setFineAmount(double fineAmount) { this.fineAmount = fineAmount; }

    public boolean isFinePaid() { return finePaid; }
    public void setFinePaid(boolean finePaid) { this.finePaid = finePaid; }

    public Integer getReturnedBy() { return returnedBy; }
    public void setReturnedBy(Integer returnedBy) { this.returnedBy = returnedBy; }
}
