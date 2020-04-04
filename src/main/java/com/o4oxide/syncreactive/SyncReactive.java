package com.o4oxide.syncreactive;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface SyncReactive {
    <T, R> CompletableFuture<R> async(FunctionInstance<T, R> blockingFunction);
    <T, R> R sync(FunctionInstance<T, CompletableFuture<R>> nonBlockingFunction);

    static SyncReactive syncReactive() {
        return new SyncReactiveImpl();
    }

    static SyncReactive syncReactive(Consumer<Runnable> contextSwitcher) {
        return new SyncReactiveImpl(contextSwitcher);
    }

    static SyncReactive syncReactive(Consumer<Runnable> asyncContextSwitcher, Consumer<Runnable> syncContextSwitcher) {
        return new SyncReactiveImpl(asyncContextSwitcher, syncContextSwitcher);
    }
}
