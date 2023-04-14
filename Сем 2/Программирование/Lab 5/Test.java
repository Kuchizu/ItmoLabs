import collections.Flat;
import collections.Sequence;
import managers.XMLManager;
import org.w3c.dom.Element;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class Test {
    public static void main(String[] args) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        System.out.println(ZonedDateTime.now());
//        System.out.println(ZonedDateTime.now().toInstant());
//        System.out.println(ZonedDateTime.now().toString());
//        System.out.println(ZonedDateTime.now().toEpochSecond());
//        System.out.println((int) ZonedDateTime.now().toEpochSecond() - 1_670_000_000);
//
//        System.out.println(Math.abs((int) System.nanoTime()));
//        System.out.println(Math.abs((int) System.nanoTime()));
//        System.out.println(Math.abs((int) System.nanoTime()));
//        System.out.println(Math.abs((int) System.nanoTime()));
//        System.out.println(Math.abs((int) System.nanoTime()));
//        System.out.println(Math.abs((int) System.nanoTime()));
//        System.out.println(Math.abs((int) System.nanoTime()));

//        Scanner obj = new Scanner(System.in);
//        String a = obj.nextLine();
//        System.out.println(a);

        XMLManager.writeToFile("");



//        System.out.println((int) (System.currentTimeMillis() & 0xfffffff));
//        System.out.println((int) (System.currentTimeMillis() & 0xfffffff));
//        System.out.println((int) (System.currentTimeMillis() & 0xfffffff));
//        System.out.println((int) (System.currentTimeMillis() & 0xfffffff));

    }
}
