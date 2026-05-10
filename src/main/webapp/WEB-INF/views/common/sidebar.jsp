<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%-- ============================================================
    좌측 사이드바 (네비게이션)
    - 사용 시 페이지에서 <c:set var="menu" value="dashboard" /> 처럼
      현재 페이지 키를 지정하면 active 표시가 적용됩니다.
============================================================ --%>
<c:set var="ctx" value="${pageContext.request.contextPath}/api/v1/main-fulfillment" />

<aside class="app-sidebar">
    <div class="sidebar-title">메뉴</div>
    <ul class="nav">
        <li class="nav-section">대시보드</li>
        <li class="nav-item ${menu eq 'dashboard' ? 'active' : ''}">
            <a href="${ctx}/showHome">홈 (대시보드)</a>
        </li>

        <li class="nav-section">상품 / 재고</li>
        <li class="nav-item ${menu eq 'product' ? 'active' : ''}">
            <a href="${ctx}/product/selectAll">상품 관리</a>
        </li>
        <li class="nav-item ${menu eq 'stock' ? 'active' : ''}">
            <a href="${ctx}/lookUpStock">재고 관리</a>
        </li>

        <li class="nav-section">입출고</li>
        <li class="nav-item ${menu eq 'in' ? 'active' : ''}">
            <a href="${ctx}/in/selectAll">입고 관리</a>
        </li>
        <li class="nav-item ${menu eq 'out' ? 'active' : ''}">
            <a href="${ctx}/out/selectAll">출고 관리</a>
        </li>

        <li class="nav-section">배송</li>
        <li class="nav-item ${menu eq 'delivery' ? 'active' : ''}">
            <a href="${ctx}/selectDeliverylist">배송 목록</a>
        </li>

        <li class="nav-section">계정</li>
        <li class="nav-item ${menu eq 'account' ? 'active' : ''}">
            <a href="${ctx}/showAccount">관리자 계정</a>
        </li>
    </ul>
    <div class="sidebar-footer">
        © 2025 OT 3PL Fulfillment
    </div>
</aside>
