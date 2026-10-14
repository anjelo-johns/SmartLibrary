<%@ page contentType="text/html;charset=UTF-8" isErrorPage="false" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <title>Something went wrong — Stackline</title>
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="auth-page">
  <div class="auth-wrap" style="grid-template-columns:1fr; max-width:520px;">
    <div class="auth-panel" style="text-align:center;">
      <h1 style="font-size:26px; margin-bottom:12px;">We hit a snag</h1>
      <p style="color:var(--ink-soft); margin-bottom:20px;">
        <c:out value="${error}" default="Something unexpected happened. Please try again."/>
      </p>
      <a class="btn-primary" style="display:inline-block; width:auto; padding:12px 26px;" href="${pageContext.request.contextPath}/login">Back to sign in</a>
    </div>
  </div>
</div>
</body>
</html>
