public class Main {
    public static void main(String[] args) {

        System.out.println();
        Person malchik = new Person("Мальчик");
        malchik.addSkill(new Skills("Сидеть на коленках", "Любит сидеть на коленках у мамочки UwU"));
        System.out.println();
        System.out.println();

        Person mom = new Person("Мама");
        mom.addSkill(new Skills("Обнимать", null));
        System.out.println();
        System.out.println();
        mom.addSkill(new Skills("Рассказывать сказки", null));
        System.out.println();
        System.out.println();

        Person father = new Person("Папа");
        father.addSkill(new Skills("Уйти на службу", null));

        System.out.println();
        System.out.println();

        Place loc1 = new Place("Локация 1");
        System.out.println();
        loc1.setType(Places.Kitchen);
        System.out.println();
        mom.addSkill(new Skills("Читать газету и пить кофе", loc1.getType().toString()));
        System.out.println();
        System.out.println();

        Place loc2 = new Place("Локация 2");
        System.out.println();
        loc2.setType(Places.Knees);
        System.out.println();
        malchik.addSkill(new Skills("Сидеть", loc2.getType().toString()));
        System.out.println();
        System.out.println();

        Person.go(malchik.getName(), loc1.typeName);
        System.out.println();
        Person.sit(malchik.getName(), mom.getName(), loc2.typeName);
        System.out.println();
        Person.come(malchik.getName(), loc1.typeName, mom.getName());
        System.out.println();
        Person.situntillwakeup(mom.getName(), malchik.getName());
        System.out.println();
        System.out.println();

    }
}