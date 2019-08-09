<%@ page language="java" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<h2>this is a demo page</h2>
<c:forEach items="${users}" var="user">
    <c:out value="${user.username}"/>
    <c:out value="${user.age}"/>
    </br>
</c:forEach>