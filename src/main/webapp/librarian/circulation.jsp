<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="pageTitle" value="Circulation Desk" scope="request"/>
<c:set var="activeNav" value="circulation" scope="request"/>
<%@ include file="/common/header.jsp" %>

<div class="page-head">
  <div><h2>Circulation desk</h2><p>Issue and return copies against a student's register number.</p></div>
</div>

<div class="panel">
  <h3>Issue a book</h3>
  <form method="post" action="${pageContext.request.contextPath}/librarian/issue" data-validate>
    <div class="form-row-2">
      <div class="field">
        <label>Register number</label>
        <input type="text" name="registerNumber" required data-rule="registerNumber" data-error-message="Enter a valid register number." placeholder="e.g. STU001">
        <div class="field-error"></div>
      </div>
      <div class="field">
        <label>Book</label>
        <select name="bookId" required>
          <c:forEach var="b" items="${books}">
            <option value="${b.id}" ${b.availableCopies <= 0 ? 'disabled' : ''}>
              <c:out value="${b.title}"/> (${b.availableCopies} available)
            </option>
          </c:forEach>
        </select>
      </div>
    </div>
    <button class="btn-primary" style="width:auto; padding:12px 22px;" type="submit">Issue book</button>
  </form>
</div>

<div class="panel">
  <h3>Active loans</h3>
  <table class="data">
    <thead><tr><th>Title</th><th>Student</th><th>Issued</th><th>Due</th><th>Fine</th><th></th></tr></thead>
    <tbody>
      <c:if test="${empty activeLoans}">
        <tr class="empty-row"><td colspan="6">No active loans.</td></tr>
      </c:if>
      <c:forEach var="l" items="${activeLoans}">
        <tr>
          <td><c:out value="${l.bookTitle}"/></td>
          <td><c:out value="${l.studentName}"/> (<c:out value="${l.studentRegNo}"/>)</td>
          <td><fmt:formatDate value="${l.issueDate}" pattern="dd MMM yyyy"/></td>
          <td><fmt:formatDate value="${l.dueDate}" pattern="dd MMM yyyy"/></td>
          <td>
            <c:choose>
              <c:when test="${l.fineAmount > 0}"><span class="fine-pill">Rs. <fmt:formatNumber value="${l.fineAmount}" pattern="0.00"/></span></c:when>
              <c:otherwise>&mdash;</c:otherwise>
            </c:choose>
          </td>
          <td>
            <form method="post" action="${pageContext.request.contextPath}/librarian/return">
              <input type="hidden" name="loanId" value="${l.id}">
              <button type="submit" class="btn-sm">Mark returned</button>
            </form>
          </td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<%@ include file="/common/footer.jsp" %>
