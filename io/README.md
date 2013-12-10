Enroscar IO
===========
Java buffers pool implementation + some other I/O utilities.

Buffers pool
------------

1. Create an instance of buffers pool.
  ```java
    BuffersPool pool = new BuffersPool();
  ```

2. Use the pool to get a temporary array instead of direct buffer allocation, and release the array when you are done:
  ```java
    // get a buffer
    byte[] buffer = pool.get(1024);

    int cnt;
    while ((cnt = in.read(buffer)) != -1) {
      // use data in buffer
    }

    // release a buffer
    pool.release(buffer);
  ```

One might also use `PoolableBufferedXXXStream` instead of `BufferedXXXStream`.
Supply your pool instance to stream wrappers using their constructors.

Class `IoUtils` contains some methods for operating on streams using a buffers pool
(like `transfer(InputStream, OutputStream, BuffersPool)`).


Stream wrappers with progress listeners
---------------------------------------

```java
ProgressListener listener = new ProgressListener() {
  @Override
  public void onInputProgress(final long bytesWritten, final long totalCount, final float percent) {
    System.out.println(percent);
  }

  @Override
  public void onOutputClosed() {
    System.out.println("closed");
  }
};

File file = new File("data.txt");
InputStream in = new ProgressListenerInputStream(new FileInputStream(file), listener, file.length());
IoUtils.consumeStream(in);
```

Library usage
-------------

TODO: aar, jar
