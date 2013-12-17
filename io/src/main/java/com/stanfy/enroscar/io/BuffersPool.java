package com.stanfy.enroscar.io;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * A pool of arrays that might be used to decode images or perform other IO operations.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class BuffersPool {

  /** Default pool configuration. */
  private static final int[][] DESCRIPTION_DEFAULT = {
      {4, IoUtils.BUFFER_SIZE_16K}, {2, IoUtils.BUFFER_SIZE_8K}
  };

  /** Buffers store. */
  private final TreeMap<Integer, List<Object>> buffers = new TreeMap<Integer, List<Object>>();

  /** Protects {@link #buffers}. */
  private final Object lock = new Object();

  /** Stats counter. */
  private int usedBuffersCount, allocatedBuffersCount;

  /** Strict mode flag. */
  boolean strictMode = true;

  public BuffersPool() {
    this(DESCRIPTION_DEFAULT);
  }

  public BuffersPool(final int[][] initDescription) {
    for (int i = initDescription.length - 1; i >= 0; i--) {
      int count = initDescription[i][0];
      int amount = initDescription[i][1];

      for (int k = count - 1; k >= 0; k--) {
        allocatedBuffersCount++;
        release(allocate(amount));
      }

    }
    usedBuffersCount = 0;
  }

  private static byte[] allocate(final int size) {
    return new byte[size];
  }

  /**
   * @return buffer with default width
   */
  public byte[] get() {
    return get(IoUtils.BUFFER_SIZE_8K);
  }

  /**
   * @param minCapacity minimal capacity of the buffer
   * @return buffer with length greater on equal than <code>minCapacity</code>
   */
  public byte[] get(final int minCapacity) {
    synchronized (lock) {
      usedBuffersCount++;

      final SortedMap<Integer, List<Object>> map = buffers.tailMap(minCapacity);
      if (map.isEmpty()) {
        allocatedBuffersCount++;
        return allocate(minCapacity);
      }

      final List<Object> bList = map.get(map.firstKey());
      if (bList == null || bList.isEmpty()) {
        allocatedBuffersCount++;
        return allocate(minCapacity);
      }

      return (byte[])bList.remove(0);
    }
  }

  /**
   * Recycle the buffer.
   * @param buffer unused buffer
   */
  public void release(final byte[] buffer) {
    if (buffer == null) {
      return;
    }

    final int capacity = buffer.length;
    if (capacity == 0) {
      return;
    }

    synchronized (lock) {
      List<Object> bList = buffers.get(capacity);
      if (bList == null) {
        bList = new LinkedList<Object>();
        buffers.put(capacity, bList);
      }
      bList.add(buffer);

      usedBuffersCount--;
    }

  }


  /**
   * In strict mode streams obtained with {@code bufferize} write warnings to {@link System#err}
   * about unreleased buffers.
   * @param strictMode strict mode enabled flag
   */
  public void setStrictMode(boolean strictMode) {
    this.strictMode = strictMode;
  }

  public int getAllocatedBuffersCount() {
    synchronized (lock) {
      return allocatedBuffersCount;
    }
  }

  public int getUsedBuffersCount() {
    synchronized (lock) {
      return usedBuffersCount;
    }
  }

  int getBuffersMapSize() {
    synchronized (lock) {
      return buffers.size();
    }
  }


  /**
   * Clear all the retained buffers to free resources.
   * May be used in low memory conditions.
   */
  public void flush() {
    synchronized (lock) {
      buffers.clear();
    }
  }


  /**
   * Wrap input stream into a buffered implementation using this buffers pool.
   * @param input input stream to wrap
   * @return buffered input stream
   */
  public InputStream bufferize(final InputStream input) {
    return bufferize(input, IoUtils.BUFFER_SIZE_8K);
  }

  /**
   * Wrap output stream into a buffered implementation using this buffers pool.
   * @param output output stream to wrap
   * @return buffered output stream
   */
  public OutputStream bufferize(final OutputStream output) {
    return bufferize(output, IoUtils.BUFFER_SIZE_8K);
  }

  /**
   * Wrap input stream into a buffered implementation using this buffers pool.
   * @param input input stream to wrap
   * @param bufferSize buffer size
   * @return buffered input stream
   */
  public InputStream bufferize(final InputStream input, final int bufferSize) {
    return new PoolableBufferedInputStream(input, bufferSize, this);
  }

  /**
   * Wrap output stream into a buffered implementation using this buffers pool.
   * @param output output stream to wrap
   * @param bufferSize buffer size
   * @return buffered output stream
   */
  public OutputStream bufferize(final OutputStream output, final int bufferSize) {
    return new PoolableBufferedOutputStream(output, bufferSize, this);
  }

}
