/* WMS 공통 유틸리티 */

const STATUS_LABELS = {
  ORDER_CREATED: '주문 생성', ORDER_HOLD: '보류',
  INBOUND_PENDING: '입고 대기', INBOUND_HOLD: '입고 보류', INBOUND_COMPLETED: '입고 완료',
  OUTBOUND_PENDING: '출고 대기', OUTBOUND_HOLD: '출고 보류', OUTBOUND_COMPLETED: '출고 완료',
  SHIPPED: '출하 완료',
};

function showToast(text, type = 'success') {
  if (!text) return;
  let c = document.getElementById('toast-container');
  if (!c) { c = document.createElement('div'); c.id = 'toast-container'; c.className = 'alert-container'; document.body.appendChild(c); }
  const el = document.createElement('div');
  el.className = `alert alert-${type} toast-message`;
  el.textContent = text;
  c.appendChild(el);
  setTimeout(() => el.remove(), 3500);
}

async function apiFetch(method, path, data) {
  const opts = { method, headers: { 'Content-Type': 'application/json' }, credentials: 'same-origin' };
  if (data != null) opts.body = JSON.stringify(data);
  const res = await fetch('/api' + path, opts);
  if (res.status === 401) { window.location.href = '/login'; return null; }
  if (res.status === 204) return null;
  const text = await res.text();
  const json = text ? JSON.parse(text) : null;
  if (!res.ok) throw new Error((json && (json.message || json.error)) || '오류가 발생했습니다.');
  return json;
}

function renderPagination(el, pageData, onPage, onSize) {
  if (!el || !pageData) return;
  const { totalElements = 0, totalPages = 0, number = 0, size = 20 } = pageData;

  const start = totalElements === 0 ? 0 : number * size + 1;
  const end   = Math.min((number + 1) * size, totalElements);
  const infoText = totalElements === 0
    ? '데이터가 없습니다'
    : `총 ${totalElements.toLocaleString()}건 중 ${start}-${end}건`;

  // 페이지 버튼: 현재 페이지 기준 앞뒤 2개씩 + 처음/끝
  const WINDOW = 2;
  let btns = '';
  btns += `<button class="page-btn" data-page="0" ${number === 0 ? 'disabled' : ''}>&laquo;</button>`;
  btns += `<button class="page-btn" data-page="${number - 1}" ${number === 0 ? 'disabled' : ''}>&lsaquo;</button>`;
  if (totalPages > 0) {
    const lo = Math.max(0, number - WINDOW);
    const hi = Math.min(totalPages - 1, number + WINDOW);
    if (lo > 0) btns += `<button class="page-btn" data-page="0">1</button>${lo > 1 ? '<span style="padding:0 4px;color:var(--text-muted)">…</span>' : ''}`;
    for (let i = lo; i <= hi; i++)
      btns += `<button class="page-btn${i === number ? ' active' : ''}" data-page="${i}">${i + 1}</button>`;
    if (hi < totalPages - 1) btns += `${hi < totalPages - 2 ? '<span style="padding:0 4px;color:var(--text-muted)">…</span>' : ''}<button class="page-btn" data-page="${totalPages - 1}">${totalPages}</button>`;
  }
  btns += `<button class="page-btn" data-page="${number + 1}" ${number >= totalPages - 1 ? 'disabled' : ''}>&rsaquo;</button>`;
  btns += `<button class="page-btn" data-page="${totalPages - 1}" ${number >= totalPages - 1 ? 'disabled' : ''}>&raquo;</button>`;

  const sizeOptions = [20, 50, 100].map(v =>
    `<option value="${v}" ${v === size ? 'selected' : ''}>${v}개씩</option>`).join('');

  el.innerHTML = `
    <div class="pagination-wrap">
      <span class="page-info">${infoText}</span>
      <div class="page-buttons">${btns}</div>
      <div class="page-size-wrap">
        <select class="page-size-select" id="page-size-select">${sizeOptions}</select>
      </div>
    </div>`;

  el.querySelectorAll('[data-page]').forEach(b => {
    if (!b.disabled) b.addEventListener('click', () => onPage(+b.dataset.page));
  });
  const sizeEl = el.querySelector('#page-size-select');
  if (sizeEl && onSize) sizeEl.addEventListener('change', () => onSize(+sizeEl.value));
}

const STATUS_BADGE_CLASS = {
  ORDER_CREATED:       'badge-created',
  READY_TO_SHIP:       'badge-created',
  INBOUND_PENDING:     'badge-pending',
  OUTBOUND_PENDING:    'badge-pending',
  ORDER_HOLD:          'badge-hold',
  INBOUND_HOLD:        'badge-hold',
  OUTBOUND_HOLD:       'badge-hold',
  INBOUND_COMPLETED:   'badge-done',
  OUTBOUND_COMPLETED:  'badge-done',
  SHIPPED:             'badge-shipped',
};

function statusBadge(status) {
  const label = STATUS_LABELS[status] || status || '-';
  const cls   = STATUS_BADGE_CLASS[status] || 'badge-muted';
  return `<span class="badge ${cls}">${escHtml(label)}</span>`;
}

function renderEmptyState(label = '조회된 데이터가 없습니다', onReset) {
  const btn = onReset
    ? `<button class="btn btn-secondary btn-sm" onclick="(${onReset.toString()})()">필터 초기화</button>`
    : '';
  return `<div class="empty-state">
    <div class="empty-state-icon">📦</div>
    <div class="empty-state-text">${label}</div>
    <div class="empty-state-sub">검색 조건을 변경하거나 새로 등록해 주세요.</div>
    ${btn}
  </div>`;
}

function fmtDate(dt) { return dt ? dt.replace('T', ' ').substring(0, 16) : '-'; }

function escHtml(s) {
  if (s == null) return '';
  return String(s).replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;');
}

/* 사이드바 현재 메뉴 활성화 */
document.addEventListener('DOMContentLoaded', () => {
  const path = window.location.pathname;
  document.querySelectorAll('.sidebar-menu a').forEach(a => {
    const href = a.getAttribute('href');
    const exact = a.dataset.exact === 'true';
    const match = exact ? path === href : (href !== '/' && path.startsWith(href)) || (href === '/' && path === '/');
    if (match) a.classList.add('active');
  });
});
