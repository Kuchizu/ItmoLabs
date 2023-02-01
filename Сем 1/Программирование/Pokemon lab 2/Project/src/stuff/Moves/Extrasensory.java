package stuff.Moves;
import ru.ifmo.se.pokemon.*;

public class Extrasensory extends PhysicalMove {
    public Extrasensory(){
        super(Type.PSYCHIC, 80, 100);
    }

    private boolean flag;
    @Override
    public void applyOppEffects(Pokemon poc) {
        if (Math.random() <= 0.1) {
            flag = true;
            Effect.flinch(poc);
        }
    }
    @Override
    protected String describe(){
        if(flag) return "Бьёт и флинчит";
        else return "Бьёт";
    }
}
