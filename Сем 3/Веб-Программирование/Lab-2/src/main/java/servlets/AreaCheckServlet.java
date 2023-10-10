package servlets;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import model.AreaData;
import model.UserAreaDatas;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@WebServlet("/check")
public class AreaCheckServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        final long startExec = System.nanoTime();
        final String x = request.getParameter("x");
        final String y = request.getParameter("y");
        final String r = request.getParameter("r");

        final double dx;
        final double dy;
        final double dr;

        System.out.printf("Got in AreaChecker with args %s %s %s ", x, y, r);

        try {
            dx = Double.parseDouble(x);
            dy = Double.parseDouble(y);
            dr = Double.parseDouble(r);

        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(400);
            return;
        }

        final boolean result = checkArea(dx, dy, dr);
        final long endExec = System.nanoTime();
        final long executionTime = endExec - startExec;
        final LocalDateTime executedAt = LocalDateTime.now();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        String formattedTime = executedAt.format(formatter);

        final AreaData data = new AreaData();
        data.setX(dx);
        data.setY(dy);
        data.setR(dr);
        data.setResult(result);
        data.setCalculationTime(executionTime);
        data.setCalculatedAt(executedAt);

        HttpSession session = request.getSession();
        UserAreaDatas sessionData = (UserAreaDatas) session.getAttribute("data");
        if (sessionData == null) {
            sessionData = new UserAreaDatas();
        }
        sessionData.addResult(data);

        session.setAttribute("data", sessionData);
        response.sendRedirect("table.jsp");
    }
    private boolean checkArea(final double x, final double y, final double r) {
        return true;
    }
}