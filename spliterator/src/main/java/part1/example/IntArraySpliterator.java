package part1.example;

import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.IntConsumer;

public class IntArraySpliterator extends Spliterators.AbstractIntSpliterator {
    // источник данных
    private final int[] array;
    // первая позиция (включая ее)
    private int startInclusive;
    // последняя позиция (исключая ее)
    private final int endExclusive;

    // создаем сплитератор из массива [0, array.length)
    public IntArraySpliterator(int[] array) {
        this(array, 0, array.length);
    }

    // создаем сплитератор из массива + передаем все базовому конструктору AbstractIntSpliterator
    // характеристики через |, т.к. это маска
    private IntArraySpliterator(int[] array, int startInclusive, int endExclusive) {
        super(endExclusive - startInclusive,
                Spliterator.IMMUTABLE
                        | Spliterator.ORDERED
                        | Spliterator.SIZED
                        | Spliterator.SUBSIZED
                        | Spliterator.NONNULL);
        this.array = array;
        this.startInclusive = startInclusive;
        this.endExclusive = endExclusive;
    }

    // Если начальная позиция меньше конечной,
    // то вытаскиваем значение из массива по нач позиции,
    // инкрементим позицию, обрабатываем значение консьюмером
    // true - если получилось, false - если нечего обрабатывтать
    @Override
    public boolean tryAdvance(IntConsumer action) {
        if (startInclusive < endExclusive) {
            action.accept(array[startInclusive++]);
            return true;
        }
        return false;
    }

    // для всех оставшихся элементов обрабатываем значение консьюмером
    @Override
    public void forEachRemaining(IntConsumer action) {
        for (int i = startInclusive; i < endExclusive; i++) {
            action.accept(array[i]);
        }
        startInclusive = endExclusive;
    }

    // пытаемся поделиться, если не получится, то вернет null,
    // если получится, то вернет новый сплитератор
    // в новый сплитератор лучше помещать начальную часть (напр., бесконеч поток)
    @Override
    public OfInt trySplit() {
        int length = endExclusive - startInclusive;
        if (length < 2) {
            return null;
        }
        int mid = startInclusive + length / 2;
        IntArraySpliterator result = new IntArraySpliterator(array, startInclusive, mid);
        startInclusive = mid;
        return result;
    }

    // возвращает маску с характеристиками
    @Override
    public int characteristics() {
        return super.characteristics();
        //return IMMUTABLE | ORDERED | SIZED | SUBSIZED | NONNULL;
    }

    // проверяем, есть ли заданные характеристики у нашего сплитератора
    @Override
    public boolean hasCharacteristics(int characteristics) {
        return (characteristics() & characteristics) == characteristics;
    }

    // возвращает приблизительное кол-во эл-ов,
    // которые есть для обработки
    @Override
    public long estimateSize() {
        return endExclusive - startInclusive;
    }

    // возвращаем точное кол-во элементов, если возможно
    // в противном случае -1L
    @Override
    public long getExactSizeIfKnown() {
        return (characteristics() & SIZED) == 0 ? -1L : estimateSize();
    }
}