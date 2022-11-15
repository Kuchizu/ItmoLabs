package stuff;

import ru.ifmo.se.pokemon.*;
import stuff.Pokemons.*;
import stuff.Moves.*;

public class Main {
    public static void main(String[] args) {
        Battle b = new Battle();

        Nidoqueen a1 = new Nidoqueen("Нидоквин", 1);
        NidoranF a2 = new NidoranF("Нидоран", 1);
        Sunkern a3 = new Sunkern("Санкенр", 1);

        Nidorina f1 = new Nidorina("Нидорина", 1);
        Suicune f2 = new Suicune("Суикун", 1);
        Sunflora f3 = new Sunflora("Санфлора", 1);

        b.addAlly(a1);
        b.addAlly(a2);
        b.addAlly(a3);

        b.addFoe(f1);
        b.addFoe(f2);
        b.addFoe(f3);

        b.go();

    }
}



// Конструктор по умолчанию   абстракция принцип         инкап