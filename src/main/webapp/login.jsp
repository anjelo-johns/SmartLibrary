<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Sign in — Stackline Smart Library</title>
  <link rel="preconnect" href="https://fonts.googleapis.com">
  <link href="https://fonts.googleapis.com/css2?family=Source+Serif+4:ital,wght@0,500;0,600;0,700;1,500&amp;family=Inter:wght@400;500;600;700&amp;display=swap" rel="stylesheet">
  <link rel="stylesheet" href="${pageContext.request.contextPath}/css/style.css">
</head>
<body>
<div class="auth-page">
  <div class="auth-wrap">
    <div class="auth-hero">
      <div>
        <div class="mark">STACKLINE LIBRARY</div>
        <h1>The catalog knows<br>what's due today.</h1>
        <p>Search the shelves, track your loans, and never lose a due date to a sticky note again.</p>
      </div>
      <div class="spines" id="spineDecoration"></div>
    </div>
    <div class="auth-panel">
      <c:if test="${param.timeout == '1'}">
        <div class="alert alert-error">Your session expired or you need to sign in first.</div>
      </c:if>
      <c:if test="${param.loggedOut == '1'}">
        <div class="alert alert-success">You've been signed out.</div>
      </c:if>
      <c:if test="${param.registered == '1'}">
        <div class="alert alert-success">Registration successful — sign in below.</div>
      </c:if>
      <c:if test="${not empty error}">
        <div class="alert alert-error"><c:out value="${error}"/></div>
      </c:if>

      <div class="role-toggle">
        <a href="#" id="tabStudent" class="active" onclick="setRole('student'); return false;">Student</a>
        <a href="#" id="tabLibrarian" onclick="setRole('librarian'); return false;">Librarian</a>
      </div>

      <form method="post" action="${pageContext.request.contextPath}/login" data-validate>
        <input type="hidden" name="loginAs" id="loginAs" value="${empty loginAs ? 'student' : loginAs}">

        <div class="field" id="identifierField">
          <label id="identifierLabel">Register number</label>
          <input type="text" name="identifier" id="identifier" required value="<c:out value="${identifier}"/>" placeholder="e.g. STU001">
        </div>
        <div class="field">
          <label>Password</label>
          <input type="password" name="password" required>
        </div>
        <button class="btn-primary" type="submit">Sign in</button>
      </form>
      <div class="hint" id="demoHint" style="margin-top:14px;">Demo student login: <b>STU001</b> / <b>Student@123</b></div>
      <div class="auth-foot">New student? <a href="${pageContext.request.contextPath}/register" style="color:var(--moss-deep); font-weight:600;">Create an account</a></div>
    </div>
  </div>
</div>
<script>
  function setRole(role){
    document.getElementById('loginAs').value = role;
    document.getElementById('tabStudent').classList.toggle('active', role==='student');
    document.getElementById('tabLibrarian').classList.toggle('active', role==='librarian');
    document.getElementById('identifierLabel').textContent = role==='student' ? 'Register number' : 'Staff username';
    document.getElementById('identifier').placeholder = role==='student' ? 'e.g. STU001' : 'librarian';
    document.getElementById('demoHint').innerHTML = role==='student'
      ? 'Demo student login: <b>STU001</b> / <b>Student@123</b>'
      : 'Demo staff login: <b>librarian</b> / <b>library123</b>';
  }
  <c:if test="${loginAs == 'librarian'}">setRole('librarian');</c:if>

  (function(){
    var wrap = document.getElementById('spineDecoration');
    var heights = [64,44,58,36,50,60,40,52];
    var colors = ['#B8863E','#A23E2D','#E3C793','#5C7C68','#B8863E','#A23E2D','#E3C793','#5C7C68'];
    heights.forEach(function(h,i){
      var el = document.createElement('i');
      el.style.height = h+'px';
      el.style.background = colors[i];
      wrap.appendChild(el);
    });
  })();
</script>
<script src="${pageContext.request.contextPath}/js/validate.js"></script>
</body>
</html>
