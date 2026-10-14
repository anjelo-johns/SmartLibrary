<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Catalog" scope="request"/>
<c:set var="activeNav" value="catalog" scope="request"/>
<%@ include file="/common/header.jsp" %>

<div class="page-head">
  <div>
    <h2>Catalog</h2>
    <p>Search the full collection and issue or reserve a copy.</p>
  </div>
  <form method="get" action="${pageContext.request.contextPath}/student/catalog" class="search-box" id="catalogForm">
    <input type="text" name="q" id="catalogSearch" placeholder="Search title, author, ISBN, category…" value="<c:out value="${q}"/>">
  </form>
</div>

<div style="display:flex; gap:14px; margin-bottom:20px; flex-wrap:wrap;">
  <form method="get" action="${pageContext.request.contextPath}/student/catalog" style="display:flex; gap:10px; align-items:center;">
    <input type="hidden" name="q" value="<c:out value="${q}"/>">
    <select name="categoryId" onchange="this.form.submit()" style="padding:9px 12px; border:1px solid var(--line);">
      <option value="">All categories</option>
      <c:forEach var="cat" items="${categories}">
        <option value="${cat.id}" ${categoryId == cat.id ? 'selected' : ''}><c:out value="${cat.categoryName}"/></option>
      </c:forEach>
    </select>
    <label style="font-size:13px; display:flex; align-items:center; gap:6px;">
      <input type="checkbox" name="onlyAvailable" value="1" ${onlyAvailable ? 'checked' : ''} onchange="this.form.submit()">
      Available only
    </label>
  </form>
</div>

<div class="catalog-grid" id="catalogGrid">
  <c:if test="${empty books}">
    <p style="color:var(--ink-soft);">No books match your search.</p>
  </c:if>
  <c:forEach var="b" items="${books}">
    <div class="book-card">
      <div class="spine"></div>
      <div class="book-body">
        <div class="book-cat"><c:out value="${b.categoryName}"/></div>
        <div class="book-title"><c:out value="${b.title}"/></div>
        <div class="book-author"><c:out value="${b.author}"/></div>
        <div class="book-meta">${b.totalCopies} ${b.totalCopies == 1 ? 'copy' : 'copies'} in collection</div>
        <div class="${b.availableCopies > 0 ? 'avail-yes' : 'avail-no'}">
          <c:choose>
            <c:when test="${b.availableCopies > 0}"><c:out value="${b.availableCopies}"/> available</c:when>
            <c:otherwise>All copies out</c:otherwise>
          </c:choose>
        </div>
        <div class="book-actions">
          <c:choose>
            <c:when test="${b.availableCopies > 0}">
              <form method="post" action="${pageContext.request.contextPath}/student/issue" style="flex:1;">
                <input type="hidden" name="bookId" value="${b.id}">
                <button type="submit" class="btn-sm filled" style="width:100%;">Issue to me</button>
              </form>
            </c:when>
            <c:otherwise>
              <form method="post" action="${pageContext.request.contextPath}/student/reservations" style="flex:1;">
                <input type="hidden" name="action" value="create">
                <input type="hidden" name="bookId" value="${b.id}">
                <button type="submit" class="btn-sm" style="width:100%;">Reserve</button>
              </form>
            </c:otherwise>
          </c:choose>
        </div>
      </div>
    </div>
  </c:forEach>
</div>

<script>
  // Wire up live search — the function is defined in app.js (already loaded via footer)
  document.addEventListener('DOMContentLoaded', function() {
    var inputEl = document.getElementById('catalogSearch');
    var gridEl  = document.getElementById('catalogGrid');
    if (inputEl && gridEl) {
      var categoryId    = '<c:out value="${categoryId}"/>';
      var onlyAvailable = '${onlyAvailable}' === 'true';
      liveSearchCatalog(inputEl, gridEl, '${pageContext.request.contextPath}', categoryId, onlyAvailable);
    }
  });
</script>
<%@ include file="/common/footer.jsp" %>
