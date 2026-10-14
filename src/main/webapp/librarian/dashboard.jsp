<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="pageTitle" value="Dashboard" scope="request"/>
<c:set var="activeNav" value="dashboard" scope="request"/>
<%@ include file="/common/header.jsp" %>

<div class="page-head">
  <div><h2>Library overview</h2><p>Live figures across the whole collection.</p></div>
</div>

<div class="stat-row">
  <div class="stat"><div class="num"><c:out value="${totalBooks}"/></div><div class="lab">Distinct titles</div></div>
  <div class="stat"><div class="num"><c:out value="${totalCopies}"/></div><div class="lab">Total copies</div></div>
  <div class="stat"><div class="num"><c:out value="${availableCopies}"/></div><div class="lab">Available now</div></div>
  <div class="stat"><div class="num"><c:out value="${totalStudents}"/></div><div class="lab">Registered students</div></div>
</div>
<div class="stat-row">
  <div class="stat"><div class="num"><c:out value="${activeLoans.size()}"/></div><div class="lab">Books currently issued</div></div>
  <div class="stat"><div class="num"><c:out value="${overdueLoans.size()}"/></div><div class="lab">Overdue right now</div></div>
  <div class="stat"><div class="num"><c:out value="${activeReservations}"/></div><div class="lab">Active reservations</div></div>
  <div class="stat"><div class="num">Rs. <fmt:formatNumber value="${outstandingFines}" pattern="0.00"/></div><div class="lab">Outstanding fines</div></div>
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
  <h3>Overdue right now</h3>
  <table class="data">
    <thead><tr><th>Student</th><th>Title</th><th>Due date</th></tr></thead>
    <tbody>
      <c:if test="${empty overdueLoans}">
        <tr class="empty-row"><td colspan="3">Nobody is overdue right now.</td></tr>
      </c:if>
      <c:forEach var="l" items="${overdueLoans}" end="9">
        <tr>
          <td><c:out value="${l.studentName}"/> (<c:out value="${l.studentRegNo}"/>)</td>
          <td><c:out value="${l.bookTitle}"/></td>
          <td><fmt:formatDate value="${l.dueDate}" pattern="dd MMM yyyy"/></td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<%@ include file="/common/footer.jsp" %>
