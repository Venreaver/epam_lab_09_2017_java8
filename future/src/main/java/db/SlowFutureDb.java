package db;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.Future;

public class SlowFutureDb<T> implements Closeable {

    private final SlowCompletableFutureDb<T> slowCompletableFutureDb;

    public SlowFutureDb(Map<String, T> values) {
        slowCompletableFutureDb = new SlowCompletableFutureDb<>(values);
    }

    // получаем типизированный Future в ответ на get-запросы
    public Future<T> get(String key) {
        return slowCompletableFutureDb.get(key);
    }

    // получаем базу, которая возвращает типизированный CompletableFuture в ответ на get-запросы
    public SlowCompletableFutureDb<T> getCompletableFutureDb() {
        return slowCompletableFutureDb;
    }

    // закрываем все
    @Override
    public void close() throws IOException {
        slowCompletableFutureDb.close();
    }
}
