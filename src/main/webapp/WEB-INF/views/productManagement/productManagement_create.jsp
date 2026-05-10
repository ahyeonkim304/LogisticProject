<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="../common/head.jsp" %>
    <title>재고 생성 | Main Fulfillment</title>
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
                <h2>재고 생성</h2>
                <div class="breadcrumb">홈 / 재고 관리 / 재고 생성</div>
            </div>

            <div class="card">
                <p class="mb-20" style="color:#6c7a89;">
                    이미 등록된 상품 코드를 입력하면 해당 상품 정보로 재고 데이터를 생성합니다.
                </p>
                <form action="${ctx}/createStock" method="post">
                    <div class="form-row">
                        <label for="productCode">상품 코드 *</label>
                        <input type="text" id="productCode" name="productCode" required placeholder="예: P0001" />
                    </div>
                    <div class="form-actions">
                        <a href="${ctx}/lookUpStock" class="btn btn-default">취소</a>
                        <button type="submit" class="btn btn-primary">재고 생성</button>
                    </div>
                </form>
            </div>
        </div>
        <%@ include file="../common/footer.jsp" %>
    </div>
</div>
</body>
</html>
