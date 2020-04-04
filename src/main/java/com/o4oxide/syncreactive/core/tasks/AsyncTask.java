package com.o4oxide.syncreactive.core.tasks;

import com.o4oxide.syncreactive.FunctionInstance;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountedCompleter;
import java.util.function.Consumer;

public class AsyncTask<T, R> extends Task<R> {

    private final FunctionInstance<T, R> blockingFunction;
    private final CompletableFuture<R> asyncFuture;
    private R result;

    public AsyncTask(FunctionInstance<T, R> blockingFunction, CompletableFuture<R> asyncFuture, Consumer<Runnable> contextSwitcher){
        super(contextSwitcher);
        this.blockingFunction = blockingFunction;
        this.asyncFuture = asyncFuture;
    }

    @Override
    public void compute() {
        result = blockingFunction.execute();
        tryComplete();
    }

    @Override
    public void onCompletion(CountedCompleter<?> caller) {
        runInContextIfAvailable(() -> asyncFuture.complete(result));
    }

    @Override
    public boolean onExceptionalCompletion(Throwable ex, CountedCompleter<?> caller) {
        this.result = null;
        runInContextIfAvailable(() -> asyncFuture.completeExceptionally(ex));
        return true;
    }

    @Override
    public R getRawResult() {
        return result;
    }
}
