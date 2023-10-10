<%@page contentType="text/html" pageEncoding="UTF-8" language="java" %>
<%@ taglib prefix="core" uri="http://java.sun.com/jsp/jstl/core" %>
<jsp:useBean id="data" scope="session" class="model.UserAreaDatas"/>
<!DOCTYPE html>
<html>
<head>
    <title>WEB LAB 1</title>
    <link rel="icon" href="https://lichess1.org/assets/_VCyzdj/logo/lichess-favicon-512.png">
    <link rel="stylesheet" href="Styles/style2.css">
    <meta charset="UTF-8">
    <script src="JQ/jquery-3.7.0.js"></script>
    <script type="text/javascript" src="Scripts/script.js"></script>
</head>
<body>
<header>
    <img id="itmo_logo" src="https://itmo.ru/promo/itmo-logo-dark.svg">
    <h1 id="lab1">Lab-2 Nodiri Khisravkhon P3231 var: 17535354 ver: 3</h1>
</header>
<hr>

<table class="win">
    <tr>
        <td>
            <div class="center-container">
                <div id="calculator" style="width: 500px; height: 500px;"></div>
            </div>
            <script src="https://www.desmos.com/api/v1.8/calculator.js?apiKey=dcb31709b452b1cf9dc26972add0fda6"></script>
            <script src="Scripts/graph.js"></script>
            <script type="text/javascript">
                enable_graph();
            </script>
        </td>
        <td id="menu">
            <form id="forma" action="${pageContext.request.contextPath}/controller" method="get">
                <div class="values">
                    <!-- X Values -->
                    <div class="x_value">
                        Choose X:
                        <input type="radio" class="x" id="x_-2" name="x" value="-2">
                        <label for="x_-2">-2</label>
                        <input type="radio" class="x" id="x_-1.5" name="x" value="-1.5">
                        <label for="x_-1.5">-1.5</label>
                        <input type="radio" class="x" id="x_-1" name="x" value="-1">
                        <label for="x_-1">-1</label>
                        <input type="radio" class="x" id="x_-0.5" name="x" value="-0.5">
                        <label for="x_-0.5">-0.5</label>
                        <input type="radio" class="x" id="x_0" name="x" value="0">
                        <label for="x_0">0</label>
                        <input type="radio" class="x" id="x_0.5" name="x" value="0.5">
                        <label for="x_0.5">0.5</label>
                        <input type="radio" class="x" id="x_1" name="x" value="1">
                        <label for="x_1">1</label>
                        <input type="radio" class="x" id="x_1.5" name="x" value="1.5">
                        <label for="x_1.5">1.5</label>
                        <input type="radio" class="x" id="x_2" name="x" value="2">
                        <label for="x_2">2</label>
                    </div>

                    <!-- Y Value -->
                    <div class="y_value">
                        Choose Y:
                        <input type="text" name="y" id="y" oninput="check_value()" placeholder="-3 ... 3">
                    </div>

                    <!-- R Value -->
                    <div id="r_value">
                        Choose R:
                        <input type="hidden" id="r_val" name="r">
                        <input type="button" class="r" value="1" onclick="saveR(this), check_value()">
                        <input type="button" class="r" value="1.5" onclick="saveR(this), check_value()">
                        <input type="button" class="r" value="2" onclick="saveR(this), check_value()">
                        <input type="button" class="r" value="2.5" onclick="saveR(this), check_value()">
                        <input type="button" class="r" value="3" onclick="saveR(this), check_value()">
                    </div>
                </div>

                <input type="submit" class="under_form_input" id="sub_but" value="Submit" disabled>
                <input type="button" class="under_form_input" id="clear_table" value="Clear table">

            </form>
        </td>
    </tr>
    <tr>
        <td id="tab" colspan="2">
            <div class="scroll-table">
                <div class="tab_head fixed-width-table">
                    <table class="tab1 fixed-width-table">
                        <thead>
                        <tr>
                            <th>X</th>
                            <th>Y</th>
                            <th>R</th>
                            <th>Hit fact</th>
                            <th>Current time</th>
                            <th>Script running time, ms</th>
                        </tr>
                        </thead>
                    </table>
                </div>
                <div id="scroll-table-body">
                    <table class="result">
                        <tbody class="result-body">
                            <core:forEach var="result" items="${data.data}">
                                <tr>
                                    <td>${result.x}</td>
                                    <td>${result.y}</td>
                                    <td>${result.r}</td>
                                    <td>${result.result ? "Попадание" : "Промах"}</td>
                                    <td>${result.calculationTime}</td>
                                </tr>
                            </core:forEach>
                        </tbody>
                    </table>
                    <a id="end_table"></a>
                </div>
            </div>
        </td>
    </tr>
</table>
</body>
</html>
