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
        String m = request.getParameter("m");
        String clear = request.getParameter("clear");

        System.out.println("Got request: " + request);
        System.out.println(x + "  " + y + "  " + r + " " + m);

        if ("1".equals(clear)) {
            request.getSession().invalidate();
            response.sendRedirect(this.getServletContext().getContextPath());
            return;
        }

        if (m != null){
            response.sendRedirect(this.getServletContext().getContextPath() + "/check?x=" + x + "&y=" + y + "&r=" + r + "&m=" + m);
        } else {
            response.sendRedirect(this.getServletContext().getContextPath() + "/check?x=" + x + "&y=" + y + "&r=" + r);
        }
    }
}
