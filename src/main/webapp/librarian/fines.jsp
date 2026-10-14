<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="pageTitle" value="Fines" scope="request"/>
<c:set var="activeNav" value="fines" scope="request"/>
<%@ include file="/common/header.jsp" %>

<div class="page-head">
  <div><h2>Fines &amp; overdue</h2><p>Everyone currently past their due date, and what's owed.</p></div>
</div>

<div class="stat-row">
  <div class="stat"><div class="num"><c:out value="${overdueLoans.size()}"/></div><div class="lab">Overdue loans</div></div>
  <div class="stat"><div class="num">Rs. <fmt:formatNumber value="${totalOutstanding}" pattern="0.00"/></div><div class="lab">Outstanding</div></div>
  <div class="stat"><div class="num">Rs. <fmt:formatNumber value="${totalCollected}" pattern="0.00"/></div><div class="lab">Total collected all-time</div></div>
</div>

<table class="data">
  <thead><tr><th>Student</th><th>Title</th><th>Status</th><th>Due date</th><th>Fine owed</th><th>Record payment</th></tr></thead>
  <tbody>
    <c:if test="${empty overdueLoans}">
      <tr class="empty-row"><td colspan="6">No outstanding fines right now.</td></tr>
    </c:if>
    <c:forEach var="l" items="${overdueLoans}">
      <tr>
        <td><c:out value="${l.studentName}"/> (<c:out value="${l.studentRegNo}"/>)</td>
        <td><c:out value="${l.bookTitle}"/></td>
        <td>
          <c:choose>
            <c:when test="${not empty l.returnDate}"><span class="ok-pill" style="background:#E2ECF5; color:#1C4E80;">Returned</span></c:when>
            <c:otherwise><span class="fine-pill">Overdue</span></c:otherwise>
          </c:choose>
        </td>
        <td><fmt:formatDate value="${l.dueDate}" pattern="dd MMM yyyy"/></td>
        <td><span class="fine-pill">Rs. <fmt:formatNumber value="${l.fineAmount}" pattern="0.00"/></span></td>
        <td>
          <form method="post" action="${pageContext.request.contextPath}/librarian/fines" style="display:flex; gap:8px;">
            <input type="hidden" name="loanId" value="${l.id}">
            <input type="number" step="0.01" min="0.01" name="amount" value="<fmt:formatNumber value='${l.fineAmount}' pattern='0.00'/>" style="width:100px; padding:6px 8px; border:1px solid var(--line);">
            <button type="submit" class="btn-sm" style="flex:none;">Mark paid</button>
          </form>
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>

<%@ include file="/common/footer.jsp" %>
