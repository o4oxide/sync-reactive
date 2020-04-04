package com.o4oxide.syncreactive;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.*;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;

public class SyncReactiveTest {

    @Test
    void asyncRunsSyncCodeAsAsync() throws ExecutionException, InterruptedException {
        SyncReactive syncReactive = SyncReactive.syncReactive();
        CountDownLatch latch = new CountDownLatch(1);
        Function<Integer, Integer> blockingFunction = num -> {
            try {
                latch.await();
                return num + 1;
            } catch (InterruptedException ex) {
                return num;
            }
        };
        CompletableFuture<Integer> future = syncReactive.async(FunctionParam.of(blockingFunction, 1));
        //Future is not done because sync code is waiting on latch
        assertFalse(future.isDone());
        //Release lock on blocking function
        latch.countDown();
        //Future completes and returns result 1 plus input: 1
        assertEquals(2, (int) future.get());
    }

    @Test
    void asyncRunsCompletionOnDifferentThreadContext() throws ExecutionException, InterruptedException {
        final String CONTEXT_THREAD_NAME = "contextSwitcher";
        SyncReactive syncReactive = SyncReactive.syncReactive(runnable -> new Thread(runnable, CONTEXT_THREAD_NAME).start());
        CountDownLatch latch = new CountDownLatch(1);
        Function<Integer, Integer> blockingFunction = num -> {
            try {
                latch.await();
                return num + 1;
            } catch (InterruptedException ex) {
                return 1;
            }
        };
        CompletableFuture<Integer> future = syncReactive.async(FunctionParam.of(blockingFunction, 1));
        //Add a completion function that will run on the supplied context when the future has completed
        CompletableFuture<Map.Entry<String,Integer>> completionFuture = future.handle((res, ex) -> Map.entry(Thread.currentThread().getName(), res));
        latch.countDown();
        //Future completion is done on supplied context thread;
        assertEquals(CONTEXT_THREAD_NAME, completionFuture.get().getKey());
        //Future completion addition result is 2
        assertEquals(2, completionFuture.get().getValue());
    }

    @Test
    void syncRunsAsyncCodeAsSync() {
        SyncReactive syncReactive = SyncReactive.syncReactive();
        long start = System.currentTimeMillis();
        Function<Integer, CompletableFuture<Integer>> asyncFunction = num -> {
            CompletableFuture<Integer> future = new CompletableFuture<>();
            Executors.newSingleThreadScheduledExecutor()
                    .schedule(()-> future.complete(num + 1),
                            2000, TimeUnit.MILLISECONDS);
            return future;
        };
        Integer result = syncReactive.sync(FunctionParam.of(asyncFunction, 1));
        long end = System.currentTimeMillis() - start;
        //Check that we actually waited for two seconds or more
        assertTrue(end >= 2000);
        //Check that we have 2 as the result
        assertEquals(2, result);
    }

    @Test
    void syncRunsAsyncCodeOnDifferentThreadContext() {
        final String CONTEXT_THREAD_NAME = "contextSwitcher";
        SyncReactive syncReactive = SyncReactive.syncReactive(runnable -> new Thread(runnable, CONTEXT_THREAD_NAME).start());
        long start = System.currentTimeMillis();
        Function<Integer, CompletableFuture<Map.Entry<String, Integer>>> asyncFunction = num -> {
            CompletableFuture<Map.Entry<String, Integer>> future = new CompletableFuture<>();
            String invocationThreadName = Thread.currentThread().getName();
            Executors.newSingleThreadScheduledExecutor()
                    .schedule(()-> future.complete(Map.entry(invocationThreadName, num +1)),
                            2000, TimeUnit.MILLISECONDS);
            return future;
        };
        Map.Entry<String, Integer> result = syncReactive.sync(FunctionParam.of(asyncFunction, 1));
        long end = System.currentTimeMillis() - start;
        //Check that we actually waited for two seconds or more
        assertTrue(end >= 2000);
        //Check that we have 2 as the result
        assertEquals(2, result.getValue());
        //Check that we ran on the context switcher thread
        assertEquals(CONTEXT_THREAD_NAME, result.getKey());
    }

    @Test
    void asyncCompletesExceptionallyWithExceptionalSyncCode() {
        SyncReactive syncReactive = SyncReactive.syncReactive();
        CountDownLatch latch = new CountDownLatch(1);
        String EXCEPTION_MESSAGE = "exception test at " + System.nanoTime();
        Function<Integer, Void> blockingFunction = num -> {
            try {
                latch.await();
                throw new SyncReactiveTestException(EXCEPTION_MESSAGE);
            } catch (InterruptedException ex) {
                return null;
            }
        };
        CompletableFuture<Void> future = syncReactive.async(FunctionParam.of(blockingFunction, 1));
        //Future is not done because sync code is waiting on latch
        assertFalse(future.isDone());
        //Release lock on blocking function
        latch.countDown();
        //Future completes exceptionally
        ExecutionException ex = assertThrows(ExecutionException.class, future::get);
        assertTrue(future.isCompletedExceptionally());
        assertEquals(SyncReactiveTestException.class, ex.getCause().getClass());
        assertEquals(EXCEPTION_MESSAGE, ex.getCause().getMessage());
    }

    @Test
    void syncThrowsUncheckedExceptionWithExceptionalAsyncCode() {
        SyncReactive syncReactive = SyncReactive.syncReactive();
        long start = System.currentTimeMillis();
        String EXCEPTION_MESSAGE = "exception test at " + System.nanoTime();
        Function<Integer, CompletableFuture<Void>> asyncFunction = num -> {
            CompletableFuture<Void> future = new CompletableFuture<>();
            Executors.newSingleThreadScheduledExecutor()
                    .schedule(()-> future.completeExceptionally(new SyncReactiveTestException(EXCEPTION_MESSAGE)),
                            2000, TimeUnit.MILLISECONDS);
            return future;
        };
        SyncReactiveTestException ex = assertThrows(SyncReactiveTestException.class, () -> syncReactive.sync(FunctionParam.of(asyncFunction, 1)));
        assertEquals(EXCEPTION_MESSAGE, ex.getMessage());
        long end = System.currentTimeMillis() - start;
        //Check that we actually waited for two seconds or more
        assertTrue(end >= 2000);
    }

    private static final class SyncReactiveTestException extends RuntimeException {
        SyncReactiveTestException(String message) {
            super(message);
        }
    }
}
