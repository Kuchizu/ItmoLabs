package servlets;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/controller")
public class ControllerServlet extends HttpServlet {
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        String x = request.getParameter("x");
        String y = request.getParameter("y");
        String r = request.getParameter("r");
        String clear = request.getParameter("clear");

        System.out.println("Got request: " + request);
        System.out.println(x + "  " + y + "  " + r);

        if ("1".equals(clear)) {
            request.getSession().invalidate();
            response.sendRedirect("http://localhost:8080/Web2-1.0-SNAPSHOT/");
            return;
        }

        if (x != null && y != null && r != null) {
            System.out.printf("Forwarding to AreaChecker with args %s %s %s ", x, y, r);
            System.out.println(this.getServletContext().getContextPath() + "/check?x=" + request.getParameter("x")
                    + "&y=" + request.getParameter("y") + "&r=" + request.getParameter("r"));
            response.sendRedirect(this.getServletContext().getContextPath() + "/check?x=" + request.getParameter("x")
                    + "&y=" + request.getParameter("y") + "&r=" + request.getParameter("r"));
        }
    }
}
