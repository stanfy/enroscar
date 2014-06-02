Version 2.0.0
=============
- Returned `Future` is an observable future allowing listening to task execution result/error:
  `ObservableFuture` interface.
- Simplified interaction with a `Goro` instance that is run in the service context:
  `BoundGoro` class.
- `Goro` constructor is removed: use static factory method in `Goro` class instead.
- Service context can be injected to tasks scheduled with `startService`:
  `ServiceContextAware` interface.
- Errors thrown within tasks scheduled with `startService` are not silently eaten.
- Ability to clear pending tasks in a queue.
- RxJava integration: `RxGoro` wrapper in `support` package. 

Version 1.2
===========

- Let's call it an initial release
