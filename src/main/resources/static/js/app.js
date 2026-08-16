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

function renderPagination(el, pageData, onPage) {
  if (!el || !pageData) return;
  const { totalElements = 0, totalPages = 0, number = 0 } = pageData;
  if (totalPages <= 1) { el.innerHTML = `<div class="pagination-wrap"><span class="page-info">총 ${totalElements}건</span></div>`; return; }
  let btns = '';
  for (let i = 0; i < totalPages; i++)
    btns += `<button class="page-btn${i === number ? ' active' : ''}" data-page="${i}">${i + 1}</button>`;
  el.innerHTML = `<div class="pagination-wrap"><span class="page-info">총 ${totalElements}건 / ${totalPages}페이지</span><div class="page-buttons">${btns}</div></div>`;
  el.querySelectorAll('[data-page]').forEach(b => b.addEventListener('click', () => onPage(+b.dataset.page)));
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
