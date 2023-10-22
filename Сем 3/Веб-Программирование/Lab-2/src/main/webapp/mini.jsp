<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="data" scope="session" class="model.UserAreaDatas"/>

<c:if test="${data.lastResult != null}">
    <tr>
        <td>${data.lastResult.x}</td>
        <td>${data.lastResult.y}</td>
        <td>${data.lastResult.r}</td>
        <td>${data.lastResult.result ? "Попадание" : "Промах"}</td>
        <td>${data.lastResult.calculationTime}</td>
    </tr>
</c:if>
