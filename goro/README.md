Goro
====

Goro performs asynchronous operations in a queue.
You may ask Goro to perform some task with `schedule` method invocation:
```
  goro.schedule(myOperations);
```

Tasks are instances of [`Callable`](https://developer.android.com/reference/java/util/concurrent/Callable.html).

All the operations you ask Goro to perform are put in a queue and executed one by one.
Goro allows you to organize multiple queues. You can specify what queue a task should be sent to
with the second argument of `schedule` method:
```
  goro.schedule(myOperations, "queue1");
```

Queue is defined with a name. Goro does not limit number of your queues and lazily creates a new
queue when a new name is passed.

While operations scheduled for the same queue are guaranteed to be executed sequentially,
operations in different queues may be executed in parallel.

After scheduling your task to be performed, Goro returns a `Future` instance that may be used
to cancel your task or wait for its finishing synchronously.

```
  Future<?> taskFuture = goro.schedule(task);
  taskFuture.cancel(true);
```

Usually we run Goro within a `Service` context to tell Android system that there are ongoing tasks
and ensure that our process is not the first candidate for termination.
Such a service is `GoroService`. We can bind to it and get Goro instance from the service:

```java
  public void bindToGoroService() {
    Intent serviceIntent = new Intent(context, GoroService.class);
    context.startService(serviceIntent);
    context.bindService(serviceIntent, this /* implements ServiceConnection */, 0);
  }

  public void onServiceConnected(ComponentName name, IBinder service) {
    this.goro = Goro.from(service);
  }
```

We may also ask `GoroService` to perform some task sending an intent containing our `Callable`
instance. Yet this instance must also implement `Parcelable` to be able to be packaged into
`Intent` extras. This way we won't need any service binding.

```java
  context.startService(GoroService.taskIntent(context, myTask));
  context.startService(GoroService.taskIntent(context, myTask2, "notDefaultQueue"));
```

Intent constructed with `GoroService.taskIntent` can also be used to obtain a `PendingIntent`
and schedule task execution with `AlarmManager`:
```java
  Intent taskIntent = GoroService.taskIntent(context, myTask);
  AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
  PendingIntent pending = PendingIntent.getService(context, 0, taskIntent, 0);
  alarmManager.set(AlarmManager.ELAPSED_REALTIME, scheduleTime, pending);
```

Usage
-----

Goro is an Android library packaged as AAR and available in Maven Central.
Add this dependency to your Android project in `build.gradle`:
```groovy
dependencies {
  compile 'com.stanfy.enroscar:enroscar-goro:1.+'
}
```

Using this library with Android Gradle plugin will automatically add `GoroService` to your
application components, so that you won't need to add anything to you `AndroidManifest` file.

If you do not plan to use `GoroService` as it is provided, change your dependency specification
to fetch a JAR instead of AAR:
```groovy
dependencies {
  compile 'com.stanfy.enroscar:enroscar-goro:1.+@jar'
}
```

You may also simply grab a [JAR](http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.stanfy.enroscar&a=enroscar-goro&v=LATEST&e=jar).


Goro listeners
--------------
You may add listeners that will be notified when each task starts, finishes, fails,
or is canceled.
```java
  goro.addListener(myListener);
```

All the listener callbacks are invoked in the main thread. Listener can be added or removed in
the main thread only too.


Queues are not threads
----------------------
There is no mapping between queues and actual threads scheduled operations are executed in.
By default, to perform tasks Goro uses the same thread pool that `AsyncTask` operate with.
On older Android versions, where this thread pool is not available in public API, Goro creates its
own pool manually with configuration similar to what is used in `AsyncTask`.

You may also specify different actual executor for Goro either with
`GoroService.setDelegateExecutor(myThreadPool)` or with `new Goro(myThreadPool)` depending on how
you use Goro.
