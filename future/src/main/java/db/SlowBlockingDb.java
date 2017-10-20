package db;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class SlowBlockingDb<T> implements Closeable {

    private final SlowFutureDb<T> slowFutureDb;

    public SlowBlockingDb(Map<String, T> values) {
        slowFutureDb = new SlowFutureDb<>(values);
    }

    // получаем элемент типа T в ответ на get-запросы
    public T get(String key) throws ExecutionException, InterruptedException {
        return slowFutureDb.get(key).get();
    }

    // получаем базу, которая возвращает типизированный Future в ответ на get-запросы
    public SlowFutureDb<T> getFutureDb() {
        return slowFutureDb;
    }

    // закрываем все
    @Override
    public void close() throws IOException {
        slowFutureDb.close();
    }
}
