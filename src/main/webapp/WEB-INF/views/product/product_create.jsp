<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="../common/head.jsp" %>
    <title>상품 등록 | Main Fulfillment</title>
</head>
<body>
<c:set var="menu" value="product" />
<c:set var="ctx" value="${pageContext.request.contextPath}/api/v1/main-fulfillment/product" />

<div class="app-shell">
    <%@ include file="../common/sidebar.jsp" %>
    <div class="app-main">
        <%@ include file="../common/header.jsp" %>
        <div class="content-wrapper">
            <div class="page-header">
                <h2>상품 등록</h2>
                <div class="breadcrumb">홈 / 상품 관리 / 상품 등록</div>
            </div>

            <div class="card">
                <form action="${ctx}/create" method="post" onsubmit="return validate();">
                    <div class="form-row">
                        <label for="productCode">상품 코드 *</label>
                        <input type="text" id="productCode" name="productCode" required maxlength="50" placeholder="예: P0001" />
                    </div>
                    <div class="form-row">
                        <label for="name">상품명 *</label>
                        <input type="text" id="name" name="name" required />
                    </div>
                    <div class="form-row">
                        <label for="productStock">현재 재고 *</label>
                        <input type="number" id="productStock" name="productStock" min="0" required />
                    </div>
                    <div class="form-row">
                        <label for="safetyStock">안전 재고 *</label>
                        <input type="number" id="safetyStock" name="safetyStock" min="0" required />
                    </div>
                    <div class="form-row">
                        <label for="leadTime">리드타임(일)</label>
                        <input type="number" id="leadTime" name="leadTime" min="0" />
                    </div>
                    <div class="form-row">
                        <label for="image">이미지 경로 *</label>
                        <input type="text" id="image" name="image" required placeholder="/data/MON1.jpg" />
                    </div>
                    <div class="form-actions">
                        <a href="${ctx}/selectAll" class="btn btn-default">취소</a>
                        <button type="submit" class="btn btn-primary">저장</button>
                    </div>
                </form>
            </div>
        </div>
        <%@ include file="../common/footer.jsp" %>
    </div>
</div>

<script>
function validate() {
    var ps = parseInt(document.getElementById('productStock').value, 10);
    var ss = parseInt(document.getElementById('safetyStock').value, 10);
    if(isNaN(ps) || isNaN(ss)) {
        alert('재고와 안전재고는 숫자여야 합니다.');
        return false;
    }
    return true;
}
</script>
</body>
</html>
