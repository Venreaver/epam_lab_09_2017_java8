package data.raw;

import data.typed.Employer;
import data.typed.Position;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public final class Generator {

    private Generator() {}

    private static String generateString() {
        String letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        int maxLength = 10;

        return IntStream.range(0, ThreadLocalRandom.current().nextInt(maxLength) + 1)
                        .mapToObj(letters::charAt)
                        .map(Object::toString)
                        .collect(Collectors.joining());
    }

    private static Person generatePerson() {
        return new Person(generateString(), generateString(), 18 + ThreadLocalRandom.current().nextInt(100));
    }

    private static String generateEmployer() {
        Employer[] employers = Employer.values();
        return employers[ThreadLocalRandom.current().nextInt(employers.length)].name();
    }

    private static String generatePosition() {
        Position[] positions = Position.values();
        return positions[ThreadLocalRandom.current().nextInt(positions.length)].name();
    }

    private static JobHistoryEntry generateJobHistoryEntry() {
        int duration = ThreadLocalRandom.current().nextInt(100) + 1;
        return new JobHistoryEntry(duration, generatePosition(), generateEmployer());
    }

    private static List<JobHistoryEntry> generateJobHistoryEntryList() {
        return Stream.generate(Generator::generateJobHistoryEntry)
                     .limit(ThreadLocalRandom.current().nextInt(6))
                     .collect(toList());
    }

    private static Employee generateEmployee() {
        return new Employee(generatePerson(), generateJobHistoryEntryList());
    }

    public static List<Employee> generateEmployeeList(int count) {
        return Stream.generate(Generator::generateEmployee)
                     .limit(count)
                     .collect(toList());
    }
}
