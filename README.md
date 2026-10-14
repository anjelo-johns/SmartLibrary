# Smart Library Management System

A full-stack library management web application built with **Java Servlets, JSP, JDBC, and MySQL**, running on **Apache Tomcat**. Built on top of an earlier front-end-only prototype (`library.html`) — this version replaces the in-memory JavaScript data with a real relational database and a proper Java backend, with every create/read/update/delete operation going through the server.

---

## 1. Required software

| Tool | Version used in this project | Notes |
|---|---|---|
| JDK | 11 (or newer) | `java -version` to check |
| Apache Maven | 3.6+ | used to build the WAR file |
| MySQL Server | 8.0+ | any recent 8.x works |
| Apache Tomcat | 9.x | uses the `javax.servlet` API (Tomcat 10+ uses `jakarta.*` and will NOT work without changing every import) |
| IDE | Eclipse (Java EE edition) or IntelliJ IDEA (Ultimate, for built-in Tomcat support) | Community/Community editions work too, with a manual Tomcat run configuration |

---

## 2. Install MySQL

1. Install MySQL Server 8.x for your OS (mysql.com, or via your package manager / XAMPP / WAMP).
2. Make sure the server is running and you know the **root password** (or create a dedicated `smart_library` user).

## 3. Create the database

Run the schema script once, using either the MySQL command line or MySQL Workbench:

```bash
mysql -u root -p < database/smart_library.sql
```

This creates the `smart_library` database, every table (`users`, `categories`, `books`, `loans`, `reservations`, `fine_payments`), and inserts the starter **categories and books** (no user accounts yet — see below).

> The librarian account and two demo student accounts are **not** in the SQL script. Passwords must be stored as a salted hash (never plain text), and hand-writing a matching hash inside a `.sql` file is fragile. Instead, they are created automatically and safely the first time the app starts — see `AppInitListener.java`. This also seeds a couple of sample loans and one sample reservation so the dashboards aren't empty on first login.

## 4. Configure your database credentials

Open:

```
src/main/resources/db.properties
```

and edit these two lines to match your MySQL setup:

```properties
db.username=root
db.password=CHANGE_ME
```

This file is packaged under `WEB-INF/classes` inside the WAR (not the public web folder), so the credentials are never reachable from a browser, a JSP, or any JavaScript file.

## 5. Import into Eclipse or IntelliJ

**Eclipse:**
1. `File → Import → Maven → Existing Maven Projects`, select the `SmartLibrary` folder.
2. Right-click the project → `Properties → Project Facets` — make sure **Dynamic Web Module** and **Java** are checked.
3. Right-click → `Run As → Maven Build...` with goal `package` to confirm it builds (this downloads the dependencies listed in `pom.xml` from Maven Central the first time).

**IntelliJ IDEA:**
1. `File → Open`, select the `SmartLibrary` folder (IntelliJ auto-detects the Maven project).
2. Let Maven download dependencies (bottom-right notification, or `mvn` panel → refresh icon).

## 6. Add Apache Tomcat

**Eclipse:**
1. `Window → Preferences → Server → Runtime Environments → Add...` → select Apache Tomcat 9, point it at your installed Tomcat folder.
2. Right-click the project → `Run As → Run on Server` → choose the Tomcat 9 server you just added.

**IntelliJ (Ultimate):**
1. `Run → Edit Configurations → + → Tomcat Server → Local`, point **Application server** at your Tomcat install.
2. Under **Deployment**, add an artifact: `SmartLibrary:war exploded`.
3. Run the configuration.

**Command line (any IDE, or no IDE at all):**
```bash
mvn clean package
cp target/SmartLibrary.war /path/to/tomcat/webapps/
/path/to/tomcat/bin/startup.sh      # or startup.bat on Windows
```
Tomcat will unpack the WAR automatically.

## 7. Run the application

Once Tomcat is running, open:

```
http://localhost:8080/SmartLibrary/
```

(or whatever context path your IDE assigned — check the console output). You should land on the sign-in page. The very first request will trigger `AppInitListener`, which seeds the librarian/demo accounts — check the Tomcat console log for a line confirming the seed completed.

## 8. Sample login credentials

| Role | Identifier | Password |
|---|---|---|
| Librarian | `librarian` | `library123` |
| Demo student | `STU001` | `Student@123` |
| Demo student | `STU002` | `Student@123` |

You can also register a brand-new student account from the sign-in page's "Create an account" link.

---

## 9. Project structure

```
SmartLibrary/
├── pom.xml
├── database/
│   └── smart_library.sql                 <- run this once to create the schema
├── src/main/resources/
│   └── db.properties                     <- your DB credentials go here
└── src/main/java/com/smartlibrary/
    ├── model/        User, Book, Category, Loan, Reservation, FinePayment, Role
    ├── dao/           UserDAO, BookDAO, CategoryDAO, LoanDAO, ReservationDAO, FineDAO
    ├── service/       LibraryService (issue/return transactions), FineService, ReservationService
    ├── servlet/       one servlet per feature (see below)
    ├── filter/        AuthenticationFilter, AuthorizationFilter
    ├── listener/      AppInitListener (one-time data seed)
    ├── exception/     LibraryException, DuplicateEntryException, UnauthorizedAccessException
    └── util/          DBConnection, PasswordUtil, ValidationUtil, LibraryConfig
└── src/main/webapp/
    ├── login.jsp, register.jsp, error.jsp, unauthorized.jsp
    ├── common/        header.jsp (sidebar + shell), footer.jsp
    ├── student/       dashboard, catalog, my-books, reservations, history, fines, profile
    ├── librarian/     dashboard, circulation, books, students, student-history, reservations, fines, reports
    ├── css/style.css
    ├── js/validate.js, js/app.js
    └── WEB-INF/web.xml
```

