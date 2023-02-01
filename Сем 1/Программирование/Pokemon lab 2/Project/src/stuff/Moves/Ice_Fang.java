package stuff.Moves;
import ru.ifmo.se.pokemon.*;

public class Ice_Fang extends PhysicalMove {
    public Ice_Fang(){
        super(Type.ICE, 65, 95);
    }
    private boolean flag1;
    private boolean flag2;
    @Override
    public void applyOppEffects(Pokemon poc) {
        if (Math.random() <= 0.1) {
            flag1 = true;
            Effect.freeze(poc);
        }
        if (Math.random() <= 0.1) {
            flag2 = true;
            Effect.flinch(poc);
        }
    }
    @Override
    protected String describe(){
        if(flag1 && flag2) return "Бьёт, фризит, флинчит";
        else if (flag1) return "Бьёт и фризит";
        else if(flag2) return "Бьёт и флинчит";
        else return "Бьёт";
    }
}
