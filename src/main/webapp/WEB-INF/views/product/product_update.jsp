<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="../common/head.jsp" %>
    <title>상품 수정 | Main Fulfillment</title>
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
                <h2>상품 수정</h2>
                <div class="breadcrumb">홈 / 상품 관리 / 상품 수정</div>
            </div>

            <div class="card">
                <form action="${ctx}/update" method="post">
                    <div class="form-row">
                        <label>상품 코드</label>
                        <input type="text" name="productCode" value="${product.productCode}" readonly />
                    </div>
                    <div class="form-row">
                        <label for="name">상품명 *</label>
                        <input type="text" id="name" name="name" value="${product.name}" required />
                    </div>
                    <div class="form-row">
                        <label for="productStock">현재 재고 *</label>
                        <input type="number" id="productStock" name="productStock" value="${product.productStock}" min="0" required />
                    </div>
                    <div class="form-row">
                        <label for="safetyStock">안전 재고 *</label>
                        <input type="number" id="safetyStock" name="safetyStock" value="${product.safetyStock}" min="0" required />
                    </div>
                    <div class="form-row">
                        <label for="leadTime">리드타임(일)</label>
                        <input type="number" id="leadTime" name="leadTime" value="${product.leadTime}" min="0" />
                    </div>
                    <div class="form-row">
                        <label for="image">이미지 경로 *</label>
                        <input type="text" id="image" name="image" value="${product.image}" required />
                    </div>
                    <div class="form-actions">
                        <a href="${ctx}/selectAll" class="btn btn-default">취소</a>
                        <button type="submit" class="btn btn-primary">수정 저장</button>
                    </div>
                </form>
            </div>
        </div>
        <%@ include file="../common/footer.jsp" %>
    </div>
</div>
</body>
</html>
