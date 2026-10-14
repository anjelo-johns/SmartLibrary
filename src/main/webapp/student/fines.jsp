<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="pageTitle" value="Fines" scope="request"/>
<c:set var="activeNav" value="fines" scope="request"/>
<%@ include file="/common/header.jsp" %>

<div class="page-head">
  <div><h2>Fines</h2><p>Fine rate is Rs. 5 per day overdue.</p></div>
</div>

<div class="panel">
  <h3>All loans with a fine</h3>
  <table class="data">
    <thead><tr><th>Title</th><th>Due date</th><th>Fine</th><th>Status</th></tr></thead>
    <tbody>
      <c:set var="anyFines" value="false"/>
      <c:forEach var="l" items="${loans}">
        <c:if test="${l.fineAmount > 0}">
          <c:set var="anyFines" value="true"/>
          <tr>
            <td><c:out value="${l.bookTitle}"/></td>
            <td><fmt:formatDate value="${l.dueDate}" pattern="dd MMM yyyy"/></td>
            <td>Rs. <fmt:formatNumber value="${l.fineAmount}" pattern="0.00"/></td>
            <td>
              <c:choose>
                <c:when test="${l.finePaid}"><span class="ok-pill">Paid</span></c:when>
                <c:otherwise><span class="fine-pill">Outstanding</span></c:otherwise>
              </c:choose>
            </td>
          </tr>
        </c:if>
      </c:forEach>
      <c:if test="${anyFines == 'false'}">
        <tr class="empty-row"><td colspan="4">No fines on record. Keep it that way!</td></tr>
      </c:if>
    </tbody>
  </table>
</div>

<div class="panel">
  <h3>Payment history</h3>
  <table class="data">
    <thead><tr><th>Amount</th><th>Paid on</th></tr></thead>
    <tbody>
      <c:if test="${empty payments}">
        <tr class="empty-row"><td colspan="2">No payments recorded yet.</td></tr>
      </c:if>
      <c:forEach var="p" items="${payments}">
        <tr>
          <td>Rs. <fmt:formatNumber value="${p.amount}" pattern="0.00"/></td>
          <td><fmt:formatDate value="${p.paymentDate}" pattern="dd MMM yyyy, hh:mm a"/></td>
        </tr>
      </c:forEach>
    </tbody>
  </table>
</div>

<%@ include file="/common/footer.jsp" %>
