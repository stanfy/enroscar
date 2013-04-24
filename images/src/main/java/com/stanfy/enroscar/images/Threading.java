package com.stanfy.enroscar.images;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import android.os.Process;

/**
 * Thread utilities used by images manager.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
final class Threading {
  /** Thread factory. */
  private static final ThreadFactory THREAD_FACTORY = new ThreadFactory();
  /** Tasks queue. */
  private static final LinkedBlockingQueue<Runnable> IMAGE_TASKS_QUEUE = new LinkedBlockingQueue<Runnable>();
  /** Workers count. */
  static int imagesWorkersCount = 2;

  /** executors. */
  private static Executor imageTasksExecutor;

  /** Hidden constructor. */
  private Threading() { /* just hide */ }

  public static Thread createThread(final Runnable worker) { return THREAD_FACTORY.newThread(worker); }

  private static ThreadPoolExecutor createExecutor(final int wCount, final BlockingQueue<Runnable> queue) {
    return new ThreadPoolExecutor(wCount, wCount, Long.MAX_VALUE, TimeUnit.MILLISECONDS, queue);
  }

  /**
   * @param workersCount max count of workers
   */
  public static void configureImageTasksExecutor(final int workersCount) {
    imagesWorkersCount = workersCount;
  }

  /**
   * @return the image tasks executor
   */
  public static Executor getImageTasksExecutor() {
    if (imageTasksExecutor == null) {
      imageTasksExecutor = createExecutor(imagesWorkersCount, IMAGE_TASKS_QUEUE);
    }
    return imageTasksExecutor;
  }

  /**
   * A custom thread factory.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  private static class ThreadFactory implements java.util.concurrent.ThreadFactory {
    /** Threads counter. */
    private final AtomicInteger counter = new AtomicInteger(1);
    @Override
    public Thread newThread(final Runnable worker) { return new ImageThread(worker, counter.getAndIncrement()); }
  }

  /**
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  private static class ImageThread extends Thread {
    public ImageThread(final Runnable worker, final int counter) {
      super(worker, "ImageWorker #" + counter);
    }
    @Override
    public void run() {
      Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND);
      super.run();
    }
  }
}
