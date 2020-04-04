package com.o4oxide.syncreactive;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public interface SyncReactive {
    <T, R> CompletableFuture<R> async(FunctionParam<T, R> blockingFunctionParam);
    <T, R> R sync(FunctionParam<T, CompletableFuture<R>> nonBlockingFunctionParam);

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
