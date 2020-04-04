package com.o4oxide.syncreactive.core.tasks;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountedCompleter;
import java.util.function.Consumer;
import java.util.function.Function;

public class AsyncTask<T, R> extends Task<R> {

    private final Function<T, R> blockingFunction;
    private final T input;
    private final CompletableFuture<R> asyncFuture;
    private R result;

    public AsyncTask(Function<T, R> blockingFunction, T input, CompletableFuture<R> asyncFuture, Consumer<Runnable> contextSwitcher){
        super(contextSwitcher);
        this.blockingFunction = blockingFunction;
        this.input = input;
        this.asyncFuture = asyncFuture;
    }

    @Override
    public void compute() {
        result = blockingFunction.apply(input);
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
