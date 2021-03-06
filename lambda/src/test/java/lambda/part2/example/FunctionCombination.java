package lambda.part2.example;

import data.Person;
import org.junit.Test;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("UnnecessaryLocalVariable")
public class FunctionCombination {

    @Test
    public void personToInt0() {
        // Person -> Integer
        Function<Person, Integer> lastNameLength = p -> p.getLastName().length();
        assertEquals(5, lastNameLength.apply(new Person("a", "abcde", 0)).intValue());
    }

    // (Person -> String, String -> Integer) -> (Person -> Integer)
//    private Function<Person, Integer> personStringPropertyToInt(
//            Function<Person, String> personToString,
//            Function<String, Integer> stringToInteger) {
//        return p -> {
//            String str = personToString.apply(p);
//            Integer result = stringToInteger.apply(str);
//            return result;
//        };
//    }

    private Function<Person, Integer> personStringPropertyToInt(
            Function<Person, String> personToString,
            Function<String, Integer> stringToInteger) {
        return p -> stringToInteger.apply(personToString.apply(p));
    }

    @Test
    public void personToInt1() {
        Function<Person, String> getLastName = Person::getLastName;
        Function<String, Integer> getLength = String::length;
        Function<Person, Integer> lastNameLength = personStringPropertyToInt(getLastName, getLength);
        assertEquals(5, lastNameLength.apply(new Person("a", "abcde", 0)).intValue());
    }

    // (A -> B, B -> C) -> A -> C
    private <A, B, C> Function<A, C> andThen(Function<A, B> f1, Function<B, C> f2) {
        return a -> f2.apply(f1.apply(a));
    }

    @Test
    public void personToInt2() {
        Function<Person, String> getLastName = Person::getLastName;
        Function<String, Integer> getLength = String::length;
        Function<Person, Integer> lastNameLength = andThen(getLastName, getLength);
        assertEquals(5, lastNameLength.apply(new Person("a", "abcde", 0)).intValue());
    }

    static class MyClass {

    }

    static class InheritedMyClass extends MyClass {

    }

    @Test
    public void personToInt3() {
        Function<Person, String> getLastName = Person::getLastName;
        Function<String, Integer> getLength = String::length;
        Function<Person, Integer> lastNameLength = getLastName.andThen(getLength);
        assertEquals(5, lastNameLength.apply(new Person("a", "abcde", 0)).intValue());
    }

    public int sum(int x, int y, int z) {
        return x + y + z;
    }

    // same: public static Function<Integer, Function<Integer, Function<Integer, Integer>>> curry()
    public Function<Integer, IntFunction<IntUnaryOperator>> currySum() {
        return x -> y -> z -> sum(x, y, z);
    }

    public void method() {
        Object a = null;
        Object b = null;
        boolean eq = a != null ? a.equals(b) : b == null;
        boolean equ = Objects.equals(a, b);
    }
}
