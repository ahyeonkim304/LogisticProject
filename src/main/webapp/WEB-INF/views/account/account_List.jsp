<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<!DOCTYPE html>
<html lang="ko">
<head>
    <%@ include file="../common/head.jsp" %>
    <title>관리자 계정 관리 | Main Fulfillment</title>
</head>
<body>
<c:set var="menu" value="account" />
<div class="app-shell">
    <%@ include file="../common/sidebar.jsp" %>
    <div class="app-main">
        <%@ include file="../common/header.jsp" %>
        <div class="content-wrapper">
            <div class="page-header">
                <h2>관리자 계정 관리</h2>
                <div class="breadcrumb">홈 / 관리자 계정</div>
            </div>

            <div class="card">
                <div class="card-title">관리자 계정 목록</div>
                <table class="data-table">
                    <thead>
                        <tr>
                            <th>관리자 ID</th>
                            <th>비밀번호</th>
                            <th class="center">관리</th>
                        </tr>
                    </thead>
                    <tbody>
                        <c:choose>
                            <c:when test="${empty accountList}">
                                <tr>
                                    <td colspan="3" class="table-empty">계정 데이터를 조회하려면 백엔드 API가 필요합니다.</td>
                                </tr>
                            </c:when>
                            <c:otherwise>
                                <c:forEach items="${accountList}" var="a">
                                    <tr>
                                        <td>${a.id}</td>
                                        <td>****</td>
                                        <td class="center">
                                            <button class="btn btn-sm">수정</button>
                                            <button class="btn btn-sm btn-danger">삭제</button>
                                        </td>
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
