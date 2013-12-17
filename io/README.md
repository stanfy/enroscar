Enroscar IO
===========
Java buffers pool implementation + some other I/O utilities.

Buffers pool
------------

Create an instance of buffers pool.
  ```java
    BuffersPool pool = new BuffersPool();
  ```

Use the pool to get a temporary array instead of direct buffer allocation, and release the array when you are done:
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

Wrap your input and output streams with a buffered implementation which retrieves buffer from the pool and releases it
when stream is closed:
  ```java
    InputStream input = pool.bufferize(urlConnection.getInputStream());
    OutputStream output = pool.bufferize(new FileOutputStream("path/to/file"));
  ```

Class `IoUtils` contains some methods for operating on streams using a buffers pool
(like `transfer(InputStream, OutputStream, BuffersPool)`).

You may also specify what buffers should be preallocated when the pool is initialized:
```java
BuffersPool pool = new BuffersPool(new int[][] {
  {/*how many*/ 4, /*what size*/ 8192}, // 4 byte arrays with length 8192
  {/*how many*/ 2, /*what size*/ 1024}  // 2 byte arrays with length 1024
});
System.out.println(pool.getAllocatedBuffersCount()); // output: 4
```


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

Grab the [JAR](http://repository.sonatype.org/service/local/artifact/maven/redirect?r=central-proxy&g=com.stanfy.enroscar&a=enroscar-io&v=LATEST&e=jar)
or use with Gradle:
```groovy
  compile 'com.stanfy.enroscar:enroscar-io:1.1'
```
or with Maven:
```xml
  <dependency>
    <groupId>com.stanfy.enroscar</groupId>
    <artifactId>enroscar-io</artifactId>
    <version>1.1</version>
  </dependency>
```
