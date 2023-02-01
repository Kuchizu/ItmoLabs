public abstract class Humanable implements Mammal {

    public String name;
    public int age;

    public Humanable(){
        this.name = "Noname";
        this.age = -1;
    }
    public Humanable(String name){
        this.name = name;
    }
    public Humanable(String name, int age){
        this.name = name;
        this.age = age;
    }

    public String getName(){
        return name;
    }

}
