package data.raw;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Employee {

    private final Person person;
    private final List<JobHistoryEntry> jobHistory;

    public Employee(Person person, List<JobHistoryEntry> jobHistory) {
        this.person = person;
        this.jobHistory = jobHistory;
    }

    public Employee withPerson(Person person) {
        return new Employee(person, jobHistory);
    }

    public Employee withJobHistory(List<JobHistoryEntry> entries) {
        return new Employee(person, entries);
    }

    public Person getPerson() {
        return person;
    }

    public List<JobHistoryEntry> getJobHistory() {
        return new ArrayList<>(jobHistory);
    }

    @Override
    public String toString() {
        return "TypedEmployee{ person=" + person + ", jobHistory=" + jobHistory + "}";
    }

    @Override
    public boolean equals(Object o) {
        return this == o
            || o != null
            && getClass() == o.getClass()
            && Objects.equals(person, ((Employee) o).person)
            && Objects.equals(jobHistory, ((Employee) o).jobHistory);
    }

    @Override
    public int hashCode() {
        return Objects.hash(person, jobHistory);
    }
}
