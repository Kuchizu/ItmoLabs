package commands;

import collections.Coordinates;
import collections.Flat;
import collections.Furnish;
import collections.House;
import exceptions.CreateObjException;
import managers.XMLManager;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.Scanner;

/**
 * Add class used to add elements to collection.
 */
public class Add extends Command {
    public String getName() {
        return "add {element}";
    }

    public String getDesc() {
        return "Добавить новый элемент в коллекцию";
    }

    @Override
    public String execute(String arg) throws ParserConfigurationException, IOException, TransformerException, SAXException, CreateObjException {
        return null;
    }

    /**
     * Execute method that adds new Object to the collection
     */
    public Flat create() {
        Scanner obj = new Scanner(System.in);

        System.out.println("Введите Name (String), /q для отмены:");
        String name = obj.nextLine();

        if (name.equals("/q")){
            return null;
        }

        while (name.isEmpty()) {
            System.err.println("Строка не может быть пустой, введите заново");
            name = obj.nextLine();
        }

        System.out.println("Введите координаты x, y (Long, Double):");
        String cord = obj.nextLine();

        while (true) {
            while (cord.isEmpty()) {
                System.err.println("Поле не может быть null, введите заново");
                cord = obj.nextLine();
            }
            while (cord.split(" ").length != 2) {
                System.err.println("Введите два значения для x, y (Long, Double)");
                cord = obj.nextLine();
            }
            try {
                Long.parseLong(cord.split(" ")[0]);
            } catch (NumberFormatException n) {
                System.err.println("Ошибка при форматировании x, введите x, y заново");
                cord = obj.nextLine();
                continue;
            }
            try {
                Double.parseDouble(cord.split(" ")[1]);
                break;
            } catch (NumberFormatException n) {
                System.err.println("Ошибка при форматировании y, введите x, y заново");
                cord = obj.nextLine();
            }
        }

        System.out.println("Введите area (Integer): ");
        String areas = obj.nextLine();

        while (areas.isEmpty()) {
            System.err.println("Поле не может быть null, введите заново");
            areas = obj.nextLine();
        }

        int area;
        while (true) {
            try {
                area = Integer.parseInt(areas);
                break;
            } catch (NumberFormatException n) {
                System.err.println("Значение area должно быть int, введите заново");
                areas = obj.nextLine();
            }
        }

        while (area < 1) {
            System.err.println("Значение поля должно быть больше  0, введите заново");
            area = Integer.parseInt(obj.nextLine());
        }

        System.out.println("Введите numberOfRooms (Integer):");
        String numberOfRooms = obj.nextLine();

        while (true) {
            try {
                while (Integer.parseInt(numberOfRooms) < 1) {
                    System.err.println("Значение поля должно быть больше  0, введите заново");
                    numberOfRooms = obj.nextLine();
                }
                while (Integer.parseInt(numberOfRooms) > 19) {
                    System.err.println("Максимальное значение поля: 19, введите заново");
                    numberOfRooms = obj.nextLine();
                }
                break;
            } catch (NumberFormatException n) {
                System.err.println("Ошибка при форматировании, введите заново");
                numberOfRooms = obj.nextLine();
            }
        }

        System.out.println("Введите timeToMetroOnFoot (Float):");
        String timeToMetroOnFoot = obj.nextLine();
        while (true) {
            try {
                if (Float.parseFloat(timeToMetroOnFoot) < 1) {
                    System.err.println("Значение поля должно быть больше  0, введите заново");
                    timeToMetroOnFoot = obj.nextLine();
                }
                break;
            } catch (NumberFormatException n) {
                System.err.println("Ошибка при форматировании, введите заново");
                timeToMetroOnFoot = obj.nextLine();
            }
        }

        System.out.println("Введите timeToMetroByTransport (Double):");
        String timeToMetroByTransport = obj.nextLine();

        while (true) {
            try {
                while (Double.parseDouble(timeToMetroByTransport) < 1) {
                    System.err.println("Значение поля должно быть больше  0, введите заново");
                    timeToMetroByTransport = obj.nextLine();
                }
                break;
            } catch (NumberFormatException n) {
                System.err.println("Ошибка при форматировании, введите заново");
                timeToMetroByTransport = obj.nextLine();
            }
        }

        System.out.println("Введите furnish (Enum [NONE, DESIGNER, FINE, LITTLE]):");
        String furnish = obj.nextLine().trim();

        if (furnish.isEmpty()) {
            furnish = "NONE";
        }
        while (true) {
            try {
                Furnish.valueOf(furnish);
                break;
            } catch (IllegalArgumentException i) {
                System.err.println("Не найдено подходящего значения, введите одно из [NONE, DESIGNER, FINE, LITTLE]");
                furnish = obj.nextLine().trim();
            }

        }

        System.out.println("Введите name of House (String):");
        String hname = obj.nextLine();

        while(hname.isEmpty()){
            System.err.println("Строка не может быть пустой, введите заново");
            hname = obj.nextLine();
        }

        System.out.println("Введите year of House (Long):");

        long hyear;
        while (true) {
            try {
                String hsyear = obj.nextLine();

                while (Long.parseLong(hsyear) < 1) {
                    System.err.println("Значение поля должно быть больше  0, введите заново");
                    hsyear = obj.nextLine();
                }
                while (Long.parseLong(hsyear) > 853) {
                    System.err.println("Максимальное значение поля: 19, введите заново");
                    hsyear = obj.nextLine();
                }

                hyear = Long.parseLong(hsyear);
                break;

            } catch (NumberFormatException n) {
                System.err.println("Ошибка при форматировании, введите заново");
            }
        }

        System.out.println("Введите numberOfFloors of House (Long):");

        long hnumberOfFloors;
        while (true) {
            try {
                String hsfyear = obj.nextLine();

                while (Long.parseLong(hsfyear) < 1) {
                    System.err.println("Значение поля должно быть больше  0, введите заново");
                    hsfyear = obj.nextLine();
                }

                hnumberOfFloors = Long.parseLong(hsfyear);
                break;

            } catch (NumberFormatException n) {
                System.err.println("Ошибка при форматировании, введите заново");
            }
        }


        System.out.println("Введите numberOfLifts of House (Int):");

        int hnumberOfLifts;
        while (true) {
            try {
                String hslyear = obj.nextLine();

                while (Long.parseLong(hslyear) < 1) {
                    System.err.println("Значение поля должно быть больше  0, введите заново");
                    hslyear = obj.nextLine();
                }

                hnumberOfLifts = Integer.parseInt(hslyear);
                break;

            } catch (NumberFormatException n) {
                System.err.println("Ошибка при форматировании, введите заново");
            }
        }

        String[] coordinates = cord.split("\\s+");

        return new Flat(
                -1,
                -1,
                name,
                new Coordinates(Long.parseLong(coordinates[0]), Double.parseDouble(coordinates[1])),
                ZonedDateTime.now(),
                area,
                Integer.parseInt(numberOfRooms),
                Float.valueOf(timeToMetroOnFoot),
                Double.parseDouble(timeToMetroByTransport),
                Furnish.valueOf(furnish),
                new House(hname, hyear, hnumberOfFloors, hnumberOfLifts)
        );

    }

}
