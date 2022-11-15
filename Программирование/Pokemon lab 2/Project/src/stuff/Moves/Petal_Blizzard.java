package stuff.Moves;
import ru.ifmo.se.pokemon.*;

public class Petal_Blizzard extends PhysicalMove {
    public Petal_Blizzard(){
        super(Type.GRASS, 90, 100);
    }

    @Override
    protected String describe(){
        return "Бьёт";
    }
}
