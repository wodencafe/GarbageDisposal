# GarbageDisposal

GarbageDisposal is a library to register callbacks when an object is Garbage Collected, without incurring the penalty for implementing the finalize method.

## Getting Started

This project is in the process of being hosted on Maven Central, when this is complete this artifact will be available and this section will be updated with the Maven coordinates.

## Example of Use

The standard usage pattern of **GarbageDispoal** is to *decorate* an object and provide a **Runnable** callback, like so:

```
Object objectToWatch = new Object();
GarbageDisposal.decorate(objectToWatch, () -> System.out.println("Object was Garbage Collected");
```

This callback will be invoked when the JVM Garbage Collection cycle runs, and the object is [Phantom Reachable](https://docs.oracle.com/javase/7/docs/api/java/lang/ref/package-summary.html#reachability)

## License

This project is licensed under the **BSD-3 License** - see the [LICENSE.md](LICENSE.md) file for details
