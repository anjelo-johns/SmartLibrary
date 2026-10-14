<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="pageTitle" value="Reports" scope="request"/>
<c:set var="activeNav" value="reports" scope="request"/>
<%@ include file="/common/header.jsp" %>

<div class="page-head">
  <div><h2>Reports</h2><p>A snapshot of activity across the library.</p></div>
</div>

<div class="stat-row">
  <div class="stat"><div class="num"><c:out value="${activeLoans.size()}"/></div><div class="lab">Currently issued</div></div>
  <div class="stat"><div class="num"><c:out value="${overdueLoans.size()}"/></div><div class="lab">Overdue</div></div>
  <div class="stat"><div class="num">Rs. <fmt:formatNumber value="${outstandingFines}" pattern="0.00"/></div><div class="lab">Outstanding fines</div></div>
  <div class="stat"><div class="num">Rs. <fmt:formatNumber value="${totalCollected}" pattern="0.00"/></div><div class="lab">Fines collected</div></div>
  <div class="stat"><div class="num"><c:out value="${activeReservations.size()}"/></div><div class="lab">Active reservations</div></div>
</div>

<div class="panel">
  <h3>Most borrowed books</h3>
  <table class="data">
    <thead><tr><th>Title</th><th>Times borrowed</th></tr></thead>
    <tbody>
      <c:if test="${empty mostBorrowed}">
        <tr class="empty-row"><td colspan="2">No borrowing activity yet.</td></tr>
      </c:if>
      <c:forEach var="row" items="${mostBorrowed}">
        <tr><td><c:out value="${row[0]}"/></td><td><c:out value="${row[1]}"/></td></tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<div class="panel">
  <h3>Currently issued</h3>
  <table class="data">
    <thead><tr><th>Title</th><th>Student</th><th>Due date</th></tr></thead>
    <tbody>
      <c:if test="${empty activeLoans}">
        <tr class="empty-row"><td colspan="3">Nothing currently issued.</td></tr>
      </c:if>
      <c:forEach var="l" items="${activeLoans}">
        <tr>
          <td><c:out value="${l.bookTitle}"/></td>
          <td><c:out value="${l.studentName}"/> (<c:out value="${l.studentRegNo}"/>)</td>
          <td><fmt:formatDate value="${l.dueDate}" pattern="dd MMM yyyy"/></td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<div class="panel">
  <h3>Overdue books</h3>
  <table class="data">
    <thead><tr><th>Title</th><th>Student</th><th>Due date</th></tr></thead>
    <tbody>
      <c:if test="${empty overdueLoans}">
        <tr class="empty-row"><td colspan="3">Nobody is overdue.</td></tr>
      </c:if>
      <c:forEach var="l" items="${overdueLoans}">
        <tr>
          <td><c:out value="${l.bookTitle}"/></td>
          <td><c:out value="${l.studentName}"/> (<c:out value="${l.studentRegNo}"/>)</td>
          <td><fmt:formatDate value="${l.dueDate}" pattern="dd MMM yyyy"/></td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<div class="panel">
  <h3>Active reservations</h3>
  <table class="data">
    <thead><tr><th>Title</th><th>Student</th><th>Queue #</th></tr></thead>
    <tbody>
      <c:if test="${empty activeReservations}">
        <tr class="empty-row"><td colspan="3">No active reservations.</td></tr>
      </c:if>
      <c:forEach var="r" items="${activeReservations}">
        <tr>
          <td><c:out value="${r.bookTitle}"/></td>
          <td><c:out value="${r.studentName}"/></td>
          <td>#<c:out value="${r.queuePosition}"/></td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<%@ include file="/common/footer.jsp" %>
