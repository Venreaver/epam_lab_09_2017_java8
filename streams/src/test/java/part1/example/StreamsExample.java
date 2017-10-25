package part1.example;

import data.Employee;
import data.JobHistoryEntry;
import data.Person;
import org.junit.Test;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.*;
import static org.junit.Assert.assertEquals;

public class StreamsExample {

    @Test
    public void checkJohnsLastNames() {
        List<String> johnsLastNames = getEmployees().stream()
                .map(Employee::getPerson)
                .filter(e -> e.getFirstName().equals("John"))
                .map(Person::getLastName)
                .distinct()         // интермидиат операции
                .collect(toList()); // терминальные операции - типа форсим) - только одна в конце
        // .toArray(String[]:new) = .toArray(size -> new String[size])
        assertEquals(Collections.singletonList("Galt"), johnsLastNames);
    }

    @Test
    public void operations() {
        Optional<JobHistoryEntry> jobHistoryEntry =
                getEmployees().stream()
                        .filter(e -> e.getPerson().getFirstName().equals("John"))
                        .map(Employee::getJobHistory)
                        .flatMap(Collection::stream)
                        .peek(System.out::println) // перебрать все элементы и куда-то деть все, не рекомендуется пользоваться им
                        .distinct()
                        .sorted(comparing(JobHistoryEntry::getDuration))
                        .skip(1)          // long
                        .limit(10)        // long
                        .unordered()      // можно не сохранять порядок
                        .parallel()       // можно вычислять операцию параллельно (chunks)
                        .sequential()     // обратное parallel - последовательно все делать
                        .findAny();       // взять любой элемент
        //      Терминальные операции
        //      .allMatch(Predicate<T>)         // все совпадения, заканчивается, когда закончится стрим, либо false
        //      .anyMatch(Predicate<T>)         // boolean хотя бы один элемент
        //      .noneMatch(Predicate<T>)        // boolean ни одного подходящего
        //      .reduce(BinaryOperator<T>)      // нужно класть ассоциативные операции сюда
        //      .collect(Collector<T, A, R>)    //
        //      .count()
        //      .findAny()
        //      .findFirst()
        //      .forEach(Consumer<T>)
        //      .forEachOrdered(Consumer<>)
        //      .max()
        //      .min()
        //      .toArray(IntFunction<A[]>)
        //      .iterator()                     // если используем итератор, то поток надо закрыть с помощью close() или в try-with-res

        // Characteristic :
        // CONCURRENT   -- parallel() true || sequential() false
        // DISTINCT     -- где исходный ресурс гарантирует уникальность
        // IMMUTABLE    -- по дефолту true
        // NONNULL      --
        // ORDERED      --
        // SIZED        --
        // SORTED       --
        // SUBSIZED     --


        System.out.println(jobHistoryEntry);
    }

    @Test
    public void checkAgedJohnsExpiriences() {
        List<Employee> employees = getEmployees();

        // Every aged (>= 25) John has an odd "dev" job experience

        employees.stream()
                .filter(e -> e.getPerson().getFirstName().equals("John"))
                .filter(e -> e.getPerson().getAge() >= 25)
                .flatMap(e -> e.getJobHistory().stream())
                .filter(e -> e.getPosition().equals("dev"))
                .filter(e -> e.getDuration() % 2 != 0)
                .distinct()
//                 .sorted(Comparator.comparing(JobHistoryEntry::getDuration))
                .sorted(Comparator.comparingInt(JobHistoryEntry::getDuration))
                .forEachOrdered(System.out::println);
    }

    @Test
    public void getProfessionals() {
        Map<String, Set<Person>> positionIndex = getPositionIndex(getEmployees());

        for (Person person : positionIndex.get("dev")) {
            System.out.println(person);
        }

        for (Person person : positionIndex.get("QA")) {
            System.out.println(person);
        }

        positionIndex.get("BA").forEach(System.out::println);
    }

    private static Stream<PersonPositionPair> employeeToPairs(Employee employee) {
        return employee.getJobHistory()
                .stream()
                .map(JobHistoryEntry::getPosition)
                .map(p -> new PersonPositionPair(employee.getPerson(), p));
    }

