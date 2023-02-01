package stuff.Moves;
import ru.ifmo.se.pokemon.*;

public class Mega_Drain extends SpecialMove {
    public Mega_Drain(){
        super(Type.GRASS, 40, 100);
    }

    @Override
    protected void applySelfEffects(Pokemon pokemon) {
        super.applySelfEffects(pokemon);
        Effect eff = new Effect();
        eff.stat(Stat.HP, 50);
        pokemon.addEffect(eff);
    }

    @Override
    protected String describe(){
        return "Сушит и пополняет себе хп";
    }
}
