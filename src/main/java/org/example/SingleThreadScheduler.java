package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SingleThreadScheduler implements Scheduler {
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    public void execute(Runnable task) {
        executor.submit(task);
    }
}
