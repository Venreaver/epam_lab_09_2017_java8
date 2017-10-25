package lambda.part2.example;

import data.Person;
import org.junit.Test;

import java.util.concurrent.ThreadLocalRandom;
import java.util.function.*;

import static org.junit.Assert.*;

public class OtherClasses {

    // int
    // long
    // double
    // + boolean
    // + void

    @Test
    // Supplier: () -> T
    // Arity 0 = арность нулевая, нет входных параметров
    public void suppliers() {
        @SuppressWarnings("RedundantStringConstructorCall")
        Supplier<String> strSupplier = () -> new String("a"); // () -> "a"; () -> String.valueOf("a"); - упадет на assertNotSame
        assertEquals("a", strSupplier.get());
        assertNotSame(strSupplier.get(), strSupplier.get()); // сравнение в физич памяти
        BooleanSupplier booleanSupplier = () -> true;
        assertTrue(booleanSupplier.getAsBoolean());
        IntSupplier intSupplier = () -> ThreadLocalRandom.current().nextInt();
        assertNotEquals(intSupplier.getAsInt(), intSupplier.getAsInt());
        LongSupplier longSupplier = () -> Integer.MAX_VALUE + 100L;
        assertTrue(longSupplier.getAsLong() > Integer.MAX_VALUE);
        DoubleSupplier doubleSupplier = () -> 0.1;
        assertEquals(0.1, doubleSupplier.getAsDouble(), 0.0001);
    }

    @Test
    // Consumer: T -> void
    // Arity 1
    public void consumers() {
        Consumer<String> stringConsumer = System.out::println;
        stringConsumer.accept("Some string");
        // IntConsumer
        // LongConsumer
        // DoubleConsumer
    }

    @Test
    // UnaryOperator: T -> T
    // Arity 1
    public void unaryOperator() {
        UnaryOperator<String> reverse = s -> new StringBuilder(s).reverse().toString();
        assertEquals("abc", reverse.apply("cba"));
        IntUnaryOperator negate = i -> -i;
        assertEquals(-1, negate.applyAsInt(1));
        // LongUnaryOperator
        // DoubleUnaryOperator
    }

    @Test
    // Function: T -> R
    // Arity 1
    public void functions() {
        Function<Person, String> getFirstName = Person::getFirstName;
        assertEquals("a", getFirstName.apply(new Person("a", "b", 33)));
        ToIntFunction<Person> getAge = Person::getAge;
        assertEquals(33, getAge.applyAsInt(new Person("a", "b", 33)));
        IntFunction<Person> withAge = i -> new Person("a", "b", i);
        assertEquals(new Person("a", "b", 666), withAge.apply(666));
        DoubleToLongFunction doubleToLong = d -> (long) d;
        assertEquals(666, doubleToLong.applyAsLong(666.66));
    }

    @Test
    // Predicate: T -> boolean
    // Arity 1
    public void predicates() {
        Predicate<String> isEmpty = String::isEmpty;
        assertEquals(true, isEmpty.test(""));
        IntPredicate positive = i -> i > 0;
        assertFalse(positive.test(-1));
        // LongPredicate
        // DoublePredicate
    }

    @Test
    // BinaryOperator: (T, T) -> T
    // Arity 2
    public void binaryOperators() {
        BinaryOperator<String> concat = String::concat;
        assertEquals("ab", concat.apply("a", "b"));
        IntBinaryOperator sum = (i1, i2) -> i1 + i2;
        assertEquals(3, sum.applyAsInt(1, 2));
        // LongBinaryOperator
        // DoubleBinaryOperator
    }

    @Test
    // BiFunction: (A, B) -> R
    // Arity 2
    public void biFunction() {
        BiFunction<Person, String, Person> changeFirstName = Person::withFirstName;
        assertEquals(new Person("c", "b", 0), changeFirstName.apply(new Person("a", "b", 0), "c"));
        ToIntBiFunction<Person, String> toIntBiFunction = (p, s) -> p.getAge() + s.length();
        assertEquals(10, toIntBiFunction.applyAsInt(new Person("", "", 8), "ab"));
        // ToLongBiFunction
        // ToDoubleBiFunction
    }

    @Test
    // BiPredicate: (A, B) -> boolean
    // Arity 2
    public void biPredicate() {
        BiPredicate<String, Person> checkFirstName = (s, p) -> s.equals(p.getFirstName());
    }

    @Test
    // BiConsumer: (A, B) -> void
    // Arity 2
    public void biConsumers() {
        BiConsumer<Person, String> biConsumer = null;
        ObjIntConsumer<String> checkLength = null;
    }

    @FunctionalInterface
    private interface PersonFactory {
        // (String, String, int) -> Person
        Person create(String name, String lastName, int age);
    }

    // частичная реализация - отличается от каррирования, т.к. каррирование только последовательно идет по всему
    // (((String, String, int) -> Person), String) -> (String, Int) -> Person
    private BiFunction<String, Integer, Person> partiallyApply(PersonFactory pf, String lastName) {
        return (name, age) -> pf.create(name, lastName, age);
    }

    // каррирование
    // ((String, String, int) -> Person) -> String -> String -> Int -> Person
    private Function<String, Function<String, IntFunction<Person>>> curry(
            PersonFactory pf) {
        return name -> lastName -> age -> pf.create(name, lastName, age);
    }

    @Test
    public void currying() {
        // (String, String, int) -> Person
        // тоже самое: final PersonFactory factory = (n, ln, a) -> new Person(n, ln, a);
        final PersonFactory factory = Person::new;
        // частичная реализация
        final BiFunction<String, Integer, Person> doe = (name, age) -> factory.create(name, "Doe", age);
        final BiFunction<String, Integer, Person> doeByParticularlyApply = partiallyApply(factory, "Doe");
        final Person mother = doe.apply("Samanta", 33);
        final Person father = doe.apply("Bob", 33);
        final Person son = doe.apply("John", 33);
        // String -> String -> int -> Person
        Function<String, Function<String, IntFunction<Person>>> curried1 = curry(Person::new);
        System.out.println(curried1.apply("J").apply("B").apply(28));
        final Function<String, Function<String, IntFunction<Person>>> curried = name -> (lastName -> age -> factory.create(name, lastName, age));
        final Function<String, IntFunction<Person>> john = curried.apply("John");
        final IntFunction<Person> johnDoeWithoutAge = john.apply("Doe");
        assertEquals(new Person("John", "Doe", 22), johnDoeWithoutAge.apply(22));
        assertEquals(new Person("John", "Doe", 33), johnDoeWithoutAge.apply(33));
    }
}
