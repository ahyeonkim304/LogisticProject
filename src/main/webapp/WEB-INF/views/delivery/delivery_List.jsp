<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="../common/head.jsp" %>
    <title>배송 목록 | Main Fulfillment</title>
</head>
<body>
<c:set var="menu" value="delivery" />
<c:set var="ctx" value="${pageContext.request.contextPath}/api/v1/main-fulfillment" />

<div class="app-shell">
    <%@ include file="../common/sidebar.jsp" %>
    <div class="app-main">
        <%@ include file="../common/header.jsp" %>
        <div class="content-wrapper">

            <div class="page-header">
                <div class="list-title-block">
                    <h2>배송 리스트</h2>
                    <div class="subtitle">배송 정보를 조회하고 관리하는 화면입니다.</div>
                </div>
                <div class="flex gap-8">
                    <button type="button" class="btn btn-default" onclick="downloadTemplate();">양식 다운로드</button>
                    <button type="button" class="btn btn-default" onclick="downloadSelected();">선택 내역 다운로드</button>
                </div>
            </div>

            <div class="card">
                <table class="data-table" id="deliveryTable">
                    <thead>
                        <tr>
                            <th class="center" style="width:36px;"><input type="checkbox" id="checkAll" /></th>
                            <th class="center">No</th>
                            <th>송장번호</th>
                            <th>주문자</th>
                            <th>연락처</th>
                            <th>주소</th>
                            <th>우편번호</th>
                            <th>상품코드</th>
                            <th>상품명</th>
                            <th class="center">수량</th>
                            <th class="center">상태</th>
                            <th class="center">관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty deliveryList}">
                                <tr><td colspan="12" class="table-empty">등록된 배송 데이터가 없습니다.</td></tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${deliveryList}" var="d">
                                    <tr>
                                        <td class="center"><input type="checkbox" name="rowCheck" value="${d.id}" /></td>
                                        <td class="center">${d.id}</td>
                                        <td>${d.trackingNumber}</td>
                                        <td>${d.userName}</td>
                                        <td>${d.hp1}-${d.hp2}-${d.hp3}</td>
                                        <td>${d.address}</td>
                                        <td>${d.zipcode}</td>
                                        <td>${d.productCode}</td>
                                        <td>${d.productName}</td>
                                        <td class="center">${d.stockCount}</td>
                                        <td class="center">
                                            <c:choose>
                                                <c:when test="${d.statusDelivery eq '배송시작'}">
                                                    <span class="badge badge-success">${d.statusDelivery}</span>
                                                </c:when>
                                                <c:otherwise>
                                                    <span class="badge badge-warning">${d.statusDelivery}</span>
                                                </c:otherwise>
                                            </c:choose>
                                        </td>
                                        <td class="center">
                                            <button type="button" class="btn btn-sm btn-success"
                                                onclick="changeStatus(${d.id}, '${d.productCode}');">배송 시작</button>
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
                        Total <c:out value="${empty deliveryList ? 0 : deliveryList.size()}" default="0" />
                    </div>
                </div>
            </div>
        </div>
        <%@ include file="../common/footer.jsp" %>
    </div>
</div>

<script>
function downloadTemplate() { window.location.href = '${ctx}/downloadDeliveryTemplate'; }
function downloadSelected() {
    var checked = $('input[name="rowCheck"]:checked');
    if(checked.length === 0) { alert('다운로드할 행을 체크해주세요.'); return; }
    var ids = checked.map(function(){ return this.value; }).get();
    window.location.href = '${ctx}/downloadDeliverySelected?ids=' + encodeURIComponent(ids.join(','));
}
function changeStatus(id, productCode) {
    if(!confirm('해당 주문을 배송 시작 상태로 변경하시겠습니까?')) return;
    var url = '${ctx}/updateDelivery'
            + '?id=' + encodeURIComponent(id)
            + '&productCode=' + encodeURIComponent(productCode)
            + '&outStatus=true&outStock=0';
    $.ajax({
        url: url, type: 'PUT',
        success: function(){ alert('배송 상태가 변경되었습니다.'); location.reload(); },
        error:   function(){ alert('상태 변경 중 오류가 발생했습니다.'); }
    });
}
$(function(){
    $('#checkAll').on('change', function(){
        $('input[name="rowCheck"]').prop('checked', this.checked);
    });
});
</script>
</body>
</html>
