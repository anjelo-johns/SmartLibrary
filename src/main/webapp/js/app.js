/* Small shared behaviors used across the app. */

// Confirmation dialog for destructive actions (delete book, cancel reservation, etc).
document.addEventListener('DOMContentLoaded', () => {
  document.querySelectorAll('[data-confirm]').forEach(el => {
    el.addEventListener('submit', (e) => {
      if (!window.confirm(el.dataset.confirm)) {
        e.preventDefault();
      }
    });
  });

  // Auto-hide flash banners after a few seconds.
  document.querySelectorAll('.alert[data-autohide]').forEach(el => {
    setTimeout(() => { el.style.display = 'none'; }, 5000);
  });
});

/**
 * Live catalog search: calls CatalogServlet with format=json and
 * re-renders the grid, so searching doesn't require a full page
 * reload. Falls back gracefully — the catalog page also works with
 * plain form submission (?q=...) if JavaScript is disabled.
 */
function liveSearchCatalog(inputEl, gridEl, contextPath, categoryId, onlyAvailable) {
  let timer = null;
  inputEl.addEventListener('input', () => {
    clearTimeout(timer);
    timer = setTimeout(() => {
      const params = new URLSearchParams({
        format: 'json',
        q: inputEl.value,
        categoryId: categoryId || '',
        onlyAvailable: onlyAvailable ? '1' : ''
      });
      fetch(contextPath + '/student/catalog?' + params.toString())
        .then(r => r.json())
        .then(books => renderBookCards(gridEl, books, contextPath))
        .catch(() => { /* silent fail — grid just stays as-is */ });
    }, 300);
  });
}

function renderBookCards(gridEl, books, contextPath) {
  if (!books.length) {
    gridEl.innerHTML = '<p style="color:var(--ink-soft); padding:20px;">No books match your search.</p>';
    return;
  }
  gridEl.innerHTML = books.map(b => `
    <div class="book-card">
      <div class="spine"></div>
      <div class="book-body">
        <div class="book-cat">${escapeHtml(b.category)}</div>
        <div class="book-title">${escapeHtml(b.title)}</div>
        <div class="book-author">${escapeHtml(b.author)}</div>
        <div class="book-meta">${b.totalCopies} ${b.totalCopies === 1 ? 'copy' : 'copies'} in collection</div>
        <div class="${b.available > 0 ? 'avail-yes' : 'avail-no'}">${b.available > 0 ? b.available + ' available' : 'All copies out'}</div>
        <div class="book-actions">
          ${b.available > 0
            ? `<form method="post" action="${contextPath}/student/issue" style="flex:1;">
                 <input type="hidden" name="bookId" value="${b.id}">
                 <button type="submit" class="btn-sm filled" style="width:100%;">Issue to me</button>
               </form>`
            : `<form method="post" action="${contextPath}/student/reservations" style="flex:1;">
                 <input type="hidden" name="action" value="create">
                 <input type="hidden" name="bookId" value="${b.id}">
                 <button type="submit" class="btn-sm" style="width:100%;">Reserve</button>
               </form>`}
        </div>
      </div>
    </div>
  `).join('');
}

function escapeHtml(s) {
  const div = document.createElement('div');
  div.textContent = s || '';
  return div.innerHTML;
}
