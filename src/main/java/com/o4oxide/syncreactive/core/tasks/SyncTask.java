package com.o4oxide.syncreactive.core.tasks;

import com.o4oxide.syncreactive.FunctionParam;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountedCompleter;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;

public class SyncTask<T, R> extends Task<R> {

    private final FunctionParam<T, CompletableFuture<R>> nonBlockingFunctionParam;
    private final ForkJoinPool pool;
    private R result;

    public SyncTask(FunctionParam<T, CompletableFuture<R>> nonBlockingFunctionParam, Consumer<Runnable> contextSwitcher, ForkJoinPool pool) {
        super(contextSwitcher);
        this.nonBlockingFunctionParam = nonBlockingFunctionParam;
        this.pool = pool;
    }

    @Override
    public void compute() {
        runInContextIfAvailable(this::runAsyncFunction);
    }

    @Override
    protected void setRawResult(R r) {
        this.result = r;
    }

    @Override
    public R getRawResult() {
        return result;
    }

    private void runAsyncFunction() {
        this.nonBlockingFunctionParam.execute()
                .whenComplete((res, ex) -> this.pool.execute(new ContextCompleter<>(res, ex, this)));
    }

    private static final class ContextCompleter<R> extends CountedCompleter<Void> {

        private final R result;
        private final Throwable ex;
        private final CountedCompleter<R> caller;

        private ContextCompleter(R result, Throwable ex, CountedCompleter<R> caller) {
            this.result = result;
            this.ex = ex;
            this.caller = caller;
        }

        @Override
        public void compute() {
            if (ex == null) {
                caller.complete(result);
            } else {
                caller.completeExceptionally(ex);
            }
            complete(null);
        }

    }
}
