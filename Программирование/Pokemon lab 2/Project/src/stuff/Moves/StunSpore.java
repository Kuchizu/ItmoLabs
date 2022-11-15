package stuff.Moves;
import ru.ifmo.se.pokemon.*;

public class StunSpore extends SpecialMove{
    public StunSpore() {
        super(Type.GRASS, 0, 75);
    }

    @Override
    public void applyOppEffects(Pokemon p) {
        if  (!p.hasType(Type.ELECTRIC)){
            Effect.paralyze(p);
        }
    }
    @Override
    protected String describe(){
        return "парализует";
    }
}
