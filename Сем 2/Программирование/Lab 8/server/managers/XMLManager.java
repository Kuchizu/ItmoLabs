package managers;

import collections.Coordinates;
import collections.Flat;
import collections.Furnish;
import collections.House;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;

/**
 * XMLManager for managing XML DB
 */
public class XMLManager {

    public static ArrayDeque<Flat> getData() {
        return data;
    }

    public void setData(ArrayDeque<Flat> data) {
        XMLManager.data = data;
    }

    public static ArrayDeque<Flat> data = new ArrayDeque<>();

    /**
     * Loads database to data object.
     *
     * @param path XML DB file path
     * @throws ParserConfigurationException Throws when DB can't be parsed.
     */
    public static void loadData(String path) throws ParserConfigurationException {

        File checknull = new File(path);
        if (checknull.length() == 0) {
            return;
        }

        Document doc = null;

        try {
            DocumentBuilderFactory dbFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilt = dbFact.newDocumentBuilder();
            doc = dBuilt.parse(new File(path));

        } catch (SAXException sax) {
            System.err.println("Неверная конфигурация xml файла, проверьте исходный файл.");
            System.exit(-1);
        } catch (IOException io) {
            System.err.println("Ошибка доступа к базе, проверьте и повторите заного.");
            System.exit(-1);
        }

        ArrayDeque<Flat> data = new ArrayDeque<>();

        NodeList nodeList = doc.getElementsByTagName("Flat");

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;

                String[] coordinates = eElement.getElementsByTagName("coordinates").item(0).getTextContent().trim().split("\\s+");
                String[] house = eElement.getElementsByTagName("house").item(0).getTextContent().trim().split("\\s+");
                int crdate = Integer.parseInt(eElement.getElementsByTagName("creationDate").item(0).getTextContent());

                Flat flat = new Flat(
                        Integer.parseInt(eElement.getElementsByTagName("id").item(0).getTextContent()),
                        -1,
                        eElement.getElementsByTagName("name").item(0).getTextContent(),
                        new Coordinates(Long.parseLong(coordinates[0]), Double.parseDouble(coordinates[1])),
                        ZonedDateTime.ofInstant(Instant.ofEpochSecond(crdate), ZoneId.of("Europe/Moscow")),
                        Integer.parseInt(eElement.getElementsByTagName("area").item(0).getTextContent()),
                        Integer.parseInt(eElement.getElementsByTagName("numberOfRooms").item(0).getTextContent()),
                        Float.valueOf(eElement.getElementsByTagName("timeToMetroOnFoot").item(0).getTextContent()),
                        Double.valueOf(eElement.getElementsByTagName("timeToMetroByTransport").item(0).getTextContent()),
                        Furnish.valueOf(eElement.getElementsByTagName("furnish").item(0).getTextContent()),

                        new House(
                                house[0],
                                Long.parseLong(house[1]),
                                Long.parseLong(house[2]),
                                Integer.parseInt(house[3])
                        )
                );

                data.add(flat);

            }
        }

        XMLManager.data = data;
    }

    /**
     * Adds new Flat element to the collection.
     *
     * @param flat Flat object
     */
    public static void addElement(Flat flat) {
        XMLManager.data.add(flat);
    }

    public static void changeElement(int flatid, Flat flat) {
        for (Flat f : XMLManager.data) {
            if (f.getId() == flatid) {
                flat.setId(f.getId());
                flat.setCreationDate(f.getCreationDate());
                XMLManager.data.remove(f);
                break;
            }
        }
        XMLManager.data.add(flat);
    }

    /**
     * Clears data object.
     */
    public static void dropAll() {
        XMLManager.data.clear();
    }

    /**
     * Writes new changes to DB file.
     *
     * @param path XML DB file path
     * @return
     * @throws TransformerException
     * @throws ParserConfigurationException
     * @throws SAXException
     * @throws IOException
     */
    public static boolean writeToFile(String path) throws ParserConfigurationException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element rootElement = doc.createElementNS(null, "data");
        doc.appendChild(rootElement);

        ArrayDeque<Flat> flats = XMLManager.getData();

        if (flats.isEmpty()) {
            try {
                new FileWriter(path, false).close();
            } catch (java.io.IOException io) {
                System.err.println("Изменения не сохранены. Ошибка доступа к файлу, проверьте файл и попробуйте сохранить ещё раз.");
                return false;
            }
        }

        for (Flat flat : flats) {

            Element nodee = doc.createElement("Flat");
            Node q = rootElement.appendChild(nodee);

            q.appendChild(parseNode(doc, "id", String.valueOf(flat.getId())));
            q.appendChild(parseNode(doc, "name", flat.getName()));

            Element corelem = doc.createElement("coordinates");
            Node coordinates = q.appendChild(corelem);
            coordinates.appendChild(parseNode(doc, "x", String.valueOf(flat.getCoordinates().getX())));
            coordinates.appendChild(parseNode(doc, "y", String.valueOf(flat.getCoordinates().getY())));


            q.appendChild(parseNode(doc, "creationDate", String.valueOf(flat.getCreationDate().toEpochSecond())));
            q.appendChild(parseNode(doc, "area", String.valueOf(flat.getArea())));
            q.appendChild(parseNode(doc, "numberOfRooms", String.valueOf(flat.getNumberOfRooms())));
            q.appendChild(parseNode(doc, "timeToMetroOnFoot", String.valueOf(flat.getTimeToMetroOnFoot())));
            q.appendChild(parseNode(doc, "timeToMetroByTransport", String.valueOf(flat.getTimeToMetroByTransport())));
            q.appendChild(parseNode(doc, "furnish", String.valueOf(flat.getFurnish())));

            Element houseelem = doc.createElement("house");
            Node house = q.appendChild(houseelem);
            house.appendChild(parseNode(doc, "name", flat.getHouse().getName()));
            house.appendChild(parseNode(doc, "year", String.valueOf(flat.getHouse().getYear())));
            house.appendChild(parseNode(doc, "numberOfFloors", String.valueOf(flat.getHouse().getNumberOfFloors())));
            house.appendChild(parseNode(doc, "numberOfLifts", String.valueOf(flat.getHouse().getNumberOfLifts())));


            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer;
            try {
                transformer = transformerFactory.newTransformer();
            } catch (TransformerConfigurationException e) {
                throw new RuntimeException(e);
            }

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);

            try {
                StreamResult file = new StreamResult(new PrintWriter(path));
                transformer.transform(source, file);
            } catch (FileNotFoundException | TransformerException jxtt) {
                System.err.println("Изменения не сохранены. Ошибка доступа к файлу, проверьте файл и попробуйте сохранить ещё раз.");
                return false;
            }

        }
        return true;
    }

    /**
     * Parses node.
     *
     * @param doc
     * @param name
     * @param value
     * @return
     */
    private static Node parseNode(Document doc, String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }
}
