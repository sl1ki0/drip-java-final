package com.newsaggregator.service;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class RefreshScheduler {
    private ScheduledExecutorService executor;

    public void start(Runnable task, long intervalSeconds) {
        stop();
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "refresh-scheduler");
            thread.setDaemon(true);
            return thread;
        });
        executor.scheduleAtFixedRate(task, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
    }

    public boolean isRunning() {
        return executor != null && !executor.isShutdown();
    }

    public void stop() {
        if (executor != null) {
            executor.shutdownNow();
            executor = null;
        }
    }
}
