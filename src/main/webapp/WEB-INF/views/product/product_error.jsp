<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="../common/head.jsp" %>
    <title>상품 처리 오류 | Main Fulfillment</title>
</head>
<body>
<c:set var="menu" value="product" />
<c:set var="ctx" value="${pageContext.request.contextPath}/api/v1/main-fulfillment/product" />

<div class="app-shell">
    <%@ include file="../common/sidebar.jsp" %>
    <div class="app-main">
        <%@ include file="../common/header.jsp" %>
        <div class="content-wrapper">
            <div class="card">
                <div class="alert alert-error">
                    상품 처리 중 오류가 발생했습니다. 입력값을 다시 확인해 주세요.
                </div>
                <a href="${ctx}/selectAll" class="btn btn-primary">상품 목록으로 돌아가기</a>
            </div>
        </div>
        <%@ include file="../common/footer.jsp" %>
    </div>
</div>
</body>
</html>
