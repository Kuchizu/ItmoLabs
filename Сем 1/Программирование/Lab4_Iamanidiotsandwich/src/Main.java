import BuildingObjects.*;
import Enums.Color;
import Enums.Core;
import Exceptions.CapacityException;
import Exceptions.CompositException;
import ObjMaker.*;
import Furnitures.*;
import characters.*;

public class Main {
    public static void main(String[] args) throws CapacityException, CompositException {

        BuildingObject home = new Home("Дом", 1, 100, 20);
        BuildingObject school = new School("Школа", 2, 315, 50);
        BuildingObject job = new Job("Работа", 3, 215, 35);

        Human char1 = new Malish("Малыш", 1, home);
        Human char2 = new Mama("Мама", 2, home);
        Human char3 = new Papa("Папа", 3, job);
        Human char4 = new Betan("Бетан", 4, school);
        Human char5 = new Betan("Боссе", 5, school);

        BuildingObject area = new Park("Площадка", 1, 1000, 1000);

        System.out.println(home.capacity(5));

        CompositMaker wood = new CompositMaker(Core.WOOD);

        Bed bed = new Bed("Кровать", 5, Color.WHITE, wood);
        CompositMaker sheet = new CompositMaker(Core.COTTON);

        Chair chair = new Chair("Стул", 2, Color.PURPLE, wood);

        bed.sleep(char1.getName());

        CompositMaker paper = new CompositMaker(Core.PAPER);
        paper.size(100);

        char1.sleep();
        char4.go(school.getName());
        char5.go(school.getName());
        char3.go(job.getName());

        char2.sit(chair);
        NewsPaper newsPaper = new NewsPaper("Газета", 1, Color.WHITE, paper);

        System.out.println(school.capacity(500));

        System.out.println("Размер школы " + school.getSquare());

        System.out.println(char1.getName() + " находится в " + char2.getLocation());
        char2.read(newsPaper.getName());

        char1.go("Кухня");
        char2.say("Сказки");

        Blanket blanket = new Blanket("Простыня", 1000, Color.BLACK, wood);
        Blanket.withholes blanket2 = new Blanket.withholes(blanket);

        char2.hug(char1);
        char1.sleep();

        Furniture bed2 = new Furniture("Bed2", 100, Color.GREEN, wood){
            void sit(Malish malish){
                System.out.println("Малыш сидит");
            }
        };

        /* Exception */

        newsPaper.setPages(-100);
        Chair chair2 = new Chair("Стул2", 10, Color.GREEN, paper);
        paper.size(10000);

    }
}