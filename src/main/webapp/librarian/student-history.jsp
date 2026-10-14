<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="pageTitle" value="Student History" scope="request"/>
<c:set var="activeNav" value="students" scope="request"/>
<%@ include file="/common/header.jsp" %>

<div class="page-head">
  <div>
    <h2><c:out value="${student.fullName}"/></h2>
    <p><c:out value="${student.registerNumber}"/> &middot; <c:out value="${student.email}"/> &middot; <c:out value="${student.department}"/></p>
  </div>
  <a class="btn-outline" href="${pageContext.request.contextPath}/librarian/students">Back to students</a>
</div>

<table class="data">
  <thead><tr><th>Title</th><th>Issued</th><th>Due</th><th>Returned</th><th>Status</th><th>Fine</th></tr></thead>
  <tbody>
    <c:if test="${empty loans}">
      <tr class="empty-row"><td colspan="6">No loan history for this student.</td></tr>
    </c:if>
    <c:forEach var="l" items="${loans}">
      <tr>
        <td><c:out value="${l.bookTitle}"/></td>
        <td><fmt:formatDate value="${l.issueDate}" pattern="dd MMM yyyy"/></td>
        <td><fmt:formatDate value="${l.dueDate}" pattern="dd MMM yyyy"/></td>
        <td><c:choose><c:when test="${not empty l.returnDate}"><fmt:formatDate value="${l.returnDate}" pattern="dd MMM yyyy"/></c:when><c:otherwise>&mdash;</c:otherwise></c:choose></td>
        <td><c:out value="${l.status}"/></td>
        <td>
          <c:choose>
            <c:when test="${l.fineAmount > 0}">Rs. <fmt:formatNumber value="${l.fineAmount}" pattern="0.00"/> ${l.finePaid ? '(paid)' : '(due)'}</c:when>
            <c:otherwise>&mdash;</c:otherwise>
          </c:choose>
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>

<%@ include file="/common/footer.jsp" %>
