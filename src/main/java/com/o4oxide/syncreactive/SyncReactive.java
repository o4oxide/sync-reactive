package com.o4oxide.syncreactive;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.function.Consumer;
import java.util.function.Function;

public interface SyncReactive {
    <T, R> CompletableFuture<R> async(Function<T, R> blockingFunction, T input);
    <T, R> R sync(Function<T, CompletableFuture<R>> nonBlockingFunction, T input);

    static SyncReactive syncReactive() {
        return new SyncReactiveImpl();
    }

    static SyncReactive syncReactive(Consumer<Runnable> contextSwitcher) {
        return new SyncReactiveImpl(contextSwitcher);
    }

    static SyncReactive syncReactive(Consumer<Runnable> asyncContextSwitcher, Consumer<Runnable> syncContextSwitcher) {
        return new SyncReactiveImpl(asyncContextSwitcher, syncContextSwitcher);
    }

    static SyncReactive syncReactive(ForkJoinPool pool) {
        return new SyncReactiveImpl(pool);
    }

    static SyncReactive syncReactive(ForkJoinPool pool, Consumer<Runnable> contextSwitcher) {
        return new SyncReactiveImpl(pool, contextSwitcher);
    }

    static SyncReactive syncReactive(ForkJoinPool pool, Consumer<Runnable> asyncContextSwitcher, Consumer<Runnable> syncContextSwitcher) {
        return new SyncReactiveImpl(pool, asyncContextSwitcher, syncContextSwitcher);
    }
}
