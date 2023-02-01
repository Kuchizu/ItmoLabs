package stuff.Moves;
import ru.ifmo.se.pokemon.*;

public class Bulldoze extends PhysicalMove {
    public Bulldoze(){
        super(Type.GROUND, 60, 100);
    }

    @Override
    protected void applyOppDamage(Pokemon pokemon, double v) {
        super.applyOppDamage(pokemon, 1);
        Effect eff = new Effect();
        eff.stat(Stat.SPEED, -1);
        pokemon.addEffect(eff);
    }

    @Override
    protected String describe(){
        return "Бьёт и замедляет";
    }
}
