Goro
====

Goro performs asynchronous operations in a queue.
You may ask Goro to perform some task with `schedule` method invocation:
```java
Goro goro = Goro.create();
goro.schedule(myOperations);
```

Tasks are instances of [`Callable`](https://developer.android.com/reference/java/util/concurrent/Callable.html).

All the operations you ask Goro to perform are put in a queue and executed one by one.
Goro allows you to organize multiple queues. You can specify what queue a task should be sent to
with the second argument of `schedule` method:
```java
goro.schedule("firstQueue", myOperations1);
goro.schedule("secondQueue", myOperations2);
goro.schedule("firstQueue", myOperations3);
goro.schedule("secondQueue", myOperations4);
```

Queue is defined with a name. Goro does not limit number of your queues and lazily creates a new
queue when a new name is passed. Controlling number of queues (actually number of different strings
you pass to Goro) is your responsibility.

While operations scheduled for the same queue are guaranteed to be executed sequentially,
operations in different queues may be executed in parallel.

After scheduling your task to be performed, Goro returns a `Future` instance that may be used
to cancel your task or wait for its finishing synchronously.
```java
Future<?> taskFuture = goro.schedule(task);
taskFuture.cancel(true);
```

`Future` returned by Goro has an extended interface `ObservableFuture`, which allows you to
asynchronously listen to task execution results:
```java
// executed in the task thread
goro.schedule(task).subscribe(new FutureObserver() {
  public void onSuccess(Result value) {
    Log.i(TAG, "Task result: " + value);
  }
  public void onError(Throwable error) {
    Log.e(TAG, "Task error: " + error);
  }
});
// customize how observer is executed
goro.schedule(task).subscribe(uiThreadExecutor, observer);
```

You may also get an
[`Executor`](https://developer.android.com/reference/java/util/concurrent/Executor.html) instance
for a particular queue to integrate Goro with other libraries:
```java
// --- RxJava ---
// Perform actions in "actions queue"
Observable.from([1, 2, 3])
    .subscribeOn(Schedulers.executor(goro.getExecutor("actions queue")))
// Subscribe to scheduled task result
Observable.from(goro.schedule(myTask)).subscribe(...);

// --- Bolts ---
// Fetch something and post database write operation to a dedicated queue
fetchAsync(object).continueWith(new Continuation<ParseObject, Long>() {
  public Long then(ParseObject object) throws Exception {
    return database.storeUser(object.get("name"), object.get("age"));
  }
}, goro.getExecutor("database"));
```

Goro Motivation
---------------
Developing Android apps you'll find out that it's a good practice to ensure sequential order of
some of your asynchronous operations, like remote backend interactions or writing to the local
database. Actually this is perhaps one of the main reasons why Android `AsyncTask` executes its
tasks one by one. However often you want to go beyond one global queue: e. g. you want to have
*separate* series of networking and local database operations. And here Goro helps.


Service
-------

Usually we run Goro within a `Service` context to tell Android system that there are ongoing tasks
and ensure that our process is not the first candidate for termination.
Such a service is `GoroService`. If you use `aar` package of this library in a Gradle build based
project, the service will be added to your app automatically.
Otherwise you'll need to insert the following line into your `AndroidManifest.xml`:
```xml
<service android:name="com.stanfy.enroscar.goro.GoroService" />
```

The service creates a `Goro` instance when it's created. To interact with this instance you'll need
to bind to the service. Use `BoundGoro` for this:
```java

public class MyActivity extends Activity {

  // Goro instance
  private BoundGoro goro;

  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    // The factory method is supplied with a Context used to execute bindService method.
    goro = Goro.bindWith(this);

    findViewById(R.id.submitButton).setOnClickListener(new OnClickListener() {
      public void onClick(View view) {
        goro.schedule(submissionTask);
      }
    });
  }

  protected void onStart() {
    super.onStart();
    goro.bind();
  }

  protected void onStop() {
    super.onStop();
    goro.unbind();
  }

}

```

We may also ask `GoroService` to perform some task sending an intent containing our `Callable`
instance. Yet this instance must also implement `Parcelable` to be able to be packaged into
`Intent` extras. This way we won't need any service binding.

```java
  context.startService(GoroService.taskIntent(context, myTask));
  context.startService(GoroService.taskIntent(context, "notDefaultQueue", myTask2));
```

Intent constructed with `GoroService.taskIntent` can also be used to obtain a `PendingIntent`
and schedule task execution with `AlarmManager` or `Notification`:
```java
  Intent taskIntent = GoroService.taskIntent(context, myTask);
  PendingIntent pending = PendingIntent.getService(context, 0, taskIntent, 0);

  AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
  alarmManager.set(AlarmManager.ELAPSED_REALTIME, scheduleTime, pending);

  new Notification.Builder(context).setContentIntent(pending);
```


Goro listeners
--------------
You may add listeners that will be notified when each task starts, finishes, fails,
or is canceled.
```java
  goro.addTaskListener(myListener);
```

All the listener callbacks are invoked in the main thread. Listener can be added or removed in
the main thread only too.


Errors Handling
---------------
Goro `schedule` method returns you an `ObservableFuture` instance. Hence you can handle an error
thrown by a scheduled `Callable` in `try-catch` block that wraps future's `get` method invocation.
```java
Future<Result> f = goro.schedule(task);
// ...
try {
  Result result = f.get();
} catch (ExecutionException e) {
  Exception thrownException = e.getCause();
}
```

Also the thrown exception will be passed to all observers that are subscribed to the `Future`
instance:
```java
goro.schedule(task).subscribe(new FutureObserver() {
  public void onSuccess(Result value) { }

  public void onError(Throwable error) {
    // handle you error here
  }
});
```

This is your responsibility to handle an error. If the returned `Future` instance is not interacted
in any way, the thrown error will be never handled by anyone. Although your app won't crash,
you'll never know about the error.

When you schedule your task with an `Intent` sent to `GoroService`
(either directly with `context.startService(GoroService.taskIntent(task))` or with execution
of `PendingIntent`), you do not have these means to track an error though. Hence, `GoroService`
sets its own observer, and if an exception is thrown by a `Callable` in such conditions, service
will throw `GoroException` wrapping the cause, which will crash the app.
This policy can be soften, if you set `EXTRA_IGNORE_ERROR` to `true` on the `Intent` passed to the
service. However we discourage you from doing this. And if you do, ensure that you have set up
a global `GoroListener` that can handle the error.


Usage
-----
![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.stanfy.enroscar/enroscar-goro/badge.svg)

Goro is an Android library packaged as AAR and available in Maven Central.
Add this dependency to your Android project in `build.gradle`:
```groovy
dependencies {
  compile 'com.stanfy.enroscar:enroscar-goro:2.0.0@aar'
}
```

Using this library with Android Gradle plugin will automatically add `GoroService` to your
application components, so that you won't need to add anything to you `AndroidManifest` file.

If you do not plan to use `GoroService` as it is provided, change your dependency specification
to fetch a JAR instead of AAR:
```groovy
dependencies {
  compile 'com.stanfy.enroscar:enroscar-goro:2.0.0'
}
```

You may also simply grab a [JAR](http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.stanfy.enroscar&a=enroscar-goro&v=LATEST&e=jar)
or an [AAR](http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.stanfy.enroscar&a=enroscar-goro&v=LATEST&e=aar).
Or use it with Maven:
```xml
  <dependency>
    <groupId>com.stanfy.enroscar</groupId>
    <artifactId>enroscar-goro</artifactId>
    <version>$latestVersion</version>
  </dependency>
```


Queues are not threads
----------------------
There is no mapping between queues and actual threads scheduled operations are executed in.
By default, to perform tasks Goro uses the same thread pool that `AsyncTask` operate with.
On older Android versions, where this thread pool is not available in public API, Goro creates its
own pool manually with configuration similar to what is used in `AsyncTask`.

You may also specify different actual executor for Goro either with
`GoroService.setDelegateExecutor(myThreadPool)` or with `new Goro(myThreadPool)` depending on how
you use Goro.

Sample
------
In this repository you'll also find [a sample](sample) demonstrating what Goro does.

RxJava Integration
------------------
As long as you have RxJava jar in your classpath, you can use class `RxGoro`, which can wrap
a `Goro` instance and provide `schedule` methods that return `rx.Observable` instead of `Future`.
