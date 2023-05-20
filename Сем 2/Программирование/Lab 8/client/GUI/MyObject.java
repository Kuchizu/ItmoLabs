package GUI;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class MyObject {

    private StringProperty name;
    private int size;
    private int weight;

    public void setSize(int size) {
        this.size = size;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }

    public MyObject(String name, int size, int weight) {
        this.name = new SimpleStringProperty(name);
        this.size = size;
        this.weight = weight;
    }

    public String getName() {
        return name.get();
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public int getSize() {
        return size;
    }

    public int getWeight() {
        return weight;
    }
}
