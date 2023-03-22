package collections;

import java.time.ZonedDateTime;

public class Flat {
    private Integer id; //Поле не может быть null, Значение поля должно быть больше 0, Значение этого поля должно быть уникальным, Значение этого поля должно генерироваться автоматически
    private String name; //Поле не может быть null, Строка не может быть пустой
    private Coordinates coordinates; //Поле не может быть null
    private java.time.ZonedDateTime creationDate; //Поле не может быть null, Значение этого поля должно генерироваться автоматически
    private Integer area; //Поле не может быть null, Значение поля должно быть больше 0
    private Integer numberOfRooms; //Максимальное значение поля: 19, Значение поля должно быть больше 0
    private Float timeToMetroOnFoot; //Значение поля должно быть больше 0
    private Double timeToMetroByTransport; //Значение поля должно быть больше 0
    private Furnish furnish; //Поле может быть null
    private House house; //Поле не может быть null

    public Flat(String name, Coordinates coordinates, Integer area, Integer numberOfRooms, Float timeToMetroOnFoot, Double timeToMetroByTransport, Furnish furnish, House house) {
        this.id = Math.abs((int) System.nanoTime());
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = ZonedDateTime.now();
        this.area = area;
        this.numberOfRooms = numberOfRooms;
        this.timeToMetroOnFoot = timeToMetroOnFoot;
        this.timeToMetroByTransport = timeToMetroByTransport;
        this.furnish = furnish;
        this.house = house;
    }

    public Flat(int id, String name, Coordinates coordinates, java.time.ZonedDateTime creationDate, Integer area, Integer numberOfRooms, Float timeToMetroOnFoot, Double timeToMetroByTransport, Furnish furnish, House house) {
        this.id = id;
        this.name = name;
        this.coordinates = coordinates;
        this.creationDate = creationDate;
        this.area = area;
        this.numberOfRooms = numberOfRooms;
        this.timeToMetroOnFoot = timeToMetroOnFoot;
        this.timeToMetroByTransport = timeToMetroByTransport;
        this.furnish = furnish;
        this.house = house;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Coordinates getCoordinates() {
        return coordinates;
    }

    public void setCoordinates(Coordinates coordinates) {
        this.coordinates = coordinates;
    }

    public ZonedDateTime getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(ZonedDateTime creationDate) {
        this.creationDate = creationDate;
    }

    public Integer getArea() {
        return area;
    }

    public void setArea(Integer area) {
        this.area = area;
    }

    public Integer getNumberOfRooms() {
        return numberOfRooms;
    }

    public void setNumberOfRooms(Integer numberOfRooms) {
        this.numberOfRooms = numberOfRooms;
    }

    public Float getTimeToMetroOnFoot() {
        return timeToMetroOnFoot;
    }

    public void setTimeToMetroOnFoot(Float timeToMetroOnFoot) {
        this.timeToMetroOnFoot = timeToMetroOnFoot;
    }

    public Double getTimeToMetroByTransport() {
        return timeToMetroByTransport;
    }

    public void setTimeToMetroByTransport(Double timeToMetroByTransport) {
        this.timeToMetroByTransport = timeToMetroByTransport;
    }

    public Furnish getFurnish() {
        return furnish;
    }

    public void setFurnish(Furnish furnish) {
        this.furnish = furnish;
    }

    public House getHouse() {
        return house;
    }

    public void setHouse(House house) {
        this.house = house;
    }

    @Override
    public String toString(){
        return String.format(
                """
    ***    ***    ***

Flat №%s

 name: %s
 coordinates: %s
 creationDate: %s
 area: %s
 numberOfRooms: %s
 timeToMetroOnFoot: %s
 timeToMetroByTransport: %s
 furnish: %s

House:

%s
    ***    ***    ***
                """,
                this.getId(),
                this.getName(),
                this.getCoordinates().toString(),
                this.getCreationDate(),
                this.getArea(),
                this.getNumberOfRooms(),
                this.getTimeToMetroOnFoot(),
                this.getTimeToMetroByTransport(),
                this.getFurnish(),
                this.getHouse().toString()
        );
    }

}

