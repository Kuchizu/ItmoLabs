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
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayDeque;
import java.util.Scanner;

public class XMLManager {

    public static ArrayDeque<Flat> getData() {
        return data;
    }

    public static void setData(ArrayDeque<Flat> data) {
        XMLManager.data = data;
    }

    private static ArrayDeque<Flat> data = new ArrayDeque<>();

    private static void removeBlank(String path) throws FileNotFoundException {

        Scanner file = new Scanner(new File(path));
        StringBuilder w = new StringBuilder();

        while (file.hasNext()) {
            String line = file.nextLine();
            if (!line.trim().isEmpty()) {
                w.append(line).append('\n');
            }
        }
        file.close();

        PrintWriter writer = new PrintWriter(path);
        writer.write(w.toString());
        writer.close();

    }

    public static void loadData(String path) throws SAXException, IOException, ParserConfigurationException {

        removeBlank(path);

        File checknull = new File("Data.xml");
        if(checknull.length() == 0){
            return;
        }

        Document doc;

        try {
            DocumentBuilderFactory dbFact = DocumentBuilderFactory.newInstance();
            DocumentBuilder dBuilt = dbFact.newDocumentBuilder();
            doc = dBuilt.parse(new File(path));

        } catch (SAXException sax) {
            throw new SAXException("Неверная конфигурация xml файла, проверьте исходный файл.");
        } catch (IOException io) {
            throw new IOException("...");
        }
        catch (ParserConfigurationException pce){
            throw new ParserConfigurationException();
        }

        ArrayDeque<Flat> data = new ArrayDeque<>();

        NodeList nodeList = doc.getElementsByTagName("Flat");

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            System.out.println(node.getNodeName());
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element eElement = (Element) node;

                String[] coordinates = eElement.getElementsByTagName("coordinates").item(0).getTextContent().trim().split("\\s+");
                String[] house = eElement.getElementsByTagName("house").item(0).getTextContent().trim().split("\\s+");
                int crdate = Integer.parseInt(eElement.getElementsByTagName("creationDate").item(0).getTextContent());

                Flat flat = new Flat(
                        Integer.parseInt(eElement.getElementsByTagName("id").item(0).getTextContent()),
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

    public static void addElement(Flat flat){
        XMLManager.data.add(flat);
    }

    public static void dropAll(){
        XMLManager.data.clear();
    }

    public static void writeToFile(String path) throws TransformerException, ParserConfigurationException, SAXException, IOException {

        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;
        builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        Element rootElement = doc.createElementNS(null, "data");
        doc.appendChild(rootElement);

        ArrayDeque<Flat> flats = XMLManager.getData();

        if(flats.isEmpty()){
            new FileWriter("Data.xml", false).close();
        }

        for(Flat flat: flats) {

            System.out.println(flat.getName());

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
            Transformer transformer = transformerFactory.newTransformer();

            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            DOMSource source = new DOMSource(doc);

            StreamResult console = new StreamResult(System.out);
            StreamResult file = new StreamResult(new File("Data.xml"));

//            transformer.transform(source, console);
            transformer.transform(source, file);
        }
    }

    private static Node parseNode(Document doc, String name, String value) {
        Element node = doc.createElement(name);
        node.appendChild(doc.createTextNode(value));
        return node;
    }
}
