# sync-reactive

### A simple library for executing blocking code asynchronously and non-blocking code synchronously without adding a Java agent.

In this era of cloud and serverless computing, minimization of compute resource usage has become paramount as it directly impacts application performance and operating costs. 

One of the techniques adopted to reduce usage of OS/CPU threads is non-blocking operations which in turn has led to adoption of the reactive programming paradigm because to take advantage of non-blocking operations, request-reply communication is replaced with notify-handle communication. 

A side effect of adopting reactive programming is that it moves the notify-handle way of programming from the lower layer (I/O interfaces) were it was needed to the highest level (domain layer) as a result we have code that is no longer representative of the business use case it's trying to solve.

What if we were able to write the business layer in the traditional way (request-reply) without breaking the non-blocking requirements of the lower layer (notify-handle).

A solution to this is already on the way for the JVM in [Project Loom](https://wiki.openjdk.java.net/display/loom/Main) although it's not yet ready as at JDK13.

There are other projects as well utilizing continuations to solve this problem...so what makes this different ?

Most other projects I know of use Java agents to transform compiled source to apply continuations. This project doesn't rather it provides two methods:
* `sync` to run non-blocking code and wait for its completion and 
* `async` to run blocking code asynchronously.

An example flow is:

'''
  
'''
