package optional;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

// объект-контейнер, который может содержать null-value или non-null value
public class Optional<T> {
    // пустой Optional (с null-value)
    private static final Optional<?> EMPTY = new Optional<>();
    // То, что прячем в контейнере, может быть null, а может содержать значение
    private final T value;

    // Конструктор, создает Optional с null-значением
    private Optional() {
        value = null;
    }

    // Конструктор, создает Optional с ненулевым value, проверяет на null параметр
    private Optional(T value) {
        this.value = Objects.requireNonNull(value);
    }

    // Возвращает Optional с non-null значением value
    public static <T> Optional<T> of(T value) {
        return new Optional<>(value);
    }

    // Возвращает пустой Optional
    public static <T> Optional<T> empty() {
        return (Optional<T>) EMPTY;
    }

    // Возвращает Optional с ненулевым значением, если value != null
    // Возвращает Optional с нулевым значением, если value == null
    public static <T> Optional<T> ofNullable(T value) {
        return value == null ? empty() : of(value);
    }

    // Проверяет, value на неравенство null
    public boolean isPresent() {
        return value != null;
    }

    // Если значение non-null, с ним проводятся действия
    public void ifPresent(Consumer<? super T> action) {
        if (isPresent()) {
            action.accept(value);
        }
    }

    // Возвращает value, если оно non-null,
    // в противном случае кидает NoSuchElementException
    public T get() {
        if (!isPresent()) {
            throw new NoSuchElementException("No value present");
        }
        return value;
    }

    // Возвращает non-null value.
    // Если value == null, возвращает other
    public T orElse(T other) {
        return isPresent() ? value : other;
    }

    // Возвращает non-null value.
    // Если value == null, возвращает get supplier-а
    public T orElseGet(Supplier<? extends T> supplier) {
        return isPresent() ? value : supplier.get();
    }

    // Возвращает non-null value.
    // Если value == null, бросает исключение с помощью get у exceptionSupplier-а
    public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
        if (isPresent()) {
            return value;
        }
        throw exceptionSupplier.get();
    }

    // Если значение non-null и удовлетворяет предикату, то возвращает его.
    // В противном случае возвращается пустой Optional
    public Optional<T> filter(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isPresent()) {
            return predicate.test(value) ? this : empty();
        } else {
            return empty();
        }
    }

    // Если значение non-null, оно обрабатывается функцией,
    // если ее результат non-null, то возвращается Optional с этим результатом.
    // В противном случае возвращается пустой Optional
    public <R> Optional<R> map(Function<? super T, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        if (isPresent()) {
            return ofNullable(mapper.apply(value));
        } else {
            return empty();
        }
    }

    // Если значение non-null, оно обрабатывается функцией,
    // если ее результат non-null, то возвращается Optional с этим результатом.
    // В противном случае возвращается пустой Optional
    public <R> Optional<R> flatMap(Function<? super T, Optional<R>> mapper) {
        Objects.requireNonNull(mapper);
        if (isPresent()) {
            return Objects.requireNonNull(mapper.apply(value));
        } else {
            return empty();
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Optional<?> optional = (Optional<?>) o;
        return Objects.equals(value, optional.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return value != null
                ? String.format("Optional[%s]", value)
                : "Optional.empty";
    }
}
