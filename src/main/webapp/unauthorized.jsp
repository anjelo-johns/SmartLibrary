<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Not authorized — Stackline</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="auth-page">
  <div class="auth-wrap" style="grid-template-columns:1fr; max-width:520px;">
    <div class="auth-panel" style="text-align:center;">
      <h1 style="font-size:26px; margin-bottom:12px;">Not authorized</h1>
      <p style="color:var(--ink-soft); margin-bottom:20px;">Your account doesn't have access to that page.</p>
      <c:if test="${sessionScope.user.role == 'LIBRARIAN'}">
        <a class="btn-primary" style="display:inline-block; width:auto; padding:12px 26px;" href="${pageContext.request.contextPath}/librarian/dashboard">Go to my dashboard</a>
      </c:if>
      <c:if test="${sessionScope.user.role == 'STUDENT'}">
        <a class="btn-primary" style="display:inline-block; width:auto; padding:12px 26px;" href="${pageContext.request.contextPath}/student/dashboard">Go to my dashboard</a>
      </c:if>
    </div>
  </div>
</div>
</body>
</html>
