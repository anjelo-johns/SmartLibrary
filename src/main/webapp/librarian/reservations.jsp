<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="pageTitle" value="Reservations" scope="request"/>
<c:set var="activeNav" value="reservations" scope="request"/>
<%@ include file="/common/header.jsp" %>

<div class="page-head">
  <div><h2>Reservations</h2><p>Every active hold across the collection, oldest first per book.</p></div>
</div>

<table class="data">
  <thead><tr><th>Title</th><th>Student</th><th>Reserved on</th><th>Queue #</th><th>Status</th></tr></thead>
  <tbody>
    <c:if test="${empty reservations}">
      <tr class="empty-row"><td colspan="5">No active reservations.</td></tr>
    </c:if>
    <c:forEach var="r" items="${reservations}">
      <tr>
        <td><c:out value="${r.bookTitle}"/></td>
        <td><c:out value="${r.studentName}"/></td>
        <td><fmt:formatDate value="${r.reservationDate}" pattern="dd MMM yyyy"/></td>
        <td>#<c:out value="${r.queuePosition}"/></td>
        <td>
          <c:choose>
            <c:when test="${r.status == 'READY'}"><span class="ok-pill">Ready for pickup</span></c:when>
            <c:otherwise>Waiting</c:otherwise>
          </c:choose>
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>

<%@ include file="/common/footer.jsp" %>
