package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class IOThreadScheduler implements Scheduler {
    private final ExecutorService executor = Executors.newCachedThreadPool();
    public void execute(Runnable task) {
        executor.submit(task);
    }
}
