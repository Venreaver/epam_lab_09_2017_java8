package benchmark;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class ThreadExample {

    public static void main(String[] args) {
        String str = "Hello";
        System.out.println(str);
        method();

        Thread mainThread = Thread.currentThread();


        ArrayList<Integer> result = new ArrayList<>();

        Thread thread = new Thread(() -> {
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            for (int i = 0; i < 5; i++) {
                result.add(i);
            }
        });
        thread.start();

        try {
            thread.join();
            for (Integer integer : result) {
                System.out.println(integer);
            }
        } catch (InterruptedException e) {
            System.out.println("Main was interrupted");
        }
    }

    public static void method() {
        int a = 10;
        method2();
    }

    public static void method2() {
        double a = 10;
    }
}
