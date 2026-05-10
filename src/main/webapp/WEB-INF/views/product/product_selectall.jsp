<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c"   uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="fmt" uri="http://java.sun.com/jsp/jstl/fmt" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="../common/head.jsp" %>
    <title>상품 관리 | Main Fulfillment</title>
</head>
<body>
<c:set var="menu" value="product" />
<c:set var="ctx" value="${pageContext.request.contextPath}/api/v1/main-fulfillment/product" />

<div class="app-shell">
    <%@ include file="../common/sidebar.jsp" %>
    <div class="app-main">
        <%@ include file="../common/header.jsp" %>
        <div class="content-wrapper">

            <%-- ============ 상단 제목 + 우측 액션 ============ --%>
            <div class="page-header">
                <div class="list-title-block">
                    <h2>상품 리스트</h2>
                    <div class="subtitle">상품 정보를 조회하고 관리하는 화면입니다.</div>
                </div>
                <div class="flex gap-8">
                    <button type="button" class="btn btn-default" onclick="downloadTemplate();">양식 다운로드</button>
                    <button type="button" class="btn btn-default" onclick="openUploadDialog();">일괄 등록</button>
                    <button type="button" class="btn btn-default" onclick="downloadSelected();">선택 내역 다운로드</button>
                    <button type="button" class="btn btn-primary" onclick="openCreateDrawer();">+ 상품 등록</button>
                </div>
            </div>

            <%-- ============ 카드 ============ --%>
            <div class="card">

                <div class="filter-row">
                    <div class="filter-cell">
                        <label>검색 조건</label>
                        <select id="searchType">
                            <option value="0">상품코드 + 이름</option>
                            <option value="1">상품코드</option>
                            <option value="2">이름</option>
                        </select>
                    </div>
                    <div class="filter-cell">
                        <label>검색어</label>
                        <input type="text" id="searchKeyword" placeholder="검색어 입력" />
                    </div>
                </div>

                <div class="flex gap-8 mb-20">
                    <button type="button" class="btn btn-primary btn-sm" onclick="searchProduct();">조회</button>
                    <button type="button" class="btn btn-default btn-sm" onclick="location.reload();">초기화</button>
                </div>

                <table class="data-table" id="productTable">
                    <thead>
                        <tr>
                            <th class="center" style="width:36px;"><input type="checkbox" id="checkAll" /></th>
                            <th>상품코드</th>
                            <th>상품명</th>
                            <th class="center">재고</th>
                            <th class="center">안전재고</th>
                            <th class="center">리드타임(일)</th>
                            <th>이미지</th>
                            <th>등록일</th>
                            <th class="center">관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty products}">
                                <tr><td colspan="9" class="table-empty">등록된 상품이 없습니다.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${products}" var="p">
                                    <tr>
                                        <td class="center"><input type="checkbox" name="rowCheck" value="${p.productCode}" /></td>
                                        <td>${p.productCode}</td>
                                        <td>${p.name}</td>
                                        <td class="center">${p.productStock}</td>
                                        <td class="center">${p.safetyStock}</td>
                                        <td class="center">${p.leadTime}</td>
                                        <td>${p.image}</td>
                                        <td>
                                            <c:if test="${not empty p.create_at}">
                                                <fmt:formatDate value="${p.create_at}" pattern="yyyy-MM-dd HH:mm" />
                                            </c:if>
                                            <c:if test="${empty p.create_at}">-</c:if>
                                        </td>
                                        <td class="center">
                                            <form action="${ctx}/updatePage" method="post" style="display:inline;">
                                                <input type="hidden" name="productCode" value="${p.productCode}" />
                                                <input type="hidden" name="name"        value="${p.name}" />
                                                <input type="hidden" name="image"       value="${p.image}" />
                                                <input type="hidden" name="productStock" value="${p.productStock}" />
                                                <input type="hidden" name="safetyStock" value="${p.safetyStock}" />
                                                <input type="hidden" name="leadTime"    value="${p.leadTime}" />
                                                <button type="submit" class="btn btn-sm">수정</button>
                                            </form>
                                            <form action="${ctx}/delete" method="post" style="display:inline;"
                                                  onsubmit="return confirm('정말 삭제하시겠습니까?');">
                                                <input type="hidden" name="productCode" value="${p.productCode}" />
                                                <button type="submit" class="btn btn-sm btn-danger">삭제</button>
                                            </form>
                                        </td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>

                <div class="pagination">
                    <div class="pager">
                        <button type="button" class="page-btn">«</button>
                        <button type="button" class="page-btn">‹</button>
                        <button type="button" class="page-btn active">1</button>
                        <button type="button" class="page-btn">›</button>
                        <button type="button" class="page-btn">»</button>
                    </div>
                    <div class="total-count">
                        Total <c:out value="${empty products ? 0 : products.size()}" default="0" />
                    </div>
                </div>
            </div>
        </div>
        <%@ include file="../common/footer.jsp" %>
    </div>
</div>

