package com.stanfy.views.utils;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Threading utilities.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public final class ThreadUtils {

  /** Thread factory. */
  private static final ThreadFactory THREAD_FACTORY = new ThreadFactory();

  /** Tasks queues. */
  private static final TasksQueue TASKS_QUEUE = new TasksQueue(), IMAGE_TASKS_QUEUE = new TasksQueue();

  /** Workers count. */
  private static int mainWorkersCount, imagesWorkersCount;

  /** executors. */
  private static Executor mainTasksExecutor, imageTasksExecutor;

  /** Hidden constructor. */
  private ThreadUtils() { /* just hide */ }

  public static Thread createThread(final Runnable worker) { return THREAD_FACTORY.newThread(worker); }

  public static void createThreadAndStart(final Runnable worker) { createThread(worker).start(); }

  private static ThreadPoolExecutor createExecutor(final int wCount, final TasksQueue queue) {
    return new ThreadPoolExecutor(wCount, wCount, Long.MAX_VALUE, TimeUnit.MILLISECONDS, queue);
  }

  /**
   * @param workersCount max count of workers
   */
  public static void configureMainTasksExecutor(final int workersCount) {
    mainWorkersCount = workersCount;
  }
  /**
   * @param workersCount max count of workers
   */
  public static void configureImageTasksExecutor(final int workersCount) {
    imagesWorkersCount = workersCount;
  }

  /**
   * @return the mainTasksExecutor
   */
  public static Executor getMainTasksExecutor() {
    if (mainTasksExecutor == null) {
      mainTasksExecutor = createExecutor(mainWorkersCount, TASKS_QUEUE);
    }
    return mainTasksExecutor;
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

  public static void cancelImageTask(final String taskName) {
    IMAGE_TASKS_QUEUE.cancelTask(taskName);
  }
  public static void cancelImageTaskByTag(final Object tag) {
    IMAGE_TASKS_QUEUE.cancelTaskByTag(tag);
  }

  /**
   * A custom thread factory.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  private static class ThreadFactory implements java.util.concurrent.ThreadFactory {

    /** Threads counter. */
    private static int counter = 0;

    @Override
    public Thread newThread(final Runnable worker) {
      final Thread thread = new Thread(worker, "Worker-" + (counter++));
      thread.setPriority(Thread.MIN_PRIORITY);
      return thread;
    }

  }

}
