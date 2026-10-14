package com.smartlibrary.util;

import java.util.regex.Pattern;

/**
 * Server-side validation rules. The same rules are mirrored in
 * js/validate.js for instant client-side feedback, but every one
 * of these checks runs again here — client-side validation alone
 * is never trusted.
 */
public final class ValidationUtil {

    private static final Pattern NAME_PATTERN =
            Pattern.compile("^[A-Za-z][A-Za-z .'-]{1,79}$");
    private static final Pattern REGISTER_NUMBER_PATTERN =
            Pattern.compile("^[A-Za-z0-9/-]{3,20}$");
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern INDIAN_PHONE_PATTERN =
            Pattern.compile("^[6-9]\\d{9}$");
    // At least 8 chars, one letter, one digit, one special character
    private static final Pattern PASSWORD_PATTERN =
            Pattern.compile("^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{8,64}$");
    private static final Pattern ISBN_PATTERN =
            Pattern.compile("^[0-9Xx-]{10,20}$");

    private ValidationUtil() {
    }

    public static String validateName(String name) {
        if (isBlank(name)) return "Name is required.";
        if (!NAME_PATTERN.matcher(name.trim()).matches()) {
            return "Name must be 2-80 characters and contain only letters, spaces, apostrophes, or hyphens.";
        }
        return null;
    }

    public static String validateRegisterNumber(String regNo) {
        if (isBlank(regNo)) return "Register number is required.";
        if (!REGISTER_NUMBER_PATTERN.matcher(regNo.trim()).matches()) {
            return "Register number must be 3-20 characters (letters, numbers, - or /).";
        }
        return null;
    }

    public static String validateEmail(String email) {
        if (isBlank(email)) return "Email is required.";
        if (!EMAIL_PATTERN.matcher(email.trim()).matches()) {
            return "Enter a valid email address.";
        }
        return null;
    }

    public static String validatePhone(String phone) {
        if (isBlank(phone)) return "Phone number is required.";
        if (!INDIAN_PHONE_PATTERN.matcher(phone.trim()).matches()) {
            return "Enter a valid 10-digit Indian mobile number.";
        }
        return null;
    }

    public static String validatePassword(String password) {
        if (isBlank(password)) return "Password is required.";
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
            return "Password must be at least 8 characters and include a letter, a number, and a special character.";
        }
        return null;
    }

    public static String validateConfirmPassword(String password, String confirm) {
        if (isBlank(confirm)) return "Please confirm your password.";
        if (!password.equals(confirm)) return "Passwords do not match.";
        return null;
    }

    public static String validateBookTitle(String title) {
        if (isBlank(title)) return "Title is required.";
        if (title.trim().length() < 2 || title.trim().length() > 200) {
            return "Title must be between 2 and 200 characters.";
        }
        return null;
    }

    public static String validateAuthor(String author) {
        if (isBlank(author)) return "Author is required.";
        if (author.trim().length() > 150) return "Author name is too long.";
        return null;
    }

    public static String validateIsbn(String isbn) {
        if (isBlank(isbn)) return null; // ISBN is optional
        if (!ISBN_PATTERN.matcher(isbn.trim()).matches()) {
            return "Enter a valid ISBN (10-20 digits, may include hyphens).";
        }
        return null;
    }

    public static String validateCopies(String copiesStr) {
        if (isBlank(copiesStr)) return "Number of copies is required.";
        try {
            int copies = Integer.parseInt(copiesStr.trim());
            if (copies < 0) return "Copies cannot be negative.";
        } catch (NumberFormatException e) {
            return "Copies must be a whole number.";
        }
        return null;
    }

    public static String validateCategoryId(String categoryIdStr) {
        if (isBlank(categoryIdStr)) return "Category is required.";
        try {
            int id = Integer.parseInt(categoryIdStr.trim());
            if (id <= 0) return "Select a valid category.";
        } catch (NumberFormatException e) {
            return "Select a valid category.";
        }
        return null;
    }

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