    // [ (John, [dev, QA]), (Bob, [QA, QA])] -> [dev -> [John], QA -> [John, Bob]]
    // [ (John, dev), (John, QA), (Bob, QA), (Bob, QA)] -> [dev -> [John], QA -> [John, Bob]]
    private Map<String, Set<Person>> getPositionIndex(List<Employee> employees) {
        Stream<PersonPositionPair> personPositionPairStream = employees.stream().flatMap(StreamsExample::employeeToPairs);

        // 3 РЕАЛИЗАЦИИ

        // Reduce with seed
        // у reduce 3 параметра тут: 1 - куда все складывается, 2 - как элементы добавляются в коллекцию
        // 3 параметр - это такая функция, которая может склеить две коллекции первого типа, нужно для того,
        // чтобы можно было распараллелить вычисления
        // reduce - частный случай collect
        personPositionPairStream
                .reduce(Collections.emptyMap(), StreamsExample::addToMap, StreamsExample::combineMaps);

//        return personPositionPairStream
//                .collect(
//                        () -> new HashMap<>(),
//                        (m, p) -> {
//                            final Set<Person> set = m.computeIfAbsent(p.getPosition(), (k) -> new HashSet<>());
//                            set.add(p.getPerson());
//                        },
//                        (m1, m2) -> {
//                            for (Map.Entry<String, Set<Person>> entry : m2.entrySet()) {
//                                Set<Person> set = m1.computeIfAbsent(entry.getKey(), (k) -> new HashSet<>());
//                                set.addAll(entry.getValue());
//                            }
//                        });
        // 1 парам - то, что будет key в мапе position, то есть по нему группируется
        // 2 парам - маппер, который достает Person в паре и кладет в Set (маппит кучу персонов в сет), маппер делает pair->person, а результат кладет в set
        return personPositionPairStream.collect(
                Collectors.groupingBy(PersonPositionPair::getPosition, mapping(PersonPositionPair::getPerson, toSet())));

        // группирует по person (это будет ключ в мапе) и кладет с помощью toList() PersonPositionPair'ы в List, который будет value в мапе
        // personPositionPairStream.collect(groupingBy(PersonPositionPair::getPerson, toList()))
    }

    private static Map<String, Set<Person>> combineMaps(Map<String, Set<Person>> u1, Map<String, Set<Person>> u2) {
        Map<String, Set<Person>> result = new HashMap<>(u1);
        u2.forEach((key, value) -> result.merge(key, new HashSet<>(value), (old, ne) -> {
            old.addAll(value);
            return old;
        }));
        return result;
//      Старое решение:
//        final HashMap<String, Set<Person>> result = new HashMap<>();
//        result.putAll(u1);
//        for (Map.Entry<String, Set<Person>> entry : u2.entrySet()) {
//            Set<Person> set = result.computeIfAbsent(entry.getKey(), (k) -> new HashSet<>());
//            set.addAll(entry.getValue());
//        }
//        return result;
    }

    private static Map<String, Set<Person>> addToMap(Map<String, Set<Person>> origin, PersonPositionPair pair) {
        Map<String, Set<Person>> result = new HashMap<>(origin);
        result.merge(pair.getPosition(), new HashSet<>(Collections.singleton(pair.getPerson())), (oldValue, newValue) -> {
            oldValue.add(pair.getPerson());
            return oldValue;
        });
        return result;
//        Старое решение:
//        final HashMap<String, Set<Person>> result = new HashMap<>();
//        result.putAll(origin);
//        Set<Person> set = result.computeIfAbsent(pair.getPosition(), (k) -> new HashSet<>());
//        set.add(pair.getPerson());
//        return result;

//        Для понимания типа:
//        Map<String, Integer> someMap = new HashMap<>();
//        someMap.put("1", 1);
//        someMap.put("2", 2);
//        someMap.put("3", 3);
//        someMap.put("4", 4);
//
//
//        someMap.put("2", 10);
//        someMap.merge("2", 10, Integer::sum);
    }


    @Test
    public void getTheCoolestOne() {
        final Map<String, Person> coolestByPosition = getCoolestByPosition(getEmployees());

        coolestByPosition.forEach((position, person) -> System.out.println(position + " -> " + person));
    }

    private static class PersonPositionDuration {
        private final Person person;
        private final String position;
        private final int duration;

        public PersonPositionDuration(Person person, String position, int duration) {
            this.person = person;
            this.position = position;
            this.duration = duration;
        }

        public Person getPerson() {
            return person;
        }

        public String getPosition() {
            return position;
        }

        public int getDuration() {
            return duration;
        }
    }

    private Map<String, Person> getCoolestByPosition(List<Employee> employees) {
        final Stream<PersonPositionDuration> personPositionDurationStream = employees.stream()
                .flatMap(
                        e -> e.getJobHistory()
                                .stream()
                                .map(j -> new PersonPositionDuration(e.getPerson(), j.getPosition(), j.getDuration())));
//        final Map<String, PersonPositionDuration> collect = personPositionDurationStream
//                .collect(toMap(
//                        PersonPositionDuration::getPosition,
//                        Function.identity(),
//                        (p1, p2) -> p1.getDuration() > p2.getDuration() ? p1 : p2));

//        Collectors.collectingAndThen()
//        Collectors.toMap()
        return personPositionDurationStream
                .collect(groupingBy(
                        PersonPositionDuration::getPosition,
                        // collectingAndThen ринимает коллектор и лямбду, коллектор отрабатывает, потом отрабаотывается лямбда
                        collectingAndThen(
                                // сначала найдем максимального, а потом лямбдой вытащим человека с макс продолжит работы
                                // p.get() - тянем optional
                                maxBy(comparing(PersonPositionDuration::getDuration)), p -> p.get().getPerson())));
    }

