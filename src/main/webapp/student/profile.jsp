<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Profile" scope="request"/>
<c:set var="activeNav" value="profile" scope="request"/>
<%@ include file="/common/header.jsp" %>

<div class="page-head">
  <div><h2>Profile</h2><p>Your account details and password.</p></div>
</div>

<div class="panel">
  <h3>Account details</h3>
  <div class="form-row-2">
    <div class="field"><label>Full name</label><input type="text" value="<c:out value="${sessionScope.user.fullName}"/>" disabled></div>
    <div class="field"><label>Register number</label><input type="text" value="<c:out value="${sessionScope.user.registerNumber}"/>" disabled></div>
  </div>
  <div class="form-row-2">
    <div class="field"><label>Email</label><input type="text" value="<c:out value="${sessionScope.user.email}"/>" disabled></div>
    <div class="field"><label>Phone</label><input type="text" value="<c:out value="${sessionScope.user.phone}"/>" disabled></div>
  </div>
  <div class="form-row-2">
    <div class="field"><label>Department</label><input type="text" value="<c:out value="${sessionScope.user.department}"/>" disabled></div>
    <div class="field"><label>Year / semester</label><input type="text" value="<c:out value="${sessionScope.user.yearSem}"/>" disabled></div>
  </div>
</div>

<div class="panel">
  <h3>Change password</h3>
  <c:if test="${not empty error}">
    <div class="alert alert-error"><c:out value="${error}"/></div>
  </c:if>
  <form method="post" action="${pageContext.request.contextPath}/student/profile" data-validate>
    <div class="field">
      <label>Current password</label>
      <input type="password" name="currentPassword" required>
    </div>
    <div class="form-row-2">
      <div class="field">
        <label>New password</label>
        <input type="password" name="newPassword" id="newPassword" required data-rule="password"
               data-error-message="At least 8 characters with a letter, number, and special character.">
        <div class="field-error"></div>
      </div>
      <div class="field">
        <label>Confirm new password</label>
        <input type="password" name="confirmPassword" required data-rule="confirmPassword" data-matches="newPassword">
        <div class="field-error"></div>
      </div>
    </div>
    <button class="btn-primary" style="width:auto; padding:11px 22px;" type="submit">Update password</button>
  </form>
</div>

<script src="${pageContext.request.contextPath}/js/validate.js"></script>
<%@ include file="/common/footer.jsp" %>
