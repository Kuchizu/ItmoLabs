import java.util.Objects;

public class Skills implements Ability{

    private final String title;
    private final String info;

    public Skills(String title, String info){
        this.title = title;
        this.info = info;
    }

    public String getTitle(){
        return title;
    }
    public String getInfo(){
        return Objects.requireNonNullElse(info, "...");
    }

    @Override
    public String name() {
        return this.getTitle();
    }
}