    @Test
    public void intStream_bad1() {
        int sumDuration =
                getEmployees().stream()
                        .flatMap(
                                employee -> employee.getJobHistory().stream()
                        )
                        .collect(mapping(JobHistoryEntry::getDuration, Collectors.reducing(0, (a, b) -> a + b)));
        System.out.println("sum: " + sumDuration);
    }

    @Test
    public void intStream_bad2() {
        int sumDuration =
                getEmployees().stream()
                        .flatMap(
                                employee -> employee.getJobHistory().stream()
                        )
                        .map(JobHistoryEntry::getDuration)
                        .collect(Collectors.reducing(0, (a, b) -> a + b));

        System.out.println("sum: " + sumDuration);
    }

    @Test
    public void intStream_bad3() {
        int sumDuration =
                getEmployees().stream()
                        .flatMap(
                                employee -> employee.getJobHistory().stream()
                        )
                        .collect(Collectors.summingInt(JobHistoryEntry::getDuration));

        System.out.println("sum: " + sumDuration);
    }

    @Test
    public void intStream() {
        final int sumDuration =
                getEmployees().stream()
                        .flatMap(employee -> employee.getJobHistory().stream())
                        .mapToInt(JobHistoryEntry::getDuration)
                        .sum();

        System.out.println("sum: " + sumDuration);
    }

    @Test
    public void intStream_array() {
        final Employee[] employeesArray = getEmployees().toArray(new Employee[0]);
        //final Employee[] employeesArray = getEmployees().stream().toArray(Employee[]::new);
        final int sumDuration =
                Arrays.stream(employeesArray)
                        .flatMap(employee -> employee.getJobHistory().stream())
                        .mapToInt(JobHistoryEntry::getDuration)
                        .sum();

        System.out.println("sum: " + sumDuration);
    }

    private List<Employee> getEmployees() {
        return Arrays.asList(
                new Employee(
                        new Person("John", "Galt", 20),
                        Arrays.asList(
                                new JobHistoryEntry(3, "dev", "epam"),
                                new JobHistoryEntry(2, "dev", "google")
                        )),
                new Employee(
                        new Person("John", "Doe", 21),
                        Arrays.asList(
                                new JobHistoryEntry(4, "BA", "yandex"),
                                new JobHistoryEntry(2, "QA", "epam"),
                                new JobHistoryEntry(2, "dev", "abc")
                        )),
                new Employee(
                        new Person("John", "White", 22),
                        Collections.singletonList(
                                new JobHistoryEntry(6, "QA", "epam")
                        )),
                new Employee(
                        new Person("John", "Galt", 23),
                        Arrays.asList(
                                new JobHistoryEntry(3, "dev", "epam"),
                                new JobHistoryEntry(2, "dev", "google")
                        )),
                new Employee(
                        new Person("John", "Doe", 24),
                        Arrays.asList(
                                new JobHistoryEntry(4, "QA", "yandex"),
                                new JobHistoryEntry(2, "BA", "epam"),
                                new JobHistoryEntry(2, "dev", "abc")
                        )),
                new Employee(
                        new Person("John", "White", 25),
                        Collections.singletonList(
                                new JobHistoryEntry(6, "QA", "epam")
                        )),
                new Employee(
                        new Person("John", "Galt", 26),
                        Arrays.asList(
                                new JobHistoryEntry(3, "dev", "epam"),
                                new JobHistoryEntry(1, "dev", "google")
                        )),
                new Employee(
                        new Person("Bob", "Doe", 27),
                        Arrays.asList(
                                new JobHistoryEntry(4, "QA", "yandex"),
                                new JobHistoryEntry(2, "QA", "epam"),
                                new JobHistoryEntry(2, "dev", "abc")
                        )),
                new Employee(
                        new Person("John", "White", 28),
                        Collections.singletonList(
                                new JobHistoryEntry(6, "BA", "epam")
                        )),
                new Employee(
                        new Person("John", "Galt", 29),
                        Arrays.asList(
                                new JobHistoryEntry(3, "dev", "epam"),
                                new JobHistoryEntry(1, "dev", "google")
                        )),
                new Employee(
                        new Person("John", "Doe", 30),
                        Arrays.asList(
                                new JobHistoryEntry(4, "QA", "yandex"),
                                new JobHistoryEntry(2, "QA", "epam"),
                                new JobHistoryEntry(5, "dev", "abc")
                        )),
                new Employee(
                        new Person("Bob", "White", 31),
                        Collections.singletonList(
                                new JobHistoryEntry(6, "QA", "epam")
                        ))
        );
    }

    private static class PersonPositionPair {
        private final Person person;
        private final String position;

        public PersonPositionPair(Person person, String position) {
            this.person = person;
            this.position = position;
        }

        public Person getPerson() {
            return person;
        }

        public String getPosition() {
            return position;
        }
    }
}
