<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="data" scope="session" class="model.UserAreaDatas"/>

<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" type="text/css" href="Styles/table.css">
    <title>Result</title>
</head>
<body>
<table>
    <thead>
    <tr>
        <th>X</th>
        <th>Y</th>
        <th>R</th>
        <th>Result</th>
        <th>Calculation Time</th>
    </tr>
    </thead>
    <tbody>
    <c:if test="${data.lastResult != null}">
        <tr>
            <td>${data.lastResult.x}</td>
            <td>${data.lastResult.y}</td>
            <td>${data.lastResult.r}</td>
            <td>${data.lastResult.result ? "Попадание" : "Промах"}</td>
            <td>${data.lastResult.calculationTime}</td>
        </tr>
    </c:if>
    </tbody>
</table>

<br>
<div style="text-align: center;">
    <a href="http://localhost:8080/Web2-1.0-SNAPSHOT/">Back Menu</a>
</div>
</body>
</html>