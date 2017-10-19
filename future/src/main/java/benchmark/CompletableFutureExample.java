package benchmark;

import java.util.Arrays;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class CompletableFutureExample {

    public static void main(String[] args) throws InterruptedException, ExecutionException {

//        IntStream range = IntStream.range(0, 500);
//        int[] ints = range.filter(value -> value < 100).toArray();
//        int[] ints1 = range.filter(value -> value > 100).toArray();
//        System.out.println(Arrays.toString(ints));
//        System.out.println(Arrays.toString(ints1));

//        simpleExample();
        acceptBoth();
    }

    private static void simpleExample() throws InterruptedException {
        ExecutorService service = Executors.newFixedThreadPool(10);
        CompletableFuture<Integer> completableFuture = CompletableFuture.supplyAsync(() -> {
            try {
                TimeUnit.SECONDS.sleep(3);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int sum = 0;
            for (int i = 0; i < 100; i++) {
                sum += i;
            }
            return sum;
        }, service).thenApply(integer -> integer + 100);


        completableFuture.thenAccept(System.out::println);
        CompletableFuture<String> stringCompletableFuture = completableFuture.thenApply(Object::toString);
        stringCompletableFuture.join();


//        try {
//            System.out.println(future.get());
//        } catch (InterruptedException | ExecutionException e) {
//            e.printStackTrace();
//        }
        for (int i = 0; i < 10; i++) {
            TimeUnit.SECONDS.sleep(1);
            System.out.println(i);
        }
        service.shutdown();
    }

    private static void acceptBoth() {
        CompletableFuture<Integer> completableFuture1 = CompletableFuture.supplyAsync(() -> 42);
        CompletableFuture<Integer> completableFuture2 = CompletableFuture.supplyAsync(() -> 58);
        CompletableFuture<Void> voidCompletableFuture = completableFuture1.thenAcceptBoth(completableFuture2, (integer, integer2) -> {
            System.out.println(integer + integer2);
        });

    }
}
