<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="pageTitle" value="My Books" scope="request"/>
<c:set var="activeNav" value="mybooks" scope="request"/>
<%@ include file="/common/header.jsp" %>

<div class="page-head">
  <div>
    <h2>My books</h2>
    <p>Everything you currently have on loan, and what it'll cost you if it's late.</p>
  </div>
</div>

<table class="data">
  <thead><tr><th>Title</th><th>Issued</th><th>Due</th><th>Status</th><th>Fine</th><th></th></tr></thead>
  <tbody>
    <c:if test="${empty activeLoans}">
      <tr class="empty-row"><td colspan="6">No books on loan right now — browse the <a href="${pageContext.request.contextPath}/student/catalog" style="color:var(--moss-deep); font-weight:600;">catalog</a> to issue one.</td></tr>
    </c:if>
    <c:forEach var="l" items="${activeLoans}">
      <tr>
        <td><c:out value="${l.bookTitle}"/></td>
        <td><fmt:formatDate value="${l.issueDate}" pattern="dd MMM yyyy"/></td>
        <td><fmt:formatDate value="${l.dueDate}" pattern="dd MMM yyyy"/></td>
        <td>
          <c:choose>
            <c:when test="${l.fineAmount > 0}"><span class="fine-pill">Overdue</span></c:when>
            <c:otherwise><span class="ok-pill">On time</span></c:otherwise>
          </c:choose>
        </td>
        <td>
          <c:choose>
            <c:when test="${l.fineAmount > 0}">Rs. <fmt:formatNumber value="${l.fineAmount}" pattern="0.00"/></c:when>
            <c:otherwise>&mdash;</c:otherwise>
          </c:choose>
        </td>
        <td>
          <form method="post" action="${pageContext.request.contextPath}/student/return">
            <input type="hidden" name="loanId" value="${l.id}">
            <button type="submit" class="btn-sm">Return</button>
          </form>
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>

<%@ include file="/common/footer.jsp" %>
