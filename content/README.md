Enroscar Content
================

Makes it easy to put your asynchronous operations behind
[Android's Loader](https://developer.android.com/reference/android/content/Loader.html).

Main abstraction here is
[`Async`](providers/src/main/java/com/stanfy/enroscar/async/Async.java)
which represents some asynchronous operation.

Start with describing your operations
-------------------------------------

Let's start with describing an async operation that loads some user name
by user's ID and provides a greeting string.

```java
@Load Async<String> loadGreeting(long userId) {
  return Tools.async(new Callable<String>() {
    public String call() {
      String name = fetchUserName(userId);
      return "Hello " + name;
    }
  });
}
```

In this example we create a simple `Callable` and pass it to `Tools.async` method that
constructs an `Async` implementation running our `Callable` in Android's `AsyncTask`.

// TODO
