Enroscar Async
==============

Makes it easy to put your asynchronous operations behind
[Android's Loader](https://developer.android.com/reference/android/content/Loader.html).

Main abstraction here is
[`Async`](providers/src/main/java/com/stanfy/enroscar/async/Async.java)
which represents some asynchronous operation.

How it is used
--------------

Describe an asynchronous operation.

```java
class Foo {
  @Load Async<String> loadGreeting(long userId) {
    return Tools.async(new Callable<String>() {
      public String call() {
        String name = fetchUserName(userId);
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

See more (including RxJava integration) at our Recipes page.

Download
--------
Gradle

```groovy
compile 
```
