<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="../common/head.jsp" %>
    <title>재고 상세 | Main Fulfillment</title>
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
                <h2>재고 상세 정보</h2>
                <div class="breadcrumb">홈 / 재고 관리 / 상세</div>
            </div>

            <div class="card">
                <div class="card-title">상품 정보</div>
                <table class="data-table">
                    <tbody>
                        <tr>
                            <th style="width:160px;">재고 ID</th>
                            <td>${oneStock.id}</td>
                        </tr>
                        <tr>
                            <th>상품코드</th>
                            <td>${oneStock.productCode}</td>
                        </tr>
                        <tr>
                            <th>상품명</th>
                            <td>${oneStock.name}</td>
                        </tr>
                        <tr>
                            <th>현재 재고</th>
                            <td>${oneStock.productStock}</td>
                        </tr>
                        <tr>
                            <th>안전 재고</th>
                            <td>${oneStock.safetyStock}</td>
                        </tr>
                        <tr>
                            <th>리드타임 (일)</th>
                            <td>${oneStock.leadTime}</td>
                        </tr>
                    </tbody>
                </table>

                <div class="form-actions">
                    <a href="${ctx}/lookUpStock" class="btn btn-default">목록</a>
                    <button type="button" class="btn btn-primary"
                            onclick="modifyStock('${oneStock.productCode}', 'in');">입고 처리</button>
                    <button type="button" class="btn btn-warning"
                            onclick="modifyStock('${oneStock.productCode}', 'out');">출고 처리</button>
                </div>
            </div>
        </div>
        <%@ include file="../common/footer.jsp" %>
    </div>
</div>

<script>
function modifyStock(productCode, mode) {
    var qty = prompt('수량을 입력하세요', '0');
    var n = parseInt(qty, 10);
    if(isNaN(n) || n <= 0) { alert('올바른 수량을 입력하세요.'); return; }

    var url;
    var data;
    if(mode === 'in') {
        url  = '${ctx}/modifyInStock';
        data = '?productCode=' + encodeURIComponent(productCode)
             + '&inStatus=true&inStock=' + n;
    } else {
        url  = '${ctx}/modifyOutStock';
        data = '?productCode=' + encodeURIComponent(productCode)
             + '&outStatus=true&outStock=' + n;
    }

    $.ajax({
        url: url + data,
        type: 'PUT',
        success: function() { alert('재고가 변경되었습니다.'); location.reload(); },
        error:   function() { alert('재고 변경 중 오류가 발생했습니다.'); }
    });
}
</script>
</body>
</html>
