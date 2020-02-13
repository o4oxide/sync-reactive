package com.o4oxide.syncreactive.core.tasks;

import java.util.concurrent.CountedCompleter;
import java.util.function.Consumer;

public abstract class Task<R> extends CountedCompleter<R> {

    protected final Consumer<Runnable> contextSwitcher;

    protected Task(Consumer<Runnable> contextSwitcher) {
        this.contextSwitcher = contextSwitcher;
    }

    protected void runInContextIfAvailable(Runnable runnable) {
        if (contextSwitcher == null) {
            runnable.run();
        } else {
            runInContext(runnable);
        }
    }

    private void runInContext(Runnable runnable) {
        final long forkJoinThreadId = Thread.currentThread().getId();
        contextSwitcher.accept(() -> {
            if (Thread.currentThread().getId() == forkJoinThreadId) {
                throw new IllegalThreadStateException("Context has not switched");
            }
            runnable.run();
        });
    }
}
