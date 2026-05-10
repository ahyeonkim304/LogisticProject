<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="../common/head.jsp" %>
    <title>관리자 로그인 | Main Fulfillment</title>
</head>
<body>
<div class="login-shell">
    <div class="login-card">
        <h1>3PL Main Fulfillment</h1>
        <div class="login-sub">관리자 로그인</div>

        <c:if test="${not empty loginError}">
            <div class="alert alert-error">${loginError}</div>
        </c:if>

        <form class="login-form" action="${pageContext.request.contextPath}/api/v1/main-fulfillment/login" method="post">
            <div class="form-row">
                <label for="id">아이디</label>
                <input type="text" id="id" name="id" placeholder="관리자 아이디" required autofocus />
            </div>
            <div class="form-row">
                <label for="pw">비밀번호</label>
                <input type="password" id="pw" name="pw" placeholder="비밀번호" />
            </div>
            <button type="submit" class="btn btn-primary">로그인</button>
        </form>
    </div>
</div>
</body>
</html>
