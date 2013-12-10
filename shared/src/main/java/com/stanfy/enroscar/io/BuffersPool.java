package com.stanfy.enroscar.io;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.beans.FlushableBean;

/**
 * A pool of temporary storages required for images decoding or other IO operations.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
@EnroscarBean(BuffersPool.BEAN_NAME)
public class BuffersPool implements FlushableBean {

  /** Bean name. */
  public static final String BEAN_NAME = "BuffersPool";

  /** Default pool configuration. */
  private static final int[][] DESCRIPTION_DEFAULT = {
      {4, IoUtils.DEFAULT_BUFFER_SIZE_FOR_IMAGES}, {2, IoUtils.DEFAULT_BUFFER_SIZE}
  };

  /** Buffers store. */
  private final TreeMap<Integer, List<Object>> buffers = new TreeMap<Integer, List<Object>>();

  /** Protects {@link #buffers}. */
  private final Object lock = new Object();

  /** Stats counter. */
  private int usedBuffersCount, buffersCount;

  public BuffersPool() {
    this(DESCRIPTION_DEFAULT);
  }

  public BuffersPool(final int[][] initDescription) {
    for (int i = initDescription.length - 1; i >= 0; i--) {
      int count = initDescription[i][0];
      int amount = initDescription[i][1];

      for (int k = count - 1; k >= 0; k--) {
        buffersCount++;
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
    return get(IoUtils.DEFAULT_BUFFER_SIZE);
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
        buffersCount++;
        return allocate(minCapacity);
      }

      final List<Object> bList = map.get(map.firstKey());
      if (bList == null || bList.isEmpty()) {
        buffersCount++;
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


  public int getBuffersCount() {
    synchronized (lock) {
      return buffersCount;
    }
  }

  public int getUsedBuffersCount() {
    synchronized (lock) {
      return usedBuffersCount;
    }
  }

  @Override
  public void flushResources(final BeansContainer beansContainer) {
    synchronized (lock) {
      if (buffers.size() > 2) {
        buffers.clear();
      }
    }
  }

}
