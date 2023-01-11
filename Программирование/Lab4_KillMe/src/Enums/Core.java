package Enums;

public enum Core {

    METAL("Метал"),
    COSTMOPLASTIC("Космопластмасса"),
    STAINLESSTEEL("Сверхпрочная нержавеющая сталь"),
    THERMOPLASTIC("Термопластмасса");

    private final String type;
    Core(String name) {
        this.type = name;
    }

    public String getName() {
        return type;
    }

}
