package stuff.Pokemons;

import stuff.Moves.*;
import ru.ifmo.se.pokemon.*;

public class Nidoqueen extends Pokemon {
    public Nidoqueen(String name, int level){
        super(name, level);
        setStats(90,92,87,75,85,76);
        setType(Type.POISON, Type.GROUND);
        setMove(new Poison_String(), new Confide(), new Furry_swipes(), new Confide());
    }
}