### Servlets and what they do

| Servlet | URL(s) | Purpose |
|---|---|---|
| `RegisterServlet` | `/register` | Student sign-up with full server-side validation |
| `LoginServlet` | `/login` | Student and librarian sign-in |
| `LogoutServlet` | `/logout` | Invalidates the session |
| `CatalogServlet` | `/student/catalog` | Search/filter books; also serves JSON for live search |
| `IssueBookServlet` | `/student/issue`, `/librarian/issue` | Issues a book (self-service or desk) |
| `ReturnBookServlet` | `/student/return`, `/librarian/return` | Returns a book, calculates fine |
| `ReservationServlet` | `/student/reservations` | Create/cancel a reservation |
| `LibrarianReservationsServlet` | `/librarian/reservations` | View all active reservations |
| `FineServlet` | `/student/fines`, `/librarian/fines` | View fines; librarian records payments |
| `StudentDashboardServlet` | `/student/dashboard` | Student's stat overview |
| `LibrarianDashboardServlet` | `/librarian/dashboard` | Library-wide stat overview |
| `CirculationServlet` | `/librarian/circulation` | Desk view: issue form + active loans |
| `BookManagementServlet` | `/librarian/books` | Add/edit/delete books |
| `StudentManagementServlet` | `/librarian/students` | Search students, view one's history |
| `ReportServlet` | `/librarian/reports` | Most-borrowed, overdue, fines, reservations |
| `MyBooksServlet` | `/student/my-books` | A student's active loans |
| `HistoryServlet` | `/student/history` | A student's returned loans |
| `ProfileServlet` | `/student/profile` | View profile, change password |

---

## 10. Business rules (configurable in `LibraryConfig.java`)

- **Loan period:** 14 days
- **Fine rate:** ₹5 per day overdue
- **Borrowing limit:** 3 books per student at once
- **Session timeout:** 30 minutes

Changing any of these constants changes the behaviour everywhere it applies — nothing else in the codebase hard-codes these numbers.

---

## 11. Security notes

- Every SQL query uses `PreparedStatement` — no user input is ever concatenated into SQL.
- Passwords are hashed with **PBKDF2WithHmacSHA256** (65,536 iterations, random 16-byte salt per user) — never stored or compared as plain text, and never put in `localStorage` or `sessionStorage`.
- `AuthenticationFilter` blocks any request under `/student/*` or `/librarian/*` without a valid session.
- `AuthorizationFilter` blocks a student from reaching `/librarian/*` (and vice versa) even by typing the URL directly — it forwards to `unauthorized.jsp`.
- The database password lives only in `db.properties`, loaded from the classpath — never referenced from a JSP or JavaScript file.
- All book-issue/return/reservation-queue transitions run inside explicit JDBC transactions (`conn.setAutoCommit(false)` + commit/rollback) with row locking (`SELECT ... FOR UPDATE`) so concurrent requests can't double-issue the last copy of a book.

---

## 12. Testing checklist

Manually walk through these after setup — all were exercised during development:

**Authentication**
- [ ] Valid student login (`STU001` / `Student@123`) → lands on student dashboard
- [ ] Valid librarian login (`librarian` / `library123`) → lands on librarian dashboard
- [ ] Wrong password → friendly error, no stack trace
- [ ] Empty fields → friendly error
- [ ] Register with an already-used register number or email → rejected with a clear message
- [ ] Password/confirm-password mismatch on registration → rejected
- [ ] While logged in as a student, manually type a `/librarian/...` URL → redirected to "Not authorized"

**Books**
- [ ] Add a book with valid data → appears in the catalog immediately
- [ ] Edit a book's copy count *below* the number currently issued → rejected with a clear message
- [ ] Delete a book with an active loan or reservation → rejected with a clear message
- [ ] Search/filter the catalog by title, author, category, and "available only"

**Borrowing**
- [ ] Issue an available book to yourself (student) → appears in "My Books" with a due date 14 days out
- [ ] Try to issue a book with 0 available copies → rejected, "Reserve" offered instead
- [ ] Try to issue the same book twice to the same student → rejected as duplicate
- [ ] Issue books until the 3-book limit is hit → 4th issue rejected
- [ ] Return a book on time → no fine
- [ ] Return a book late → fine calculated as `overdue days × ₹5`, matching what's shown before returning

**Fines**
- [ ] Librarian marks a fine as paid (full amount) → loan flips to "paid", payment appears in history
- [ ] Attempt to pay more than the outstanding fine → rejected

**Reservations**
- [ ] Reserve a book with 0 copies available → queue position assigned
- [ ] Reserve the same book twice → rejected as duplicate
- [ ] Cancel your own reservation → removed from your list
- [ ] Return a book that has a reservation queue → the earliest reservation flips to "Ready"

---

## 13. Where the frontend and backend actually connect

Every button and form in the UI posts to a real servlet, which calls a DAO/service class, which runs a `PreparedStatement` against MySQL — there is no JavaScript array standing in for the database anywhere in this version. The one piece of client-side JavaScript that talks to the server directly is the catalog's live search box, which calls `CatalogServlet` with `?format=json` and re-renders results from the JSON response (see `js/app.js`, function `liveSearchCatalog`) — everything else is plain form submissions, which also means the app works even with JavaScript disabled (aside from that one convenience feature and the client-side validation messages).
