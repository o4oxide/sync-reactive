package com.o4oxide.syncreactive;

import com.o4oxide.syncreactive.core.tasks.AsyncTask;
import com.o4oxide.syncreactive.core.tasks.SyncTask;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;

class SyncReactiveImpl implements SyncReactive {

    private final ForkJoinPool pool;
    private Consumer<Runnable> completionContextSwitcher;
    private Consumer<Runnable> invocationContextSwitcher;


    SyncReactiveImpl() {
        this.pool = new ForkJoinPool(
                            Runtime.getRuntime().availableProcessors(),
                            ForkJoinPool.defaultForkJoinWorkerThreadFactory,
                            null,
                            true)
        ;
    }

    SyncReactiveImpl(Consumer<Runnable> contextSwitcher) {
        this();
        this.completionContextSwitcher = contextSwitcher;
        this.invocationContextSwitcher = contextSwitcher;
    }

    SyncReactiveImpl(Consumer<Runnable> completionContextSwitcher, Consumer<Runnable> invocationContextSwitcher) {
        this();
        this.completionContextSwitcher = completionContextSwitcher;
        this.invocationContextSwitcher = invocationContextSwitcher;
    }

    @Override
    public <T, R> CompletableFuture<R> async(Function<T, R> blockingFunction, T input) {
        CompletableFuture<R> asyncFuture = new CompletableFuture<>();
        pool.execute(new AsyncTask<>(blockingFunction, input, asyncFuture, completionContextSwitcher));
        return asyncFuture;
    }

    @Override
    public <T, R> R sync(Function<T, CompletableFuture<R>> nonBlockingFunction, T input) {
        return pool.invoke(new SyncTask<>(nonBlockingFunction, input, invocationContextSwitcher, this.pool));
    }
}
