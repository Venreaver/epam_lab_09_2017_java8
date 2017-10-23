package lambda.part1.exercise;

import org.junit.Test;

import java.util.Arrays;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

public class Lambdas03Exercise {
    private interface GenericProduct<T> {
        T prod(T a, int i);

        default T twice(T t) {
            return prod(t, 2);
        }
    }

    @Test
    public void generic0() {
        // Use anonymous class
        final GenericProduct<Integer> prod = new GenericProduct<Integer>() {
            @Override
            public Integer prod(Integer a, int i) {
                return a * i;
            }
        };
        assertEquals(Integer.valueOf(6), prod.prod(3, 2));
    }

    @Test
    public void generic1() {
        // Use statement lambda
        final GenericProduct<Integer> prod = (Integer num, int i) -> {
            int result = num * i;
            return result;
        };
        assertEquals(Integer.valueOf(6), prod.prod(3, 2));
    }

    @Test
    public void generic2() {
        // Use expression lambda
        final GenericProduct<Integer> prod = (Integer num, int i) -> num * i;
        assertEquals(Integer.valueOf(6), prod.prod(3, 2));
    }

    private static String stringProd(String s, int i) {
        final StringBuilder sb = new StringBuilder();
        for (int j = 0; j < i; j++) {
            sb.append(s);
        }
        return sb.toString();
    }

    private static String stringProdByStream(String s, int i) {
        return Stream.generate(() -> s).limit(i).collect(Collectors.joining());
    }

    @Test
    public void strSum() {
        // use stringProd :: class method-reference lambda
        final GenericProduct<String> prod = Lambdas03Exercise::stringProd; // use stringProd;
        assertEquals("aa", prod.prod("a", 2));
    }

    @Test
    public void strSumByStream() {
        // use stringProdByStream :: class method-reference lambda
        final GenericProduct<String> prod = Lambdas03Exercise::stringProdByStream; // use stringProdByStream;
        assertEquals("aa", prod.prod("a", 2));
    }

    private final String delimiter = "-";

    private String stringSumWithDelimiter(String s, int i) {
        final StringJoiner sj = new StringJoiner(delimiter);
        for (int j = 0; j < i; j++) {
            sj.add(s);
        }
        return sj.toString();
    }

    private String stringSumWithDelimiterByStream(String s, int i) {
        return Stream.generate(() -> s).limit(i).collect(Collectors.joining(delimiter));
    }

    @Test
    public void strSum2() {
        // use stringSumWithDelimiter :: object method-reference lambda
        final GenericProduct<String> prod = this::stringSumWithDelimiter;
        assertEquals("a-a-a", prod.prod("a", 3));
    }

    @Test
    public void strSum2ByStream() {
        // use stringSumWithDelimiterByStream :: object method-reference lambda
        final GenericProduct<String> prod = this::stringSumWithDelimiterByStream;
        assertEquals("a-a-a", prod.prod("a", 3));
    }
}
