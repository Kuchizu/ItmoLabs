package stuff.Pokemons;

import stuff.Moves.*;
import ru.ifmo.se.pokemon.*;

public class NidoranF extends Pokemon {
    public NidoranF(String name, int level){
        super(name, level);
        setStats(55,47,52,40,40,41);
        setType(Type.POISON);
        setMove(new Poison_String(), new Confide());
    }
}