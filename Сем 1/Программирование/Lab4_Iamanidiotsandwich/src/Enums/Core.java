package Enums;

public enum Core {

    WOOD("Древесина"),
    COTTON("Хлопок"),
    PAPER("Бумага"),
    PLASTIC("Пластмасса");

    private final String type;
    Core(String name) {
        this.type = name;
    }

    public String getName() {
        return type;
    }

}
