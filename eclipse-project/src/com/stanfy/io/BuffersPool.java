package com.stanfy.io;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.app.beans.EnroscarBean;
import com.stanfy.app.beans.FlushableBean;

/**
 * A pool of temporary storages required for images decoding or other IO operations.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
@EnroscarBean(BuffersPool.BEAN_NAME)
public class BuffersPool implements FlushableBean {

  /** Bean name. */
  public static final String BEAN_NAME = "BuffersPool";

  /** Default buffer size. */
  public static final int DEFAULT_SIZE_FOR_IMAGES = 16 * 1024;

  /** Used buffers count. */
  private final AtomicInteger usedBuffersCount = new AtomicInteger(0), buffersCount = new AtomicInteger(0);

  /** Logging tag. */
  private static final String TAG = BEAN_NAME;

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_BUFFERS;

  /** Buffers store. */
  private final TreeMap<Integer, List<Object>> buffers = new TreeMap<Integer, List<Object>>();

  /** Protects {@link #buffers}. */
  private Object lock = new Object();

  private static byte[] allocate(final int size) { return new byte[size]; }

  /** Default pool configuration. */
  private static final int[][] DESCRIPTION_DEFAULT = {
    {4, DEFAULT_SIZE_FOR_IMAGES}, {2, IoUtils.BUF_SIZE}
  };

  public BuffersPool() {
    this(DESCRIPTION_DEFAULT);
  }

  public BuffersPool(final int[][] initDescription) {
    if (DEBUG) { Log.i(TAG, "Creating buffers. Types count: " + initDescription.length); }
    for (int i = initDescription.length - 1; i >= 0; i--) {
      final int count = initDescription[i][0];
      final int amount = initDescription[i][1];
      for (int k = count - 1; k >= 0; k--) {
        buffersCount.incrementAndGet();
        release(allocate(amount));
      }
    }
    usedBuffersCount.set(0);
  }

  /**
   * @return buffer with default width
   */
  public byte[] get() {
    return get(IoUtils.BUF_SIZE);
  }

  /**
   * @param minCapacity minimal capacity of the buffer
   * @return buffer with length greater on equal than <code>minCapacity</code>
   */
  public byte[] get(final int minCapacity) {
    if (DebugFlags.STRICT_MODE && minCapacity % IoUtils.BUF_SIZE != 0) {
      Log.v(TAG, "Be careful. Buffer capacity cannot be divided into " + IoUtils.BUF_SIZE);
    }
    usedBuffersCount.incrementAndGet();
    final SortedMap<Integer, List<Object>> map = buffers.tailMap(minCapacity);

    synchronized (lock) {
      if (map == null || map.isEmpty()) {
        buffersCount.incrementAndGet();
        return allocate(minCapacity);
      }

      final List<Object> bList = map.get(map.firstKey());
      if (bList == null || bList.isEmpty()) {
        buffersCount.incrementAndGet();
        return allocate(minCapacity);
      }

      final byte[] array = (byte[])bList.remove(0);
      return array;
    }
  }

  /**
   * Recycle the buffer.
   * @param buffer unused buffer
   */
  public void release(final byte[] buffer) {
    if (buffer == null) { return; }
    final int capacity = buffer.length;
    if (capacity == 0) { return; }

    synchronized (lock) {
      List<Object> bList = buffers.get(capacity);
      if (bList == null) {
        bList = new LinkedList<Object>();
        buffers.put(capacity, bList);
      }
      bList.add(buffer);
    }

    usedBuffersCount.decrementAndGet();
    if (DEBUG) { Log.d(TAG, "Buffers in use: " + usedBuffersCount + "/" + buffersCount); }
  }

  @Override
  public void flushResources() {
    synchronized (lock) {
      if (buffers.size() > 2) {
        buffers.clear();
        Log.i(TAG, "Buffers flushed");
      }
    }
  }

}
