package com.smartlibrary.util;

/**
 * Command-line utility to generate a secure PBKDF2 hash for a password.
 * Usage:
 *   java com.smartlibrary.util.HashPassword <plain-text-password>
 */
public class HashPassword {
    public static void main(String[] args) {
        if (args.length < 1 || args[0].trim().isEmpty()) {
            System.err.println("Usage: java com.smartlibrary.util.HashPassword <plain-password>");
            System.exit(1);
        }
        String plain = args[0];
        String hashed = PasswordUtil.hash(plain);
        System.out.println("==================================================");
        System.out.println("Input password : " + plain);
        System.out.println("PBKDF2 Hash    : " + hashed);
        System.out.println("Verified       : " + PasswordUtil.verify(plain, hashed));
        System.out.println("==================================================");
    }
}
