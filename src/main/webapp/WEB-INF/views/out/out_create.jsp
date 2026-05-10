<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="../common/head.jsp" %>
    <title>출고 등록 | Main Fulfillment</title>
</head>
<body>
<c:set var="menu" value="out" />
<c:set var="ctx" value="${pageContext.request.contextPath}/api/v1/main-fulfillment/out" />

<div class="app-shell">
    <%@ include file="../common/sidebar.jsp" %>
    <div class="app-main">
        <%@ include file="../common/header.jsp" %>
        <div class="content-wrapper">
            <div class="page-header">
                <h2>출고 등록</h2>
                <div class="breadcrumb">홈 / 출고 관리 / 출고 등록</div>
            </div>

            <div class="card">
                <form action="${ctx}/create" method="post">
                    <div class="form-row">
                        <label for="productCode">상품 코드 *</label>
                        <input type="text" id="productCode" name="productCode" value="${productCode}" required />
                    </div>
                    <div class="form-row">
                        <label for="outStock">출고 수량 *</label>
                        <input type="number" id="outStock" name="outStock" min="1" required />
                    </div>
                    <div class="form-row">
                        <label for="outStatus">출고 상태</label>
                        <select id="outStatus" name="outStatus">
                            <option value="false" selected>진행중</option>
                            <option value="true">완료</option>
                        </select>
                    </div>
                    <div class="form-actions">
                        <a href="${ctx}/selectAll" class="btn btn-default">취소</a>
                        <button type="submit" class="btn btn-primary">등록</button>
                    </div>
                </form>
            </div>
        </div>
        <%@ include file="../common/footer.jsp" %>
    </div>
</div>
</body>
</html>
