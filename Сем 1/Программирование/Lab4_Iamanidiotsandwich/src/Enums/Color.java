package Enums;

public enum Color {
    BLACK("чёрный"),
    WHITE("белый"),
    RED("красный"),
    BLUE("голубой"),
    GREEN("зелёный"),
    PURPLE("пурпурный");

    private final String name;
    Color(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
