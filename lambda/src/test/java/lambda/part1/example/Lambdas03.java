package lambda.part1.example;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"Convert2Lambda", "FieldCanBeLocal"})
public class Lambdas03 {

    // SAM interface
    //

    @FunctionalInterface
    private interface GenericSum<T> {
        T sum(T a, T b);

        default T twice(T t) {
            return sum(t, t);
        }
    }

    @Test
    public void generic0() {
        final GenericSum<Integer> sum =
                new GenericSum<Integer>() {
                    @Override
                    public Integer sum(Integer i1, Integer i2) {
                        System.out.print("before sum");
                        return i1 + i2;
                    }
                };
        assertEquals(sum.sum(1, 2), Integer.valueOf(3));
    }

    @Test
    public void generic1() {
        // statement lambda
        final GenericSum<Integer> sum =
                (Integer i1, Integer i2) -> {
                    System.out.print("before sum");
                    return i1 + i2;
                };
        assertEquals(sum.sum(1, 2), Integer.valueOf(3));
    }

    @Test
    public void generic2() {
        // expression lambda
        final GenericSum<Integer> sum = (i1, i2) -> i1 + i2;
        assertEquals(sum.twice(1), Integer.valueOf(2));
        assertEquals(sum.sum(1, 2), Integer.valueOf(3));

    }

    private static String stringSum(String s1, String s2) {
        return s1 + s2;
    }

    @Test
    public void strSum() {
        // Class method-reference lambda
        final GenericSum<String> sum = Lambdas03::stringSum;
        assertEquals(sum.sum("a", "b"), "ab");
    }

    private final String delimiter = "-";

    private String stringSumWithDelimiter(String s1, String s2) {
        return s1 + delimiter + s2;
    }

    @Test
    public void strSum2() {
        // Object method-reference lambda
        final GenericSum<String> sum = this::stringSumWithDelimiter;
        assertEquals(sum.sum("a", "b"), "a-b");
    }
}