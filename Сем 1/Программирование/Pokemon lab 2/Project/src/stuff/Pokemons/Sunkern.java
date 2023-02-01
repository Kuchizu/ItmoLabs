package stuff.Pokemons;

import stuff.Moves.*;
import ru.ifmo.se.pokemon.*;

public class Sunkern extends Pokemon {
    public Sunkern(String name, int level){
        super(name, level);
        setStats(30,30,30,30,30,30);
        setType(Type.GRASS);
        setMove(new Confide(), new Mega_Drain(), new Swagger());
    }
}