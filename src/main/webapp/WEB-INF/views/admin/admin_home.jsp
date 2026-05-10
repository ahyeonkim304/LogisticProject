<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="../common/head.jsp" %>
    <title>대시보드 | Main Fulfillment</title>
</head>
<body>
<c:set var="menu" value="dashboard" />
<div class="app-shell">
    <%@ include file="../common/sidebar.jsp" %>
    <div class="app-main">
        <%@ include file="../common/header.jsp" %>
        <div class="content-wrapper">
            <div class="page-header">
                <h2>대시보드</h2>
                <div class="breadcrumb">홈 / 대시보드</div>
            </div>

            <%-- 요약 카드 --%>
            <c:set var="totalDelivery" value="${empty deliveryList ? 0 : 0}" />
            <c:set var="readyCnt" value="0" />
            <c:set var="startCnt" value="0" />
            <c:forEach items="${deliveryList}" var="d">
                <c:set var="totalDelivery" value="${totalDelivery + 1}" />
                <c:choose>
                    <c:when test="${d.statusDelivery eq '배송시작'}">
                        <c:set var="startCnt" value="${startCnt + 1}" />
                    </c:when>
                    <c:otherwise>
                        <c:set var="readyCnt" value="${readyCnt + 1}" />
                    </c:otherwise>
                </c:choose>
            </c:forEach>

            <div class="dashboard-grid">
                <div class="metric-card">
                    <div class="metric-label">전체 배송 건수</div>
                    <div class="metric-value">${totalDelivery}</div>
                </div>
                <div class="metric-card warn">
                    <div class="metric-label">배송 준비중</div>
                    <div class="metric-value">${readyCnt}</div>
                </div>
                <div class="metric-card success">
                    <div class="metric-label">배송 시작</div>
                    <div class="metric-value">${startCnt}</div>
                </div>
            </div>

            <div class="card">
                <div class="card-title">최근 배송 현황</div>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>송장번호</th>
                            <th>주문자</th>
                            <th>연락처</th>
                            <th>상품명</th>
                            <th class="center">수량</th>
                            <th class="center">상태</th>
                            <th>상품코드</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty deliveryList}">
                                <tr>
                                    <td colspan="7" class="table-empty">등록된 배송 데이터가 없습니다.</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${deliveryList}" var="d">
                                    <tr>
                                        <td>${d.trackingNumber}</td>
                                        <td>${d.userName}</td>
                                        <td>${d.hp1}-${d.hp2}-${d.hp3}</td>
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
                                        <td>${d.productCode}</td>
                                    </tr>
                                </c:forEach>
                            </c:otherwise>
                        </c:choose>
                    </tbody>
                </table>
            </div>
        </div>
        <%@ include file="../common/footer.jsp" %>
    </div>
</div>
</body>
</html>
