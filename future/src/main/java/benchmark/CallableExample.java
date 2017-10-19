package benchmark;

import java.util.List;
import java.util.concurrent.*;

public class CallableExample {

    public static void main(String[] args) {
        Callable<Integer> callable = () -> {
            TimeUnit.SECONDS.sleep(5);
            return 42;
        };
        Runnable runnable = () -> System.out.println("24");

        ExecutorService service = Executors.newFixedThreadPool(10);
        service.submit(runnable);
        Future<Integer> future = service.submit(callable);

        try {
            System.out.println("Before");
            System.out.println(future.get());
            System.out.println("After");
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }


        List<Runnable> runnables = service.shutdownNow();
    }
}
