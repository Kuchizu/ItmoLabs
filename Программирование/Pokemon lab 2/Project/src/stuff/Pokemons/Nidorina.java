package stuff.Pokemons;

import stuff.Moves.*;
import ru.ifmo.se.pokemon.*;

public class Nidorina extends Pokemon {
    public Nidorina(String name, int level){
        super(name, level);
        setStats(70,62,67,55,55,56);
        setType(Type.POISON);
        setMove(new Poison_String(), new Confide(), new Furry_swipes());
    }
}