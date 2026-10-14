<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="pageTitle" value="Dashboard" scope="request"/>
<c:set var="activeNav" value="dashboard" scope="request"/>
<%@ include file="/common/header.jsp" %>

<div class="page-head">
  <div>
    <h2>Welcome back, <c:out value="${sessionScope.user.fullName}"/></h2>
    <p>Here's where things stand with your loans right now.</p>
  </div>
</div>

<div class="stat-row">
  <div class="stat"><div class="num"><c:out value="${totalBorrowed}"/></div><div class="lab">Books on loan</div></div>
  <div class="stat"><div class="num"><c:out value="${dueSoon}"/></div><div class="lab">Due within 3 days</div></div>
  <div class="stat"><div class="num"><c:out value="${overdueCount}"/></div><div class="lab">Overdue</div></div>
  <div class="stat"><div class="num">Rs. <fmt:formatNumber value="${outstandingFine}" pattern="0.00"/></div><div class="lab">Outstanding fine</div></div>
  <div class="stat"><div class="num"><c:out value="${activeReservations}"/></div><div class="lab">Active reservations</div></div>
</div>

<div class="panel">
  <h3>Currently borrowed</h3>
  <table class="data">
    <thead><tr><th>Title</th><th>Issued</th><th>Due</th><th></th></tr></thead>
    <tbody>
      <c:if test="${empty activeLoans}">
        <tr class="empty-row"><td colspan="4">Nothing on loan — browse the <a href="${pageContext.request.contextPath}/student/catalog" style="color:var(--moss-deep); font-weight:600;">catalog</a> to get started.</td></tr>
      </c:if>
      <c:forEach var="l" items="${activeLoans}">
        <tr>
          <td><c:out value="${l.bookTitle}"/></td>
          <td><fmt:formatDate value="${l.issueDate}" pattern="dd MMM yyyy"/></td>
          <td><fmt:formatDate value="${l.dueDate}" pattern="dd MMM yyyy"/></td>
          <td><a href="${pageContext.request.contextPath}/student/my-books" class="btn-sm" style="flex:none;">View</a></td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<div class="panel">
  <h3>Recently returned</h3>
  <table class="data">
    <thead><tr><th>Title</th><th>Returned</th></tr></thead>
    <tbody>
      <c:if test="${empty recentHistory}">
        <tr class="empty-row"><td colspan="2">Nothing returned yet.</td></tr>
      </c:if>
      <c:forEach var="l" items="${recentHistory}" varStatus="st" end="4">
        <tr>
          <td><c:out value="${l.bookTitle}"/></td>
          <td><fmt:formatDate value="${l.returnDate}" pattern="dd MMM yyyy"/></td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<%@ include file="/common/footer.jsp" %>
