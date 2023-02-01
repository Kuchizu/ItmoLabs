package stuff.Moves;
import ru.ifmo.se.pokemon.*;

public class Confide extends StatusMove {
    public Confide(){
        super();
    }

    protected void applyOppDamage(Pokemon pokemon, double v) {
        Effect eff = new Effect();
        eff.stat(Stat.SPECIAL_ATTACK, -1);
        pokemon.addEffect(eff);
    }

    @Override
    protected String describe(){
        return  "Бьёт и снижает атаку";
    }
}
