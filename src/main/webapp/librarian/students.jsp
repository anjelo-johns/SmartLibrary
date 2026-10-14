<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<c:set var="pageTitle" value="Students" scope="request"/>
<c:set var="activeNav" value="students" scope="request"/>
<%@ include file="/common/header.jsp" %>

<div class="page-head">
  <div><h2>Students</h2><p>Search the student directory or open a profile to see full borrowing history.</p></div>
  <form method="get" action="${pageContext.request.contextPath}/librarian/students" class="search-box">
    <input type="text" name="q" placeholder="Search name, register number, email…" value="<c:out value="${q}"/>">
  </form>
</div>

<table class="data">
  <thead><tr><th>Name</th><th>Register no.</th><th>Email</th><th>Department</th><th>Year</th><th></th></tr></thead>
  <tbody>
    <c:if test="${empty students}">
      <tr class="empty-row"><td colspan="6">No students found.</td></tr>
    </c:if>
    <c:forEach var="s" items="${students}">
      <tr>
        <td><c:out value="${s.fullName}"/></td>
        <td><c:out value="${s.registerNumber}"/></td>
        <td><c:out value="${s.email}"/></td>
        <td><c:out value="${s.department}"/></td>
        <td><c:out value="${s.yearSem}"/></td>
        <td><a class="btn-sm" style="flex:none;" href="${pageContext.request.contextPath}/librarian/students?id=${s.id}">View history</a></td>
      </tr>
    </c:forEach>
  </tbody>
</table>

<%@ include file="/common/footer.jsp" %>
