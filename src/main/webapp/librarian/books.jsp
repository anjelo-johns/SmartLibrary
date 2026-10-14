<%@ page contentType="text/html;charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<c:set var="pageTitle" value="Manage Collection" scope="request"/>
<c:set var="activeNav" value="books" scope="request"/>
<%@ include file="/common/header.jsp" %>

<div class="page-head">
  <div><h2>Manage collection</h2><p>Add titles, adjust copy counts, or retire an entry.</p></div>
  <form method="get" action="${pageContext.request.contextPath}/librarian/books" class="search-box">
    <input type="text" name="q" placeholder="Search title, author, ISBN…" value="<c:out value="${q}"/>">
  </form>
</div>

<div class="panel">
  <h3>Add a book</h3>
  <form method="post" action="${pageContext.request.contextPath}/librarian/books" data-validate>
    <input type="hidden" name="action" value="add">
    <div class="form-row-2">
      <div class="field">
        <label>Title</label>
        <input type="text" name="title" required data-rule="none" placeholder="e.g. Effective Java">
      </div>
      <div class="field">
        <label>Author</label>
        <input type="text" name="author" required placeholder="e.g. Joshua Bloch">
      </div>
    </div>
    <div class="form-row-2">
      <div class="field">
        <label>ISBN (optional)</label>
        <input type="text" name="isbn" placeholder="e.g. 9780134685991">
      </div>
      <div class="field">
        <label>Category</label>
        <select name="categoryId" required>
          <option value="">Select a category</option>
          <c:forEach var="cat" items="${categories}">
            <option value="${cat.id}"><c:out value="${cat.categoryName}"/></option>
          </c:forEach>
        </select>
      </div>
    </div>
    <div class="form-row-2">
      <div class="field">
        <label>Publisher</label>
        <input type="text" name="publisher">
      </div>
      <div class="field">
        <label>Publication year</label>
        <input type="number" name="publicationYear" min="1400" max="2100">
      </div>
    </div>
    <div class="form-row-2">
      <div class="field">
        <label>Total copies</label>
        <input type="number" name="totalCopies" min="0" value="1" required>
      </div>
      <div class="field">
        <label>Description</label>
        <input type="text" name="description" placeholder="One-line summary (optional)">
      </div>
    </div>
    <button class="btn-primary" style="width:auto; padding:12px 22px;" type="submit">Add book</button>
  </form>
</div>

<table class="data">
  <thead><tr><th>Title</th><th>Author</th><th>Category</th><th>Total</th><th>Available</th><th></th></tr></thead>
  <tbody>
    <c:if test="${empty books}">
      <tr class="empty-row"><td colspan="6">No books found.</td></tr>
    </c:if>
    <c:forEach var="b" items="${books}">
      <tr>
        <td><c:out value="${b.title}"/></td>
        <td><c:out value="${b.author}"/></td>
        <td><c:out value="${b.categoryName}"/></td>
        <td><c:out value="${b.totalCopies}"/></td>
        <td><c:out value="${b.availableCopies}"/></td>
        <td style="white-space:nowrap;">
          <%-- Safe: data-* attributes carry all values; no JS string interpolation --%>
          <button type="button" class="btn-sm edit-book-btn" style="flex:none;"
                  data-id="${b.id}"
                  data-title="<c:out value='${b.title}'/>"
                  data-author="<c:out value='${b.author}'/>"
                  data-isbn="<c:out value='${b.isbn}'/>"
                  data-category-id="${b.categoryId}"
                  data-publisher="<c:out value='${b.publisher}'/>"
                  data-year="${b.publicationYear}"
                  data-copies="${b.totalCopies}"
                  data-description="<c:out value='${b.description}'/>">Edit</button>
          <form method="post" action="${pageContext.request.contextPath}/librarian/books" style="display:inline;" data-confirm="Remove this book from the collection?">
            <input type="hidden" name="action" value="delete">
            <input type="hidden" name="bookId" value="${b.id}">
            <button type="submit" class="btn-sm outline-rust" style="flex:none;">Remove</button>
          </form>
        </td>
      </tr>
    </c:forEach>
  </tbody>
</table>

<!-- Edit modal (shown/hidden via JS) -->
<div id="editOverlay" style="display:none; position:fixed; inset:0; background:rgba(31,42,36,0.5); z-index:50; align-items:center; justify-content:center;">
  <div class="panel" style="max-width:520px; width:92%; margin:0;">
    <h3>Edit book</h3>
    <form method="post" action="${pageContext.request.contextPath}/librarian/books" data-validate>
      <input type="hidden" name="action" value="edit">
      <input type="hidden" name="bookId" id="editId">
      <div class="form-row-2">
        <div class="field"><label>Title</label><input type="text" name="title" id="editTitle" required></div>
        <div class="field"><label>Author</label><input type="text" name="author" id="editAuthor" required></div>
      </div>
      <div class="form-row-2">
        <div class="field"><label>ISBN</label><input type="text" name="isbn" id="editIsbn"></div>
        <div class="field">
          <label>Category</label>
          <select name="categoryId" id="editCategory" required>
            <c:forEach var="cat" items="${categories}">
              <option value="${cat.id}"><c:out value="${cat.categoryName}"/></option>
            </c:forEach>
          </select>
        </div>
      </div>
      <div class="form-row-2">
        <div class="field"><label>Publisher</label><input type="text" name="publisher" id="editPublisher"></div>
        <div class="field"><label>Publication year</label><input type="number" name="publicationYear" id="editYear"></div>
      </div>
      <div class="form-row-2">
        <div class="field"><label>Total copies</label><input type="number" name="totalCopies" id="editCopies" min="0" required></div>
        <div class="field"><label>Description</label><input type="text" name="description" id="editDescription"></div>
      </div>
      <div style="display:flex; gap:10px;">
        <button class="btn-primary" style="width:auto; padding:11px 20px;" type="submit">Save changes</button>
        <button class="btn-sm" style="flex:none; padding:11px 18px;" type="button" id="closeEditBtn">Cancel</button>
      </div>
    </form>
  </div>
</div>

<script>
  // Delegated listener — reads field values safely from data-* attributes (no eval/interpolation)
  document.querySelectorAll('.edit-book-btn').forEach(function(btn) {
    btn.addEventListener('click', function() {
      var d = this.dataset;
      document.getElementById('editId').value          = d.id;
      document.getElementById('editTitle').value        = d.title;
      document.getElementById('editAuthor').value       = d.author;
      document.getElementById('editIsbn').value         = (d.isbn === 'null' || !d.isbn) ? '' : d.isbn;
      document.getElementById('editCategory').value     = d.categoryId;
      document.getElementById('editPublisher').value    = (d.publisher === 'null' || !d.publisher) ? '' : d.publisher;
      document.getElementById('editYear').value         = (d.year === 'null' || !d.year) ? '' : d.year;
      document.getElementById('editCopies').value       = d.copies;
      document.getElementById('editDescription').value  = (d.description === 'null' || !d.description) ? '' : d.description;
      document.getElementById('editOverlay').style.display = 'flex';
    });
  });
  document.getElementById('closeEditBtn').addEventListener('click', function() {
    document.getElementById('editOverlay').style.display = 'none';
  });
</script>

<%@ include file="/common/footer.jsp" %>
