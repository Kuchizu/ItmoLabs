package stuff.Moves;
import ru.ifmo.se.pokemon.*;

public class Swagger extends SpecialMove{
    public Swagger(){
        super(Type.NORMAL,0,90);
    }

    @Override
    protected void applyOppEffects(Pokemon opp){
        opp.confuse();
        opp.setMod(Stat.ATTACK,2);
    }
    @Override
    protected String describe(){
        return "флексит";
    }
}