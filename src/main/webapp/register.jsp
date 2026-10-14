<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Create account — Stackline Smart Library</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Source+Serif+4:ital,wght@0,500;0,600;0,700;1,500&amp;family=Inter:wght@400;500;600;700&amp;display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="auth-page">
  <div class="auth-wrap" style="max-width:760px;">
    <div class="auth-hero">
      <div>
        <div class="mark">STACKLINE LIBRARY</div>
        <h1>Join the<br>reading room.</h1>
        <p>Register once with your student details to search the catalog, issue books, and track fines.</p>
      </div>
    </div>
    <div class="auth-panel">
      <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
      </c:if>
      <form method="post" action="${pageContext.request.contextPath}/register" data-validate>
        <div class="field">
          <label>Full name</label>
          <input type="text" name="fullName" required data-rule="name" data-error-message="2-80 letters, spaces, apostrophes or hyphens only."
                 value="<c:out value="${fullName}"/>">
          <div class="field-error"></div>
        </div>
        <div class="form-row-2">
          <div class="field">
            <label>Register number</label>
            <input type="text" name="registerNumber" required data-rule="registerNumber" data-error-message="3-20 letters/numbers, - or /."
                   value="<c:out value="${registerNumber}"/>">
            <div class="field-error"></div>
          </div>
          <div class="field">
            <label>Email</label>
            <input type="email" name="email" required data-rule="email" data-error-message="Enter a valid email address."
                   value="<c:out value="${email}"/>">
            <div class="field-error"></div>
          </div>
        </div>
        <div class="form-row-2">
          <div class="field">
            <label>Phone (10-digit Indian mobile)</label>
            <input type="text" name="phone" required data-rule="phone" data-error-message="Enter a valid 10-digit Indian mobile number."
                   value="<c:out value="${phone}"/>">
            <div class="field-error"></div>
          </div>
          <div class="field">
            <label>Department</label>
            <input type="text" name="department" placeholder="e.g. Computer Science" value="<c:out value="${department}"/>">
          </div>
        </div>
        <div class="field">
          <label>Year / semester</label>
          <input type="text" name="yearSem" placeholder="e.g. 3rd Year" value="<c:out value="${yearSem}"/>">
        </div>
        <div class="form-row-2">
          <div class="field">
            <label>Password</label>
            <input type="password" name="password" id="password" required data-rule="password"
                   data-error-message="At least 8 characters with a letter, number, and special character.">
            <div class="field-error"></div>
          </div>
          <div class="field">
            <label>Confirm password</label>
            <input type="password" name="confirmPassword" required data-rule="confirmPassword" data-matches="password">
            <div class="field-error"></div>
          </div>
        </div>
        <button class="btn-primary" type="submit">Create account</button>
      </form>
      <div class="auth-foot">Already registered? <a href="${pageContext.request.contextPath}/login" style="color:var(--moss-deep); font-weight:600;">Sign in</a></div>
    </div>
  </div>
</div>
<script src="${pageContext.request.contextPath}/js/validate.js"></script>
</body>
</html>
