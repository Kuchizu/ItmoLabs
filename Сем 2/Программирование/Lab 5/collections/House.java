package collections;

public class House {
    private String name; //Поле может быть null
    private Long year; //Максимальное значение поля: 853, Значение поля должно быть больше 0
    private long numberOfFloors; //Значение поля должно быть больше 0
    private int numberOfLifts; //Значение поля должно быть больше 0

    public House(String name, Long year, long numberOfFloors, int numberOfLifts) {
        this.name = name;
        this.year = year;
        this.numberOfFloors = numberOfFloors;
        this.numberOfLifts = numberOfLifts;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getYear() {
        return year;
    }

    public void setYear(Long year) {
        this.year = year;
    }

    public long getNumberOfFloors() {
        return numberOfFloors;
    }

    public void setNumberOfFloors(long numberOfFloors) {
        this.numberOfFloors = numberOfFloors;
    }

    public int getNumberOfLifts() {
        return numberOfLifts;
    }

    public void setNumberOfLifts(int numberOfLifts) {
        this.numberOfLifts = numberOfLifts;
    }

    @Override
    public String toString(){
        return String.format(
"""
    name: %s
    year: %s
    numberOfFloors: %s
    numberOfLifts: %s
""",

                this.getName(),
                this.getYear(),
                this.getNumberOfFloors(),
                this.getNumberOfLifts()
        );
    }

}
