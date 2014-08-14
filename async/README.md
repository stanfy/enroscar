Enroscar Async
==============

Makes it easy to put your asynchronous operations behind
[Android's Loader](https://developer.android.com/reference/android/content/Loader.html).

Main abstraction here is
[`Async`](core/src/main/java/com/stanfy/enroscar/async/Async.java)
which represents some asynchronous operation.

How it is used
--------------

Read our [recipes page](https://github.com/stanfy/enroscar/wiki/Enroscar-Async-Recipes).
Here is a quick overview.

Describe an asynchronous operation.

```java
class Foo {
  @Load Async<String> loadGreeting(final String name) {
    return Tools.async(new Callable<String>() {
      public String call() {
        try { Thread.sleep(1000); } catch (InterruptedException ignored) { }
        return "Hello " + name;
      }
    });
  }
}
```

In this example we create a simple `Callable` and pass it to `Tools.async` method that
constructs an `Async` implementation running our `Callable` in Android's `AsyncTask`.

An annotations processor generates an operator class `FooOperator` that can be used to 
start/cancel your operations or subscribe to their execution results. Use it!

```java
// this an object that provides operations
Foo foo = new Foo();

// prepare the operator
// FooOperator is a generated class
FooOperator operator = FooOperator.build()
    .withinActivity(activity)
    .operations(foo)
    .get();

// subscribing
operator.when().loadGreetingIsFinished()
    .doOnResult(new Action<String>() {
      @Override
      public void act(final String greeting) {
        Log.i("Async", "Greeting loaded: " + greeting);
      }
    });

// starting
operator.loadGreeting();

// cancelling
operator.cancelLoadGreeting();    
```

**Note** that working with an `operator` you actually control an Android `Loader`. Hence, using it 
within an `Activity` or `Fragment` you do not care about their lifecycle. Actions you provide 
subscribing to operations will be automatically attached to running tasks during `Activity` 
recreation.


Download
--------
![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.stanfy.enroscar/enroscar-async/badge.svg)

Gradle

```groovy
compile 'com.stanfy.enroscar:enroscar-async:{latestVersionHere}'
// annotation processor should be added to 'compile' configuration
provided 'com.stanfy.enroscar:enroscar-async-compiler:{latestVersionHere}'
```
To use it with RxJava you'll also need
```groovy
compile 'com.stanfy.enroscar:enroscar-async-rx:{latestVersionHere}'
compile 'com.netflix.rxjava:rxjava-core:{latestVersionHere}'
```
