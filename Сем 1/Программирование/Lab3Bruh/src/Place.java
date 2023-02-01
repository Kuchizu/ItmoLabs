import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class Place extends AbsPlace{

    private PlaceEnum type;

    public Place(String name){
        this.placeName = name;
        System.out.printf("Место %s создано", name);
    }

    public void setType(@NotNull PlaceEnum type){
        this.type = type;
        switch (type) {
            case Kitchen -> typeName = "Кухня";
            case Knees -> typeName = "Коленки";
        }
        System.out.printf("%s объявлена как %s", placeName, typeName);
    }

    public PlaceEnum getType() {
        return this.type;
    }

    public String getPlace() {
        return Objects.requireNonNullElse(placeName, "...");
    }

}
