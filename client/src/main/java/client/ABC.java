package client;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ABC {
    private static final ExecutorService service = Executors.newCachedThreadPool();

    private static int mark = 1;
    private static final int loop = 5;

    public static void main(String[] args) {
        service.execute(() -> {
            try {
                for (int i = 0; i < loop; i++) {
                    synchronized (service) {
                        while (mark != 1) {
                            service.wait();
                        }
                        mark = 2;
                        System.out.print("A");
                        service.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        service.execute(() -> {
            try {
                for (int i = 0; i < loop; i++) {
                    synchronized (service) {
                        while (mark != 2) {
                            service.wait();
                        }
                        mark = 3;
                        System.out.print("B");
                        service.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        service.execute(() -> {
            try {
                for (int i = 0; i < loop; i++) {
                    synchronized (service) {
                        while (mark != 3) {
                            service.wait();
                        }
                        mark = 1;
                        System.out.print("C");
                        service.notifyAll();
                    }
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });

        service.shutdown();
    }
}