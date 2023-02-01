package Enums;

public enum Color {
    BLACK("чёрный"),
    GREEN("зелёный"),
    BLUE("голубой"),
    EMERALDGREEN("изумрудно-зелёный"),
    WHITE("белый"),
    BRIGHTWHITE("ярко-белый"),
    RED("красный"),
    LightCherry("светло-вишнёвый"),
    DARKPURPLE("тёмно-фиолетовый"),
    BRIGHTBLUE("светло-голубой"),
    PURPLE("пурпурный");

    private final String name;
    Color(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
