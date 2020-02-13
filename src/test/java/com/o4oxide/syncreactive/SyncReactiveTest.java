package com.o4oxide.syncreactive;

import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.concurrent.*;

import static org.junit.jupiter.api.Assertions.*;

public class SyncReactiveTest {

    @Test
    void asyncRunsSyncCodeAsAsync() throws ExecutionException, InterruptedException {
        SyncReactive syncReactive = SyncReactive.syncReactive();
        CountDownLatch latch = new CountDownLatch(1);
        //Add 1 to the input
        CompletableFuture<Integer> future =
                syncReactive.async(num -> {
                    try {
                        latch.await();
                        return num + 1;
                    } catch (InterruptedException ex) {
                        return num;
                    }
        }, 1);
        //Future is not done because sync code is waiting on latch
        assertFalse(future.isDone());
        latch.countDown();
        //Future is done because sync code has been released from latch
        assertFalse(future.isDone());
        //Future result is 1 plus input: 1
        assertEquals(2, (int) future.get());
    }

    @Test
    void asyncRunsCompletionOnDifferentThreadContext() throws ExecutionException, InterruptedException {
        final String CONTEXT_THREAD_NAME = "contextSwitcher";
        SyncReactive syncReactive = SyncReactive.syncReactive(runnable -> new Thread(runnable, CONTEXT_THREAD_NAME).start());
        CountDownLatch latch = new CountDownLatch(1);
        CompletableFuture<Integer> future = syncReactive.async(num -> {
            try {
                latch.await();
                return num + 1;
            } catch (InterruptedException ex) {
                return 1;
            }
        }, 1);
        //Add a completion function that will run when the future has completed
        CompletableFuture<String> completionFuture = future.handle((res, ex) -> Thread.currentThread().getName());
        latch.countDown();
        //Future completion is done on supplied context thread;
        assertEquals(CONTEXT_THREAD_NAME, completionFuture.get());
    }

    @Test
    void syncRunsAsyncCodeAsSync() {
        SyncReactive syncReactive = SyncReactive.syncReactive();
        //Add 1 to the input
        long start = System.currentTimeMillis();
        Integer result = syncReactive.sync(num -> {
            CompletableFuture<Integer> future = new CompletableFuture<>();
            Executors.newSingleThreadScheduledExecutor()
                    .schedule(()-> future.complete(num + 1),
                            2000, TimeUnit.MILLISECONDS);
            return future;
        }, 1);
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
        //Add 1 to the input
        long start = System.currentTimeMillis();
        Map.Entry<String, Integer> result = syncReactive.sync(num -> {
            CompletableFuture<Map.Entry<String, Integer>> future = new CompletableFuture<>();
            String invocationThreadName = Thread.currentThread().getName();
            Executors.newSingleThreadScheduledExecutor()
                    .schedule(()-> future.complete(Map.entry(invocationThreadName, num +1)),
                            2000, TimeUnit.MILLISECONDS);
            return future;
        }, 1);
        long end = System.currentTimeMillis() - start;
        //Check that we actually waited for two seconds or more
        assertTrue(end >= 2000);
        //Check that we have 2 as the result
        assertEquals(2, result.getValue());
        //Check that we ran on the context switcher thread
        assertEquals(CONTEXT_THREAD_NAME, result.getKey());
    }
}
