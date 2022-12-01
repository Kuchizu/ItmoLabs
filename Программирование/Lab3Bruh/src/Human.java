public abstract class Human implements Homosapiens {

    public String name;
    public int age;

    public Human(){
        this.name = "Noname";
        this.age = -1;
    }
    public Human(String name){
        this.name = name;
    }
    public Human(String name, int age){
        this.name = name;
        this.age = age;
    }

    public String getName(){
        return name;
    }

}
