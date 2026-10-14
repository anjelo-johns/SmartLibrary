<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="pageTitle" value="Borrowing History" scope="request"/>
<c:set var="activeNav" value="history" scope="request"/>
<%@ include file="/common/header.jsp" %>

<div class="page-head">
  <div><h2>Borrowing history</h2><p>Every book you've returned so far.</p></div>
</div>

<table class="data">
  <thead><tr><th>Title</th><th>Issued</th><th>Returned</th><th>Fine charged</th></tr></thead>
  <tbody>
    <c:if test="${empty history}">
      <tr class="empty-row"><td colspan="4">Nothing returned yet.</td></tr>
    </c:if>
    <c:forEach var="l" items="${history}">
      <tr>
        <td><c:out value="${l.bookTitle}"/></td>
        <td><fmt:formatDate value="${l.issueDate}" pattern="dd MMM yyyy"/></td>
        <td><fmt:formatDate value="${l.returnDate}" pattern="dd MMM yyyy"/></td>
        <td>
          <c:choose>
            <c:when test="${l.fineAmount > 0}">Rs. <fmt:formatNumber value="${l.fineAmount}" pattern="0.00"/></c:when>
            <c:otherwise>&mdash;</c:otherwise>
          </c:choose>
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>

<%@ include file="/common/footer.jsp" %>
