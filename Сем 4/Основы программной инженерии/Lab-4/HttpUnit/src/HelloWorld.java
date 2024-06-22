import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/*
 * HelloWorld.java
 * 
 * Created on Jul 17, 2007, 11:47:20 AM
 * 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author gs145266
 */
public class HelloWorld extends HttpServlet {

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 4.0 " +
                                        "Transitional//EN\">\n" +
                "<HTML>\n" +
                "<HEAD><TITLE>Hello World</TITLE></HEAD>\n" +
                "<BODY>\n" +
                "<H1>Hello World</H1>\n");
        out.println("<script language=\"JavaScript\"type=\"text/javascript\">");
        out.println("<!--");
        out.println("document.wr('Hello Document')");
        out.println("//-->");
        out.println("</script>");
        out.println("</BODY></HTML>");
    }
}
