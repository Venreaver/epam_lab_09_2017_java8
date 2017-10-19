package data.typed;

import data.raw.Person;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.ArrayList;
import java.util.List;

public class TypedEmployee {
    private final Person person;
    private final List<TypedJobHistoryEntry> jobHistoryEntries;

    public TypedEmployee(Person person, List<TypedJobHistoryEntry> jobHistoryEntries) {
        this.person = person;
        this.jobHistoryEntries = new ArrayList<>(jobHistoryEntries);
    }

    public Person getPerson() {
        return person;
    }

    public List<TypedJobHistoryEntry> getJobHistoryEntries() {
        return new ArrayList<>(jobHistoryEntries);
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("person", person)
                .append("jobHistoryEntries", jobHistoryEntries)
                .toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        TypedEmployee employee = (TypedEmployee) o;

        return new EqualsBuilder()
                .append(person, employee.person)
                .append(jobHistoryEntries, employee.jobHistoryEntries)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(person)
                .append(jobHistoryEntries)
                .toHashCode();
    }
}
