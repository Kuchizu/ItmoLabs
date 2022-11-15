package stuff.Pokemons;

import stuff.Moves.*;
import ru.ifmo.se.pokemon.*;

public class Sunflora extends Pokemon {
    public Sunflora(String name, int level){
        super(name, level);
        setStats(75,75,55,105,85,30);
        setType(Type.GRASS);
        setMove(new Confide(), new Mega_Drain(), new Swagger(), new Petal_Blizzard());
    }
}