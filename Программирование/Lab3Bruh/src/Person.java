import java.util.ArrayList;
import java.util.List;

public class Person extends Human {
    private final List<Skills> skills = new ArrayList<Skills>();

    public Person(String name){
        this.name = name;
        System.out.println("Человек - " + name + " сгенерирован");
    }
    public List<Skills> getSkills() {
        return skills;
    }

    public void addSkill(Skills skill){
        skills.add(skill);
        System.out.printf(
                "Человеку %s добавлено умение %s (%s)",
                this.getName(), skill.getTitle(), skill.getInfo()
        );
    }

    public static void go(String name, String place){
        System.out.printf("%s пришлёпал на %s", name, place);
    }

    public static void sit(String name1, String name2, String place){
        System.out.printf("Хотя %s уже большой он с удовольствием сидит у %s на %s", name1, name2, place);
    }

    public static void come(String name1, String place, String name2){
        System.out.printf("Когда %s вошел в %s, %s читала газету и пила кофе", name1, place, name2);
    }

    public static void situntillwakeup(String name1, String name2){
        System.out.printf("%s обняла его и нежно прижала к себе. Так они сидели пока %s окончательно не проснулся.", name1, name2);
    }
}
