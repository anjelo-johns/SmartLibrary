package com.smartlibrary.model;

import java.sql.Timestamp;

/**
 * Represents a row in the `users` table — used for BOTH students and
 * the librarian, distinguished by {@link #role}. Students log in with
 * registerNumber; the librarian logs in with username.
 */
public class User {
    private int id;
    private String fullName;
    private String registerNumber; // students only
    private String username;       // librarian only
    private String email;
    private String phone;
    private String passwordHash;
    private String department;
    private String yearSem;
    private Role role;
    private String status;
    private Timestamp createdAt;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getRegisterNumber() { return registerNumber; }
    public void setRegisterNumber(String registerNumber) { this.registerNumber = registerNumber; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }

    public String getYearSem() { return yearSem; }
    public void setYearSem(String yearSem) { this.yearSem = yearSem; }

    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public boolean isLibrarian() { return role == Role.LIBRARIAN; }
    public boolean isStudent() { return role == Role.STUDENT; }
}
