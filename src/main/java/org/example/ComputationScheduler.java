package org.example;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

class ComputationScheduler implements Scheduler {
    private final ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    public void execute(Runnable task) {
        executor.submit(task);
    }
}
