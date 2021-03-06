package lambda.part2.example;

import data.Person;
import org.junit.Test;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.junit.Assert.*;

public class ArrowNotation {
    // нотация стрелочка
    // String -> int
    private static int strLength(String s) {
        return s.length();
    }

    @Test
    public void stringToInt() {
        // String -> Integer
        Function<String, Integer> strLength = ArrowNotation::strLength;
        assertEquals(5, strLength.apply("12345").intValue());
    }

    @Test
    public void personToString() {
        // Person -> String
        Function<Person, String> lastName = Person::getLastName;
        assertEquals("lastName", lastName.apply(new Person("f", "lastName", 0)));
    }

    @Test
    public void personToInt() {
        // Person -> Integer
        Function<Person, Integer> lastNameLength = p -> p.getLastName().length();
        assertEquals(5, lastNameLength.apply(new Person("a", "abcde", 0)).intValue());
    }

    // Предикат принимает что-то, возвращает boolean
    // (Person, String) -> boolean
    private static boolean sameLastName(Person p, String lastName) {
        return p.getLastName().equals(lastName);
    }

    @Test
    public void checkLastName() {
        // (Person, String) -> Boolean
        BiFunction<Person, String, Boolean> sameLastName = ArrowNotation::sameLastName;
        assertTrue(sameLastName.apply(new Person("a", "b", 0), "b"));
    }

    // Теория: функция (A, B, C, D, E) -> R => A -> B -> C -> D -> E -> R  -- операция каррирования

    // (Person, Person -> String) -> (String -> boolean)
    private static Predicate<String> propertyChecker(Person p, Function<Person, String> getProperty) {
        return s -> {
            String propertyValue = getProperty.apply(p);
            return s.equals(propertyValue);
        };
    }

    // (Person -> String) -> Person -> String -> boolean
    private static Function<Person, Predicate<String>> propertyChecker2(Function<Person, String> getProperty) {
        return p -> expectedPropValue -> getProperty.apply(p).equals(expectedPropValue);
    }

    @Test
    public void checkProperty2() {
        Function<Person, Predicate<String>> ageChecker = propertyChecker2(person -> String.valueOf(person.getAge()));
        Function<Person, Predicate<String>> lastNameChecker = propertyChecker2(Person::getLastName);
        Person person = new Person("a", "b", 33);
        assertTrue(lastNameChecker.apply(person).test("b"));
        assertFalse(lastNameChecker.apply(person).test("B"));
        assertTrue(ageChecker.apply(person).test("33"));
        assertFalse(ageChecker.apply(person).test("44"));
    }

    @Test
    public void checkProperty() {
        Person person = new Person("a", "b", 0);
        // Person -> String
        Function<Person, String> getFirstName = Person::getFirstName;
        // String -> boolean
        Predicate<String> checkFirstName = propertyChecker(person, getFirstName);
        assertTrue(checkFirstName.test("a"));
        // nice style
        assertTrue(propertyChecker(new Person("a", "b", 25), Person::getFirstName).test("a"));
    }
}
