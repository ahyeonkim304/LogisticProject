/** === 공통 알럿(alert) 자동소멸 === */

function hideAlert(element) {
    if (!element) return;
    element.classList.add('fade-out');          // CSS transition 실행 (0.4s)
    setTimeout(() => { element.remove(); }, 450); // 전환 완료 후 DOM 제거
}

/** JS에서 동적으로 토스트를 띄울 때 사용 */
function showToast(message, type = 'success') {
    const container = document.getElementById('alertContainer');
    if (!container) return;
    const el = document.createElement('div');
    el.className = `alert alert-${type} toast-message`;
    el.innerText = message;
    container.appendChild(el);
    setTimeout(() => hideAlert(el), 3000);
}

/** 페이지 로드 시 서버가 렌더링한 flash 알럿을 3초 후 자동 소멸 */
document.addEventListener('DOMContentLoaded', () => {
    document.querySelectorAll('.toast-message').forEach(el => {
        setTimeout(() => hideAlert(el), 3000);
    });
});
