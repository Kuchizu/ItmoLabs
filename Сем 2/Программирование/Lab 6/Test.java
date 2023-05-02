import collections.Flat;
import managers.XMLManager;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class Test {
    public class q{
    }
    public class Animal extends q{

        public void feed() {

            System.out.println("Animal.feed()");
        }
    }

    public class Pet extends Animal {

        public void call() {

            System.out.println("Pet.call()");
        }
    }

    public class Cat extends Pet {

        public void meow() {

            System.out.println("Cat.meow()");
        }
    }

    public void iterateAnimals(Collection<? super Animal> animals) {
        animals.add(new Cat());



    }
    public void main(String[] args) throws ParserConfigurationException, IOException, TransformerException, SAXException {
        System.out.println(ZonedDateTime.now());

        List<Animal> animals = new ArrayList<>();
        animals.add(new Animal());
        animals.add(new Animal());

        iterateAnimals(animals);

    }

}
