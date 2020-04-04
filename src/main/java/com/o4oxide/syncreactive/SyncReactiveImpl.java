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
    public <T, R> CompletableFuture<R> async(FunctionParam<T, R> blockingFunctionParam) {
        CompletableFuture<R> asyncFuture = new CompletableFuture<>();
        pool.execute(new AsyncTask<>(blockingFunctionParam, asyncFuture, completionContextSwitcher));
        return asyncFuture;
    }

    @Override
    public <T, R> R sync(FunctionParam<T, CompletableFuture<R>> nonBlockingFunctionParam) {
        return pool.invoke(new SyncTask<>(nonBlockingFunctionParam, invocationContextSwitcher, this.pool));
    }
}
