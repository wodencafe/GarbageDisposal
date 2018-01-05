# GarbageDisposal

**GarbageDisposal** is a library to register callbacks when an object is [Garbage Collected](https://www.cubrid.org/blog/understanding-java-garbage-collection), without incurring the penalty for implementing the [finalize](https://docs.oracle.com/javase/9/docs/api/java/lang/Object.html#finalize--) method. Also *finalize* is now **deprecated**.

Please see [here](https://stackoverflow.com/questions/2860121/why-do-finalizers-have-a-severe-performance-penalty) and [here](https://docs.oracle.com/javase/9/docs/api/java/lang/Object.html#finalize--) for more details on why it is problematic to implement the *finalize* method directly.

## Getting Started

This project is in the process of being hosted on [Maven Central](https://search.maven.org/), when this is complete this artifact will be available and this section will be updated with the Maven coordinates.

## Example of Use

The standard usage pattern of **GarbageDispoal** is to *decorate* an object and provide a **Runnable** callback, like so:

```
Object objectToWatch = new Object();
GarbageDisposal.decorate(objectToWatch, () -> System.out.println("Object was Garbage Collected");
```

This callback will be invoked when the JVM Garbage Collection cycle runs, and the object is [Phantom Reachable](https://docs.oracle.com/javase/7/docs/api/java/lang/ref/package-summary.html#reachability)

## Built With

* [Gradle](https://gradle.org/) - Dependency Management

## License

This project is licensed under the **BSD-3 License** - see the [LICENSE.md](LICENSE.md) file for details
