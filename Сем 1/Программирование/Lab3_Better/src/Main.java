import BuildingObjects.*;
import actions.Locationable;
import characters.*;

public class Main {
    public static void main(String[] args) {

        BuildingObject home = new Home("Дом", 1, 100, 20);
        BuildingObject school = new School("Школа", 2, 315, 50);
        BuildingObject job = new Job("Работа", 3, 215, 35);

        Human char1 = new Malish("Малыш", 1, home);
        Human char2 = new Mama("Мама", 2, home);
        Human char3 = new Papa("Папа", 3, job);
        Human char4 = new Betan("Бетан", 4, school);
        Human char5 = new Betan("Боссе", 5, school);

        System.out.println();
        System.out.println(home.capacity(5));

        char1.sleep();
        char4.go(school.getName());
        char5.go(school.getName());
        char3.go(job.getName());

        System.out.println(school.capacity(500));
        System.out.println("Размер школы " + school.getSquare());

        System.out.println(char1.getName() + " находится в " + char2.getLocation());

        char1.go("Кухня");
        char2.read("Газета");
        char2.say("Сказки");

        char2.hug(char1);
        char1.sleep();

    }
}