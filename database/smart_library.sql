-- ============================================================
-- Smart Library Management System — Database Schema
-- Run this whole script in MySQL (Workbench, CLI, or phpMyAdmin)
-- to create the schema and tables. It does NOT insert user
-- accounts (librarian/students) — those are seeded automatically
-- and safely password-hashed the first time the application
-- starts (see AppInitListener.java). Categories and books ARE
-- inserted here since they don't involve password hashing.
-- ============================================================

DROP DATABASE IF EXISTS smart_library;
CREATE DATABASE smart_library CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE smart_library;

-- ------------------------------------------------------------
-- users  (holds BOTH students and the librarian, distinguished
-- by `role`). register_number is used by students to log in;
-- username is used by the librarian. MySQL unique indexes allow
-- multiple NULLs, so a librarian row can have a NULL
-- register_number and a student row can have a NULL username.
-- ------------------------------------------------------------
CREATE TABLE users (
    id                INT AUTO_INCREMENT PRIMARY KEY,
    full_name         VARCHAR(100)        NOT NULL,
    register_number   VARCHAR(30)         NULL,
    username          VARCHAR(50)         NULL,
    email             VARCHAR(120)        NOT NULL,
    phone             VARCHAR(15)         NOT NULL,
    password_hash     VARCHAR(255)        NOT NULL,
    department        VARCHAR(80)         NULL,
    year_sem          VARCHAR(20)         NULL,
    role              ENUM('STUDENT','LIBRARIAN') NOT NULL,
    status            ENUM('ACTIVE','INACTIVE')   NOT NULL DEFAULT 'ACTIVE',
    created_at        TIMESTAMP           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_users_register_number (register_number),
    UNIQUE KEY uq_users_username (username),
    UNIQUE KEY uq_users_email (email)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- categories
-- ------------------------------------------------------------
CREATE TABLE categories (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    category_name   VARCHAR(80) NOT NULL UNIQUE,
    description     VARCHAR(255) NULL
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- books
-- ------------------------------------------------------------
CREATE TABLE books (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    title               VARCHAR(200) NOT NULL,
    author              VARCHAR(150) NOT NULL,
    isbn                VARCHAR(20)  NULL UNIQUE,
    category_id         INT NOT NULL,
    publisher           VARCHAR(150) NULL,
    publication_year    INT NULL,
    total_copies        INT NOT NULL DEFAULT 1,
    available_copies    INT NOT NULL DEFAULT 1,
    description         TEXT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_books_category FOREIGN KEY (category_id) REFERENCES categories(id),
    CONSTRAINT chk_books_copies CHECK (total_copies >= 0 AND available_copies >= 0 AND available_copies <= total_copies)
) ENGINE=InnoDB;

CREATE INDEX idx_books_title  ON books(title);
CREATE INDEX idx_books_author ON books(author);

-- ------------------------------------------------------------
-- loans  (the "transactions" table — one row per issue/return)
-- ------------------------------------------------------------
CREATE TABLE loans (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    book_id         INT NOT NULL,
    student_id      INT NOT NULL,
    issued_by       INT NULL,        -- librarian user id, NULL if self-service
    issue_date      DATE NOT NULL,
    due_date        DATE NOT NULL,
    return_date     DATE NULL,
    status          ENUM('ACTIVE','RETURNED','OVERDUE') NOT NULL DEFAULT 'ACTIVE',
    fine_amount     DECIMAL(8,2) NOT NULL DEFAULT 0.00,
    fine_paid       BOOLEAN NOT NULL DEFAULT FALSE,
    returned_by     INT NULL,        -- librarian user id, NULL if self-service
    CONSTRAINT fk_loans_book    FOREIGN KEY (book_id)    REFERENCES books(id),
    CONSTRAINT fk_loans_student FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT fk_loans_issuer  FOREIGN KEY (issued_by)  REFERENCES users(id),
    CONSTRAINT fk_loans_returner FOREIGN KEY (returned_by) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX idx_loans_student ON loans(student_id);
CREATE INDEX idx_loans_book    ON loans(book_id);
CREATE INDEX idx_loans_status  ON loans(status);

-- ------------------------------------------------------------
-- reservations
-- ------------------------------------------------------------
CREATE TABLE reservations (
    id                  INT AUTO_INCREMENT PRIMARY KEY,
    book_id             INT NOT NULL,
    student_id          INT NOT NULL,
    reservation_date    DATE NOT NULL,
    queue_position      INT NOT NULL,
    status              ENUM('WAITING','READY','FULFILLED','CANCELLED') NOT NULL DEFAULT 'WAITING',
    CONSTRAINT fk_res_book    FOREIGN KEY (book_id)    REFERENCES books(id),
    CONSTRAINT fk_res_student FOREIGN KEY (student_id) REFERENCES users(id)
) ENGINE=InnoDB;

CREATE INDEX idx_res_book ON reservations(book_id);
CREATE INDEX idx_res_student ON reservations(student_id);

-- ------------------------------------------------------------
-- fine_payments
-- ------------------------------------------------------------
CREATE TABLE fine_payments (
    id              INT AUTO_INCREMENT PRIMARY KEY,
    loan_id         INT NOT NULL,
    student_id      INT NOT NULL,
    amount          DECIMAL(8,2) NOT NULL,
    payment_date    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    collected_by    INT NOT NULL,
    CONSTRAINT fk_fp_loan       FOREIGN KEY (loan_id) REFERENCES loans(id),
    CONSTRAINT fk_fp_student    FOREIGN KEY (student_id) REFERENCES users(id),
    CONSTRAINT fk_fp_collector  FOREIGN KEY (collected_by) REFERENCES users(id)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- Sample categories and books (no password data, safe to insert directly)
-- ------------------------------------------------------------
INSERT INTO categories (category_name, description) VALUES
 ('Computer Science', 'Programming, algorithms, and systems'),
 ('Mathematics', 'Pure and applied mathematics'),
 ('Fiction', 'Novels and short stories'),
 ('Biography', 'Life stories and memoirs'),
 ('Engineering', 'Core and applied engineering'),
 ('Reference', 'Dictionaries, encyclopedias, and manuals');

INSERT INTO books (title, author, isbn, category_id, publisher, publication_year, total_copies, available_copies, description) VALUES
 ('Introduction to Algorithms', 'Cormen, Leiserson, Rivest, Stein', '9780262033848', 1, 'MIT Press', 2009, 3, 3, 'The standard reference on algorithms and data structures.'),
 ('Database System Concepts', 'Silberschatz, Korth, Sudarshan', '9780078022159', 1, 'McGraw-Hill', 2019, 2, 2, 'Comprehensive introduction to database systems.'),
 ('Operating System Concepts', 'Silberschatz, Galvin, Gagne', '9781118063330', 1, 'Wiley', 2018, 2, 2, 'Core operating systems theory and design.'),
 ('Clean Code', 'Robert C. Martin', '9780132350884', 1, 'Prentice Hall', 2008, 1, 1, 'A handbook of agile software craftsmanship.'),
 ('Linear Algebra and Its Applications', 'David C. Lay', '9780321982384', 2, 'Pearson', 2015, 2, 2, 'Undergraduate linear algebra textbook.'),
 ('The Pragmatic Programmer', 'Hunt & Thomas', '9780135957059', 1, 'Addison-Wesley', 2019, 2, 2, 'Classic guide to software engineering practice.'),
 ('Sapiens', 'Yuval Noah Harari', '9780062316097', 4, 'Harper', 2015, 2, 2, 'A brief history of humankind.'),
 ('The Alchemist', 'Paulo Coelho', '9780061122415', 3, 'HarperOne', 1993, 3, 3, 'A shepherd boy''s journey in search of treasure.'),
 ('Structural Analysis', 'R.C. Hibbeler', '9780134610672', 5, 'Pearson', 2017, 1, 1, 'Fundamentals of structural analysis for civil engineers.'),
 ('Oxford Advanced Learner''s Dictionary', 'Oxford Press', '9780194799485', 6, 'Oxford University Press', 2020, 1, 1, 'Comprehensive English learner''s dictionary.'),
 ('Computer Networking: A Top-Down Approach', 'Kurose & Ross', '9780133594140', 1, 'Pearson', 2016, 2, 2, 'Networking fundamentals from the application layer down.'),
 ('Discrete Mathematics and Its Applications', 'Kenneth H. Rosen', '9780073383095', 2, 'McGraw-Hill', 2018, 2, 2, 'Core discrete math for computer science students.');

-- Librarian, demo students, sample loans and a sample reservation are
-- created automatically the first time the app starts up — see
-- src/main/java/com/smartlibrary/listener/AppInitListener.java and the
-- README for the exact demo credentials.
