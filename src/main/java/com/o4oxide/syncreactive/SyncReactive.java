package com.o4oxide.syncreactive;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;

public interface SyncReactive {
    <T, R> CompletableFuture<R> async(Function<T, R> blockingFunction, T input);
    <T, R> R sync(Function<T, CompletableFuture<R>> nonBlockingFunction, T input);

    static SyncReactive create() {
        return new SyncReactiveImpl();
    }

    static SyncReactive create(Consumer<Runnable> contextSwitcher) {
        return new SyncReactiveImpl(contextSwitcher);
    }

    static SyncReactive create(Consumer<Runnable> asyncContextSwitcher, Consumer<Runnable> syncContextSwitcher) {
        return new SyncReactiveImpl(asyncContextSwitcher, syncContextSwitcher);
    }

    static SyncReactive create(ForkJoinPool pool) {
        return new SyncReactiveImpl(pool);
    }

    static SyncReactive create(ForkJoinPool pool, Consumer<Runnable> contextSwitcher) {
        return new SyncReactiveImpl(pool, contextSwitcher);
    }

    static SyncReactive create(ForkJoinPool pool, Consumer<Runnable> asyncContextSwitcher, Consumer<Runnable> syncContextSwitcher) {
        return new SyncReactiveImpl(pool, asyncContextSwitcher, syncContextSwitcher);
    }
}
