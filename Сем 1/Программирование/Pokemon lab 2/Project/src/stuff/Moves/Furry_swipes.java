package stuff.Moves;
import ru.ifmo.se.pokemon.*;

public class Furry_swipes extends PhysicalMove{
    public Furry_swipes(){
        super(Type.NORMAL, 18, 80);
    }

    @Override
    protected void applyOppDamage(Pokemon pokemon, double v) {
        super.applyOppDamage(pokemon, v + v);
    }

    @Override
    protected String describe(){
        return "Бьёт несколько раз";
    }
}
