# GarbageDisposal

**GarbageDisposal** is a library to register [callbacks](https://en.wikipedia.org/wiki/Callback_(computer_programming)) when an object is [Garbage Collected](https://www.cubrid.org/blog/understanding-java-garbage-collection), without incurring the [penalty](http://thefinestartist.com/effective-java/07) for implementing the [finalize](https://docs.oracle.com/javase/9/docs/api/java/lang/Object.html#finalize--) method - which, by the way, is now [deprecated as of Java 9](https://www.infoq.com/news/2017/03/Java-Finalize-Deprecated), and seems to be on its way out.

Please see [here](https://stackoverflow.com/questions/2860121/why-do-finalizers-have-a-severe-performance-penalty) and [here](https://docs.oracle.com/javase/9/docs/api/java/lang/Object.html#finalize--) for more details on why it is problematic to implement the *finalize* method directly.

## Getting Started

This project is in the process of being hosted on [Maven Central](https://search.maven.org/), when this is complete this artifact will be available and this section will be updated with the *Maven Coordinates*. 

### Maven
If you would like to start using this library in your [Maven](https://maven.apache.org/) projects, please add the following to your **pom.xml**:
```
<repositories>
    <repository>
        <id>jitpack.io</id>
        <url>https://jitpack.io</url>
    </repository>
</repositories>
```
```
<dependency>
    <groupId>com.github.wodencafe</groupId>
    <artifactId>GarbageDisposal</artifactId>
    <version>master-SNAPSHOT</version>
</dependency>
```

### Gradle
If you would like to start using this library in your [Gradle](https://gradle.org/) projects, please add the following to your **build.gradle**:
```
    repositories {
        maven { url "https://jitpack.io" 
}
```
```
dependencies {
    // https://jitpack.io/#wodencafe/GarbageDisposal
    compile 'com.github.wodencafe:GarbageDisposal:master-SNAPSHOT'
}
```

For customizing and playing with the source for yourself, please see the **[Play with the source](#play-with-the-source)** section.

## Example of Use

The standard usage pattern of [GarbageDisposal.java](src/main/java/club/wodencafe/decorators/GarbageDisposal.java) is to [decorate()](src/main/java/club/wodencafe/decorators/GarbageDisposal.java#L57) an object and provide a [Runnable](https://docs.oracle.com/javase/9/docs/api/java/lang/Runnable.html) callback, like so:

```
Object objectToWatch = new Object();
GarbageDisposal.decorate(objectToWatch, () -> System.out.println("Object was Garbage Collected");
```

This callback will be invoked when the [JVM Garbage Collection](https://www.dynatrace.com/resources/ebooks/javabook/how-garbage-collection-works/) cycle runs, and the object is [Phantom Reachable](https://docs.oracle.com/javase/7/docs/api/java/lang/ref/package-summary.html#reachability)

## Play with the source

To grab a copy of this code for yourself, please run the following commands in your workspace or directory of your choosing:
```
git clone https://github.com/wodencafe/GarbageDisposal
cd GarbageDisposal
./gradlew build
```

This will build the jar in:
`./build/libs/GarbageDisposal.jar`

You can then reference this jar for your own projects.

## Built With

* [Gradle](https://gradle.org/) - Dependency Management and Build Sytem
* [JitPack.io](https://jitpack.io/#wodencafe/GarbageDisposal) - Easy to use package repository for Git

## License

This project is licensed under the [BSD-3 License](https://opensource.org/licenses/BSD-3-Clause) - see the [LICENSE.md](LICENSE.md) file for details
