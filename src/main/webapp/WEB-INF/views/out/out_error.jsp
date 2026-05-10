<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="../common/head.jsp" %>
    <title>출고 처리 오류 | Main Fulfillment</title>
</head>
<body>
<c:set var="menu" value="out" />
<c:set var="ctx" value="${pageContext.request.contextPath}/api/v1/main-fulfillment/out" />
<div class="app-shell">
    <%@ include file="../common/sidebar.jsp" %>
    <div class="app-main">
        <%@ include file="../common/header.jsp" %>
        <div class="content-wrapper">
            <div class="card">
                <div class="alert alert-error">출고 처리 중 오류가 발생했습니다.</div>
                <a href="${ctx}/selectAll" class="btn btn-primary">출고 목록</a>
            </div>
        </div>
        <%@ include file="../common/footer.jsp" %>
    </div>
</div>
</body>
</html>
