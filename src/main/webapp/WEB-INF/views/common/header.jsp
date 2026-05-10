<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- ============================================================
    상단 헤더 영역
============================================================ --%>
<header class="app-header">
    <div class="brand">
        <span class="logo-mark">3PL</span>
        <span>Main Fulfillment 관리 시스템</span>
    </div>
    <div class="user-area">
        <span>관리자 :</span>
        <span class="user-name">
            <c:choose>
                <c:when test="${not empty sessionScope.adminId}">${sessionScope.adminId}</c:when>
                <c:otherwise>admin</c:otherwise>
            </c:choose>
        </span>
        <a class="logout-link" href="${pageContext.request.contextPath}/api/v1/main-fulfillment/