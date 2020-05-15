# sync-reactive

### A simple library for executing blocking code asynchronously and non-blocking code synchronously without adding a Java agent.

In this era of cloud and serverless computing, minimization of compute resource usage has become paramount as it directly impacts application performance and operating costs. 

One of the techniques adopted to reduce usage of OS/CPU threads is non-blocking operations which in turn has led to adoption of the reactive programming paradigm. This is because to take advantage of non-blocking operations, request-reply communication is replaced with notify-handle communication. 

A side effect of adopting reactive programming is that it moves the notify-handle way of programming from the lower layer (I/O interfaces) were it was needed to the highest level (domain layer) as a result we have code that is no longer representative of the business use case it's trying to solve.

What if we were able to write the business layer in the traditional way (request-reply) without breaking the non-blocking requirements of the lower layer (notify-handle).

A solution to this is already on the way for the JVM in [Project Loom](https://wiki.openjdk.java.net/display/loom/Main) although it's not yet ready as at JDK13.

There are other projects as well utilizing continuations to solve this problem...so what makes this different ?

Most other projects I know of use Java agents to transform compiled source to apply continuations. This project doesn't rather it provides two methods:
* `sync` to run non-blocking code and wait for its completion and 
* `async` to run blocking code asynchronously.

An example usage is:

```
  SyncReactive syncReactive = SyncReactive.create(); // or SyncReactive.create(yourForkJoinPool);
  Result result = syncReactive.sync(req-> callSomeNonBlockingService(), requestObj);
  doSomethingWithResult(result);
```
This will cause the thread to wait for the result, until it becomes available.

#### How does this work? 
Under the hood, **sync-reactive** uses the JVM ForkJoinPool (FJP) work stealing behaviour to pretend to wait. 
A `SyncTask` which extends [`CountedCompleter`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CountedCompleter.html) is created and [`invoke`](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/ForkJoinPool.html#invoke-java.util.concurrent.ForkJoinTask-)d on the FJP. The invoke call causes the FJP to not return until the `SyncTask` is completed. Within the `SyncTask`, the non-blocking call is made asynchronously and a callback is set to complete the `SyncTask` when the call completes. This waiting for completion by the callback causes the FJP to wait until the async call returns.
#### Blocking behaviour
But is this not causing a blocking behaviour, no, it is not. What is really happening is that the FJP only blocks when there is no more work for it to do on it's work queue. So typically, it queues the `SyncTask` on it's work queue and as many more that are submitted and uses the few threads within it to pick up from the queue and execute. In the event that there is no more task to execute then it blocks and waits for more work to be submitted.
#### Starvation by CPU intensive task
It is always advisable to apply non-blocking to non-CPU intensive tasks such as network calls or calls to the file system. It is not advisable to hog the KFJ threads using a task that sits on a thread.
#### Running non-blocking call in a specific context thread
Often times we want to run the non-blocking code in a thread context different from that of the FKJ, this is possible by initializing
`SyncReactive` with a context switching consumer. See `SyncReactiveTest.syncRunsAsyncCodeOnDifferentThreadContext` for how this is done
#### Other Uses
There is an `async` method that does the reverse, run blocking code asynchronously using the FKJ. Again, be mindful of not hogging threads.
Also for the `async` method it is possible to supply a context switching consumer on which you want the completion of the async run of the blocking code to be executed. See `SyncReactiveTest.asyncRunsCompletionOnDifferentThreadContext`.
