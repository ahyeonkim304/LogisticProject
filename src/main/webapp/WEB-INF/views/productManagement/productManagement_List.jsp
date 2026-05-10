<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="../common/head.jsp" %>
    <title>재고 관리 | Main Fulfillment</title>
</head>
<body>
<c:set var="menu" value="stock" />
<c:set var="ctx" value="${pageContext.request.contextPath}/api/v1/main-fulfillment" />

<div class="app-shell">
    <%@ include file="../common/sidebar.jsp" %>
    <div class="app-main">
        <%@ include file="../common/header.jsp" %>
        <div class="content-wrapper">

            <div class="page-header">
                <div class="list-title-block">
                    <h2>재고 리스트</h2>
                    <div class="subtitle">재고 정보를 조회하고 관리하는 화면입니다.</div>
                </div>
                <div class="flex gap-8">
                    <button type="button" class="btn btn-default" onclick="downloadTemplate();">양식 다운로드</button>
                    <button type="button" class="btn btn-default" onclick="openUploadDialog();">일괄 등록</button>
                    <button type="button" class="btn btn-default" onclick="downloadSelected();">선택 내역 다운로드</button>
                    <a href="${ctx}/goToCreateStock" class="btn btn-primary">+ 재고 생성</a>
                </div>
            </div>

            <div class="card">
                <table class="data-table" id="stockTable">
                    <thead>
                        <tr>
                            <th class="center" style="width:36px;"><input type="checkbox" id="checkAll" /></th>
                            <th class="center">No</th>
                            <th>상품코드</th>
                            <th>상품명</th>
                            <th class="center">현재재고</th>
                            <th class="center">안전재고</th>
                            <th class="center">리드타임</th>
                            <th class="center">상태</th>
                            <th class="center">관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty stockList}">
                                <tr><td colspan="9" class="table-empty">등록된 재고 데이터가 없습니다.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${stockList}" var="s">
                                    <tr>
                                        <td class="center"><input type="checkbox" name="rowCheck" value="${s.id}" /></td>
                                        <td class="center">${s.id}</td>
                                        <td>${s.productCode}</td>
                                        <td>${s.name}</td>
                                        <td class="center">${s.productStock}</td>
                                        <td class="center">${s.safetyStock}</td>
                                        <td class="center">${s.leadTime}</td>
                                        <td class="center">
                                            <c:choose>
                                                <c:when test="${s.productStock <= s.safetyStock}">
                                                    <span class="badge badge-danger">재고 부족</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-success">정상</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="center">
                                            <a href="${ctx}/selectStockDetail?id=${s.id}" class="btn btn-sm">상세</a>
                                            <form action="${ctx}/deleteStock" method="post" style="display:inline;"
                                                  onsubmit="return confirm('정말 삭제하시겠습니까?');">
                                                <input type="hidden" name="id" value="${s.id}" />
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
                        Total <c:out value="${empty stockList ? 0 : stockList.size()}" default="0" />
                    </div>
                </div>
            </div>
        </div>
        <%@ include file="../common/footer.jsp" %>
    </div>
</div>

<form id="bulkUploadForm" action="${ctx}/bulkUploadStocks" method="post" enctype="multipart/form-data" style="display:none;">
    <input type="file" id="bulkUploadFile" name="file" accept=".csv" onchange="submitBulkUpload();" />
</form>

<script>
function downloadTemplate() { window.location.href = '${ctx}/downloadStockTemplate'; }
function openUploadDialog() { document.getElementById('bulkUploadFile').click(); }
function submitBulkUpload() {
    if(!confirm('CSV 파일을 일괄 등록하시겠습니까?')) return;
    document.getElementById('bulkUploadForm').submit();
}
function downloadSelected() {
    var checked = $('input[name="rowCheck"]:checked');
    if(checked.length === 0) { alert('다운로드할 행을 체크해주세요.'); return; }
    var ids = checked.map(function(){ return this.value; }).get();
    window.location.href = '${ctx}/downloadStockSelected?ids=' + encodeURIComponent(ids.join(','));
}
$(function(){
    $('#checkAll').on('change', function(){
        $('input[name="rowCheck"]').prop('checked', this.checked);
    });
});
</script>
</body>
</html>
