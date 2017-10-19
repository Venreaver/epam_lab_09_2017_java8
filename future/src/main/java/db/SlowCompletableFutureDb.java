package db;

import java.io.Closeable;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.*;

public class SlowCompletableFutureDb<T> implements DataStorage<String, T>, Closeable {

    private volatile Map<String, T> values;
    private final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
    private TimeUnit timeoutUnits;
    private int maxTimeout;

    public SlowCompletableFutureDb(Map<String, T> values, int maxTimeout, TimeUnit timeoutUnits) {
        this.values = new HashMap<>(values);
        this.maxTimeout = maxTimeout;
        this.timeoutUnits = timeoutUnits;
    }

    public SlowCompletableFutureDb(Map<String, T> values) {
        this(values, 100, TimeUnit.MILLISECONDS);
    }

    public CompletableFuture<T> get(String key) {
        T value = values.get(key);
        CompletableFuture<T> result = new CompletableFuture<>();

        int timeout = ThreadLocalRandom.current().nextInt(maxTimeout);

        scheduledExecutorService.schedule(() -> result.complete(value), timeout, timeoutUnits);
        return result;
    }

    public void setValues(Map<String, T> values) {
        this.values = new HashMap<>(values);
    }

    @Override
    public void close() throws IOException {
        try {
            scheduledExecutorService.shutdownNow();
            scheduledExecutorService.awaitTermination(5, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
