/*
 * Main.java
 *
 * Created on Jul 1, 2007, 7:24:43 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */



import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpUnitOptions;
import com.meterware.httpunit.WebRequest;
import com.meterware.httpunit.WebResponse;
import com.meterware.servletunit.ServletRunner;
import com.meterware.servletunit.ServletUnitClient;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.SAXException;

/**
 *
 * @author gs145266
 */
public class Main {

    /** Creates a new instance of Main */
    public Main() {
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            HttpUnitOptions.setExceptionsThrownOnScriptError(false);
            ServletRunner sr = new ServletRunner();
            sr.registerServlet("myServlet", HelloWorld.class.getName());
            ServletUnitClient sc = sr.newClient();
            int number = 1;
            WebRequest request = new GetMethodWebRequest("http://test.meterware.com/myServlet");
            while (true) {
                WebResponse response = sc.getResponse(request);
                System.out.println("Count: " + number++ + response);
                java.lang.Thread.sleep(200);
                HttpUnitOptions.clearScriptErrorMessages();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        } catch (MalformedURLException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger("global").log(Level.SEVERE, null, ex);
        }
    }
}