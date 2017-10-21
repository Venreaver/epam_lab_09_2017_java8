package part1.exercise;

import org.junit.Test;

import java.util.Optional;
import java.util.concurrent.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Methods {

    private ForkJoinPool customExecutor = ForkJoinPool.commonPool();

    private Integer slowTask() {
        try {
            TimeUnit.SECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ThreadLocalRandom.current().nextInt();
    }

    @Test
    public void create() {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        CompletableFuture<Integer> completedFuture = CompletableFuture.completedFuture(1);

        CompletableFuture<Integer> supplied = CompletableFuture.supplyAsync(this::slowTask);
        CompletableFuture<Integer> supplied2 = CompletableFuture.supplyAsync(this::slowTask, customExecutor);

        CompletableFuture<Void> runAsync = CompletableFuture.runAsync(this::slowTask);
        CompletableFuture<Void> runAsync2 = CompletableFuture.runAsync(this::slowTask, customExecutor);
        // new
        // supplyAsync
        // runAsync
        // completed
    }

    @Test
    public void write() {
        // complete (completed)
        CompletableFuture<Integer> future = new CompletableFuture<>();
        future.complete(1);

        CompletableFuture<Integer> future2 = new CompletableFuture<>();
        //new Thread(() -> future2.complete(slowTask())).start();
        ForkJoinPool.commonPool().submit(() -> {
            try {
                Integer result = slowTask();
                future2.complete(result);
            } catch (Exception e) {
                future2.completeExceptionally(e);
            }
        });

        CompletableFuture<Void> voidFuture = new CompletableFuture<>();
        ForkJoinPool.commonPool().submit(() -> {
            try {
                slowTask();
                voidFuture.complete(null);
            } catch (Exception e) {
                voidFuture.completeExceptionally(e);
            }
        });

        // completeExceptionally
    }

    @Test
    public void rewrite() throws ExecutionException, InterruptedException {
        // obtrudeValue
        // obtrudeException
        CompletableFuture<Integer> future = CompletableFuture.completedFuture(1);
        assertEquals(Integer.valueOf(1), future.get());

        future.obtrudeValue(2);
        assertEquals(Integer.valueOf(2), future.get());
    }

    @Test
    public void read() {
        CompletableFuture<Integer> future = CompletableFuture.completedFuture(1);
        CompletableFuture<Integer> future2 = new CompletableFuture<>();
        future2.completeExceptionally(new UnsupportedOperationException());
        // isDone
        assertTrue(future.isDone());
        assertTrue(future2.isDone());
        // get
        try {
            assertEquals(Integer.valueOf(1), future.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        // join
        assertEquals(Integer.valueOf(1), future.join());
        // getNow
        future.getNow(0);
        // getNumberOfDependents
    }

    @Test
    public void foreachMapFlatMap() {
        CompletableFuture<Integer> future = null;
        CompletableFuture<Integer> future1 = null;
        CompletableFuture<String> future2 = null;

        // forEach
        CompletableFuture<Void> thenAccept = future.thenAccept(System.out::println);
        CompletableFuture<Void> thenAcceptAsync = future.thenAcceptAsync(System.out::println);
        CompletableFuture<Void> thenAcceptAsync2 = future.thenAcceptAsync(System.out::println, customExecutor);
        CompletableFuture<Void> thenRun = future.thenRun(() -> System.out.println("Done"));
        // map
        CompletableFuture<String> thenApply = future.thenApply(Object::toString);
        // flatMap
        CompletableFuture<Integer> thenCompose = future.thenCompose(i -> CompletableFuture.completedFuture(i + 1));
        CompletableFuture<Integer> thenComposeAsync = future.thenComposeAsync(i -> CompletableFuture.completedFuture(i + 1));
        // *3
        // *Async

        CompletableFuture<Integer> sum = future.thenCompose(i -> future1.thenApply(i1 -> i + i1));
    }

    @Test
    public void allAnyOf() {
        CompletableFuture<Integer> future = null;
        CompletableFuture<Integer> future1 = null;
        CompletableFuture<String> future2 = null;

        // All of
        CompletableFuture<Void> allOf = CompletableFuture.allOf(future, future1, future2);
        allOf.thenRun(() -> {
            Integer i = future.join();
            String s = future2.join();
        });


        // Any of
        CompletableFuture<Object> anyOf = CompletableFuture.anyOf(future, future1);
        anyOf.thenRun(() -> {
            if (future.isDone()) {
                future.join();
            } else {
                future1.join();
            }
        });


        // any of
        // applyToEither
        CompletableFuture<Integer> applyToEither = future.applyToEither(future1, i -> i + 1);
        // acceptEither
        CompletableFuture<Void> acceptEither = future.acceptEither(future1, System.out::println);
        // runAfterEither
        CompletableFuture<Void> runAfterEither = future.runAfterEither(future1, () -> System.out.println("Done!"));

        // all of
        // thenCombine
        // runAfterBoth
        // thenAcceptBoth

    }

    @Test
    public void recover() {
        CompletableFuture<String> future = new CompletableFuture<>();
        // exceptionally
        CompletableFuture<Optional<String>> exceptionally =
                future.thenApply(Optional::of).exceptionally(e -> {
                    e.printStackTrace();
                    return Optional.empty();
                });

        // handle
        CompletableFuture<Optional<?>> handle =
                future.handle((s, e) -> {
                    if (e != null) {
                        e.printStackTrace();
                        return Optional.empty();
                    } else {
                        return Optional.of(s);
                    }
                });

    }

}
