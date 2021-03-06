package lambda.part1.example;

import data.Person;
import org.junit.Test;

@SuppressWarnings({"Convert2Lambda", "Anonymous2MethodRef"})
public class Lambdas04 {

    private void run(Runnable r) {
        r.run();
    }

    @Test
    public void closure() {
        // effectively final - можно не объявлять как final переменную, которая используется в функц. прог с Java 8
        Person person = new Person("John", "Galt", 33);
        run(new Runnable() {
            @Override
            public void run() {
                person.print();
            }
        });
        //person = new Person("a", "a", 44);
    }

    @Test
    public void closure_lambda() {
        Person person = new Person("John", "Galt", 33);
        // statement lambda
        run(() -> {
            person.print();
        });
        // expression lambda
        run(() -> person.print());
        // method reference
        run(person::print);
    }

    private Person _person = null;

    public Person getPerson() {
        return _person;
    }

    @Test
    public void closure_this_lambda() {
        _person = new Person("John", "Galt", 33);
        run(() -> /*this.*/_person.print()); // GC Problems - замкнулись на this
        run(/*this.*/_person::print);        // замкнулись на this _person
        _person = new Person("a", "a", 1);
        run(() -> /*this.*/_person.print()); // GC Problems - замкнулись на this
        run(/*this.*/_person::print);        // замкнулись на this _person
    }

    // подготавливаем лямбду, но run вызван не будет
    private Runnable runLater(Runnable r) {
        return () -> {
            System.out.println("before run");
            r.run();
        };
    }

    @Test
    public void closure_this_lambda2() {
        _person = new Person("John", "Galt", 33);
        //final Person person = _person;
        final Runnable r1 = runLater(() -> _person.print()); // замкнет на this, потом сменит значение перед run
        final Runnable r2 = runLater(getPerson()::print);    // замкнет на _person, потом НЕ сменит значение перед run
        Runnable r3 = runLater(this.getPerson()::print);     // можно без final обходиться, будет тоже самое, что и в прошл стр.
        _person = new Person("a", "a", 1);
        r1.run();
        r2.run();
        r3.run();
    }
}
