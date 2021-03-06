package lambda.part3.exercise;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"WeakerAccess"})
public class Mapping {

    private static class MapHelper<T> {

        private final List<T> list;

        public MapHelper(List<T> list) {
            this.list = list;
        }

        public List<T> getList() {
            return list;
        }

        // ([T], T -> R) -> [R]
        public <R> MapHelper<R> map(Function<T, R> f) {
            List<R> result = new ArrayList<>(list.size());
            list.forEach(t -> result.add(f.apply(t)));
            return new MapHelper<R>(result);
        }

        // ([T], T -> [R]) -> [R]
        public <R> MapHelper<R> flatMap(Function<T, List<R>> f) {
            List<R> result = new ArrayList<>(list.size());
            list.forEach(t -> result.addAll(f.apply(t)));
            return new MapHelper<R>(result);
        }
    }

    @Test
    public void mapping() {
        List<Employee> employees = Arrays.asList(
                new Employee(new Person("a", "Galt", 30),
                        Arrays.asList(
                                new JobHistoryEntry(2, "dev", "epam"),
                                new JobHistoryEntry(1, "dev", "google")
                        )),
                new Employee(new Person("b", "Doe", 40),
                        Arrays.asList(
                                new JobHistoryEntry(3, "qa", "yandex"),
                                new JobHistoryEntry(1, "qa", "epam"),
                                new JobHistoryEntry(1, "dev", "abc")
                        )),
                new Employee(new Person("c", "White", 50),
                        Collections.singletonList(
                                new JobHistoryEntry(5, "qa", "epam")
                        ))
        );
        List<Employee> mappedEmployees = new MapHelper<>(employees)
                .map(e -> e.withPerson(e.getPerson().withFirstName("John"))) // Изменить имя всех сотрудников на John .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                .map(e -> e.withJobHistory((addOneYear(e.getJobHistory())))) // Добавить всем сотрудникам 1 год опыта .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                .map(e -> e.withJobHistory(changeQaToQa(e.getJobHistory()))) // Заменить все qa на QA
                .getList();
        List<Employee> expectedResult = Arrays.asList(
                new Employee(new Person("John", "Galt", 30),
                        Arrays.asList(
                                new JobHistoryEntry(3, "dev", "epam"),
                                new JobHistoryEntry(2, "dev", "google")
                        )),
                new Employee(new Person("John", "Doe", 40),
                        Arrays.asList(
                                new JobHistoryEntry(4, "QA", "yandex"),
                                new JobHistoryEntry(2, "QA", "epam"),
                                new JobHistoryEntry(2, "dev", "abc")
                        )),
                new Employee(new Person("John", "White", 50),
                        Collections.singletonList(
                                new JobHistoryEntry(6, "QA", "epam")
                        ))
        );
        assertEquals(expectedResult, mappedEmployees);
    }

    private List<JobHistoryEntry> changeQaToQa(List<JobHistoryEntry> jh) {
        return new MapHelper<>(jh).map(e -> e.getPosition().equals("qa") ? e.withPosition("QA") : e).getList();
    }

    private List<JobHistoryEntry> addOneYear(List<JobHistoryEntry> jh) {
        return new MapHelper<>(jh).map(e -> e.withDuration(e.getDuration() + 1)).getList();
    }

    private static class LazyMapHelper<T, R> {
        private final List<T> list;
        private final Function<T, R> function;

        public LazyMapHelper(List<T> list, Function<T, R> function) {
            this.list = list;
            this.function = function;
        }

        public static <T> LazyMapHelper<T, T> from(List<T> list) {
            return new LazyMapHelper<>(list, Function.identity());
        }

        public List<R> force() {
            return new MapHelper<T>(list).map(function).getList();
        }

        public <R2> LazyMapHelper<T, R2> map(Function<R, R2> f) {
            return new LazyMapHelper<T, R2>(list, function.andThen(f));
        }
    }

    @Test
    public void lazyMapping() {
        List<Employee> employees = Arrays.asList(
                new Employee(
                        new Person("a", "Galt", 30),
                        Arrays.asList(
                                new JobHistoryEntry(2, "dev", "epam"),
                                new JobHistoryEntry(1, "dev", "google")
                        )),
                new Employee(
                        new Person("b", "Doe", 40),
                        Arrays.asList(
                                new JobHistoryEntry(3, "qa", "yandex"),
                                new JobHistoryEntry(1, "qa", "epam"),
                                new JobHistoryEntry(1, "dev", "abc")
                        )),
                new Employee(
                        new Person("c", "White", 50),
                        Collections.singletonList(
                                new JobHistoryEntry(5, "qa", "epam")
                        ))
        );
        List<Employee> mappedEmployees = LazyMapHelper.from(employees)
                .map(e -> e.withPerson(e.getPerson().withFirstName("John"))) // Изменить имя всех сотрудников на John .map(e -> e.withPerson(e.getPerson().withFirstName("John")))
                .map(e -> e.withJobHistory((addOneYear(e.getJobHistory())))) // Добавить всем сотрудникам 1 год опыта .map(e -> e.withJobHistory(addOneYear(e.getJobHistory())))
                .map(e -> e.withJobHistory(changeQaToQa(e.getJobHistory())))  // Заменить все qu на QA
                .force();
        List<Employee> expectedResult = Arrays.asList(
                new Employee(new Person("John", "Galt", 30),
                        Arrays.asList(
                                new JobHistoryEntry(3, "dev", "epam"),
                                new JobHistoryEntry(2, "dev", "google")
                        )),
                new Employee(new Person("John", "Doe", 40),
                        Arrays.asList(
                                new JobHistoryEntry(4, "QA", "yandex"),
                                new JobHistoryEntry(2, "QA", "epam"),
                                new JobHistoryEntry(2, "dev", "abc")
                        )),
                new Employee(new Person("John", "White", 50),
                        Collections.singletonList(
                                new JobHistoryEntry(6, "QA", "epam")
                        ))
        );
        assertEquals(expectedResult, mappedEmployees);
    }

    // * LazyFlatMapHelper: но это неточно :D
    private static class LazyFlatMapHelper<T, R> {
        private final List<T> list;
        private final BiConsumer<List<T>, Consumer<R>> biCons;

        public LazyFlatMapHelper(List<T> list, BiConsumer<List<T>, Consumer<R>> listConsumer) {
            this.list = list;
            this.biCons = listConsumer;
        }

        public static <T> LazyFlatMapHelper<T, T> from(List<T> list) {
            return new LazyFlatMapHelper<>(list, (lst, consumer) -> lst.forEach(elem -> consumer.accept(elem)));
        }

        public List<R> force() {
            List<R> result = new ArrayList<>(list.size());
            biCons.accept(list, el -> result.add(el));
            return result;
        }

        public <R2> LazyFlatMapHelper<T, R2> flatMap(Function<R, List<R2>> f) {
            return new LazyFlatMapHelper<>(list, (lst, consumer) -> biCons.accept(lst, el -> f.apply(el)));
        }
    }
}
