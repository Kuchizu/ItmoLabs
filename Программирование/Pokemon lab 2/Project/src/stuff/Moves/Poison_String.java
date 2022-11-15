package stuff.Moves;
import ru.ifmo.se.pokemon.*;

public class Poison_String extends PhysicalMove {
    public Poison_String(){
        super(Type.POISON, 15, 100);
    }
    private boolean flag;
    @Override
    public void applyOppEffects(Pokemon poc) {
        if (Math.random() <= 0.3) {
            flag = true;
            Effect.poison(poc);
        }
    }

    @Override
    protected String describe(){
        if(flag) return "Бьёт и травит";
        else return "Бьёт";
    }
}