<%-- ============ 우측 슬라이드 등록 패널 (Drawer) ============ --%>
<div id="drawerOverlay" class="drawer-overlay" onclick="closeCreateDrawer();"></div>
<aside id="createDrawer" class="drawer" aria-hidden="true">
    <div class="drawer-header">
        <h3>상품 등록</h3>
        <button type="button" class="drawer-close" onclick="closeCreateDrawer();" aria-label="닫기">×</button>
    </div>
    <form id="createDrawerForm" action="${ctx}/create" method="post" onsubmit="return validateCreate();">
        <div class="drawer-body">
            <div class="form-row"><label for="d_productCode">상품 코드 *</label>
                <input type="text" id="d_productCode" name="productCode" required maxlength="50" placeholder="예: P0001" />
            </div>
            <div class="form-row"><label for="d_name">상품명 *</label>
                <input type="text" id="d_name" name="name" required />
            </div>
            <div class="form-row"><label for="d_productStock">현재 재고 *</label>
                <input type="number" id="d_productStock" name="productStock" min="0" required />
            </div>
            <div class="form-row"><label for="d_safetyStock">안전 재고 *</label>
                <input type="number" id="d_safetyStock" name="safetyStock" min="0" required />
            </div>
            <div class="form-row"><label for="d_leadTime">리드타임(일)</label>
                <input type="number" id="d_leadTime" name="leadTime" min="0" />
            </div>
            <div class="form-row"><label for="d_image">이미지 경로 *</label>
                <input type="text" id="d_image" name="image" required placeholder="/data/MON1.jpg" />
            </div>
        </div>
        <div class="drawer-footer">
            <button type="button" class="btn btn-default" onclick="closeCreateDrawer();">취소</button>
            <button type="submit" class="btn btn-primary">저장</button>
        </div>
    </form>
</aside>

<%-- ============ 일괄 업로드용 숨김 form ============ --%>
<form id="bulkUploadForm" action="${ctx}/bulkUpload" method="post" enctype="multipart/form-data" style="display:none;">
    <input type="file" id="bulkUploadFile" name="file" accept=".csv" onchange="submitBulkUpload();" />
</form>

<script>
/* ----- Drawer 열고 닫기 ----- */
function openCreateDrawer() {
    document.getElementById('drawerOverlay').classList.add('is-open');
    document.getElementById('createDrawer').classList.add('is-open');
}
function closeCreateDrawer() {
    document.getElementById('drawerOverlay').classList.remove('is-open');
    document.getElementById('createDrawer').classList.remove('is-open');
}
document.addEventListener('keydown', function(e){ if(e.key==='Escape') closeCreateDrawer(); });

/* ----- 등록 폼 검증 ----- */
function validateCreate() {
    var ps = parseInt(document.getElementById('d_productStock').value, 10);
    var ss = parseInt(document.getElementById('d_safetyStock').value, 10);
    if(isNaN(ps) || isNaN(ss)) { alert('재고와 안전재고는 숫자여야 합니다.'); return false; }
    return true;
}

/* ----- 양식 다운로드 ----- */
function downloadTemplate() {
    window.location.href = '${ctx}/downloadTemplate';
}

/* ----- 일괄 등록 (파일 선택 → 자동 submit) ----- */
function openUploadDialog() {
    document.getElementById('bulkUploadFile').click();
}
function submitBulkUpload() {
    if(!confirm('CSV 파일을 일괄 등록하시겠습니까?')) return;
    document.getElementById('bulkUploadForm').submit();
}

/* ----- 선택 내역 다운로드 ----- */
function downloadSelected() {
    var checked = $('input[name="rowCheck"]:checked');
    if(checked.length === 0) { alert('다운로드할 행을 체크해주세요.'); return; }
    var codes = checked.map(function(){ return this.value; }).get();
    window.location.href = '${ctx}/downloadSelected?productCodes=' + encodeURIComponent(codes.join(','));
}

/* ----- 검색 (AJAX) ----- */
function searchProduct() {
    var keyword = $('#searchKeyword').val();
    var type    = $('#searchType').val();
    if(!keyword) { alert('검색어를 입력하세요'); return; }

    $.ajax({
        url: '${ctx}/search', type: 'GET',
        data: { searchKeyword: keyword, searchType: type },
        success: function(list){ renderRows(list); },
        error: function(xhr){
            if(xhr.status === 404) renderRows([]);
            else alert('검색 중 오류가 발생했습니다.');
        }
    });
}
function renderRows(list) {
    var tbody = $('#productTable tbody');
    tbody.empty();
    if(!list || list.length === 0){
        tbody.append('<tr><td colspan="9" class="table-empty">검색 결과가 없습니다.</td></tr>');
        return;
    }
    list.forEach(function(p){
        var row = '<tr>'
                + '<td class="center"><input type="checkbox" name="rowCheck" value="'+(p.productCode||'')+'" /></td>'
                + '<td>'+(p.productCode||'')+'</td>'
                + '<td>'+(p.name||'')+'</td>'
                + '<td class="center">'+(p.productStock!=null?p.productStock:'-')+'</td>'
                + '<td class="center">'+(p.safetyStock!=null?p.safetyStock:'-')+'</td>'
                + '<td class="center">'+(p.leadTime!=null?p.leadTime:'-')+'</td>'
                + '<td>'+(p.image||'')+'</td>'
                + '<td>'+(p.create_at?p.create_at:'-')+'</td>'
                + '<td class="center">-</td>'
                + '</tr>';
        tbody.append(row);
    });
}

/* ----- 전체 선택 ----- */
$(function(){
    $('#checkAll').on('change', function(){
        $('input[name="rowCheck"]').prop('checked', this.checked);
    });
});
</script>
</body>
</html>
