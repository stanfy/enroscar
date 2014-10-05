Version 2.0-RC1
---------------
- Automatic binding to existing loaders after activity recreation. Eliminates need to save state
  about running operation.
- Generation of `{operation}IsStartedDo(Runnable)` methods for adding callbacks that are invoked
  when asynchronous operation starts.

Version 1.0.0
-------------
Initial release.
