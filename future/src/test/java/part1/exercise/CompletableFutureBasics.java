package part1.exercise;

import data.raw.Employee;
import data.raw.Generator;
import data.raw.Person;
import db.SlowCompletableFutureDb;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

public class CompletableFutureBasics {

    private static SlowCompletableFutureDb<Employee> employeeDb;
    private static List<String> keys;

    @BeforeClass
    public static void before() {
        Map<String, Employee> employeeMap = Generator.generateEmployeeList(1000)
                                                     .stream()
                                                     .collect(toMap(e -> getKeyByPerson(e.getPerson()), Function.identity(), (a, b) -> a));
        employeeDb = new SlowCompletableFutureDb<>(employeeMap);
        keys = new ArrayList<>(employeeMap.keySet());
    }

    private static String getKeyByPerson(Person person) {
        return person.getFirstName() + "_" + person.getLastName() + "_" + person.getAge();
    }

    @AfterClass
    public static void after() {
        try {
            employeeDb.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void createNonEmpty() throws ExecutionException, InterruptedException {
        Person person = new Person("John", "Galt", 33);

        // TODO Create non empty Optional
        Optional<Person> optPerson = null;

        assertTrue(optPerson.isPresent());
        assertEquals(person, optPerson.get());

        // TODO Create stream with a single element
        Stream<Person> streamPerson = null;

        List<Person> persons = streamPerson.collect(toList());
        assertThat(persons.size(), is(1));
        assertEquals(person, persons.get(0));

        // TODO Create completed CompletableFuture
        CompletableFuture<Person> futurePerson = null;

        assertTrue(futurePerson.isDone());
        assertEquals(person, futurePerson.get());
    }

    @Test
    public void createEmpty() throws ExecutionException, InterruptedException {
        // TODO Create empty Optional
        Optional<Person> optPerson = null;

        assertFalse(optPerson.isPresent());

        // TODO Create empty stream
        Stream<Person> streamPerson = null;

        List<Person> persons = streamPerson.collect(toList());
        assertThat(persons.size(), is(0));

        // TODO Complete CompletableFuture with NoSuchElementException
        CompletableFuture<Person> futurePerson = null;
        // futurePerson.???

        assertTrue(futurePerson.isCompletedExceptionally());
        assertTrue(futurePerson
                .thenApply(x -> false)
                .exceptionally(t -> t.getCause() instanceof NoSuchElementException).get());
    }

    @Test
    public void forEach() throws ExecutionException, InterruptedException {
        Person person = new Person("John", "Galt", 33);

        // TODO Create non empty Optional
        Optional<Person> optPerson = null;

        CompletableFuture<Person> result1 = new CompletableFuture<>();

        // TODO using optPerson.ifPresent complete result1
        assertEquals(person, result1.get());

        // TODO Create stream with a single element
        Stream<Person> streamPerson = null;

        CompletableFuture<Person> result2 = new CompletableFuture<>();

        // TODO Using streamPerson.forEach complete result2
        assertEquals(person, result2.get());

        // TODO Create completed CompletableFuture
        CompletableFuture<Person> futurePerson = null;

        CompletableFuture<Person> result3 = new CompletableFuture<>();

        // TODO Using futurePerson.thenAccept complete result3
        assertEquals(person, result3.get());
    }

    @Test
    public void map() throws ExecutionException, InterruptedException {
        Person person = new Person("John", "Galt", 33);

        // TODO Create non empty Optional
        Optional<Person> optPerson = null;

        // TODO get Optional<first name> from optPerson
        Optional<String> optFirstName = null;

        assertEquals(person.getFirstName(), optFirstName.get());

        // TODO Create stream with a single element
        Stream<Person> streamPerson = null;

        // TODO Get Stream<first name> from streamPerson
        Stream<String> streamFirstName = null;

        assertEquals(person.getFirstName(), streamFirstName.collect(toList()).get(0));

        // TODO Create completed CompletableFuture
        CompletableFuture<Person> futurePerson = null;

        // TODO Get CompletableFuture<first name> from futurePerson
        CompletableFuture<String> futureFirstName = null;

        assertEquals(person.getFirstName(), futureFirstName.get());
    }

    @Test
    public void flatMap() throws ExecutionException, InterruptedException {
        Person person = employeeDb.get(keys.get(0)).thenApply(Employee::getPerson).get();

        // TODO Create non empty Optional
        Optional<Person> optPerson = null;

        // TODO Using flatMap and .getFirstName().codePoints().mapToObj(p -> p).findFirst()
        // TODO get the first letter of first name if any
        Optional<Integer> optFirstCodePointOfFirstName = null;

        assertEquals(Integer.valueOf(65), optFirstCodePointOfFirstName.get());

        // TODO Create stream with a single element
        Stream<Person> streamPerson = null;

        // TODO Using flatMapToInt and .getFirstName().codePoints() get codepoints stream from streamPerson
        IntStream codePoints = null;

        int[] codePointsArray = codePoints.toArray();
        assertEquals(person.getFirstName(), new String(codePointsArray, 0, codePointsArray.length));

        // TODO Create completed CompletableFuture
        CompletableFuture<Person> futurePerson = null;

        // TODO Get CompletableFuture<Employee> from futurePerson using getKeyByPerson and employeeDb
        CompletableFuture<Employee> futureEmployee = null;

        assertEquals(person, futureEmployee.thenApply(Employee::getPerson).get());
    }
}
