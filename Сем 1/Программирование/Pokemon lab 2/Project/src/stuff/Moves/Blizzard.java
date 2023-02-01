package stuff.Moves;
import ru.ifmo.se.pokemon.*;

public class Blizzard extends SpecialMove {
    public Blizzard(){
        super(Type.ICE, 110, 70);
    }
    private boolean flag;
    @Override
    public void applyOppEffects(Pokemon poc) {
        if (Math.random() <= 0.1) {
            flag = true;
            Effect.freeze(poc);
        }
    }
    @Override
    protected String describe(){
        if(flag) return "Бьёт и фризит";
        else return "Бьёт";
    }
}
