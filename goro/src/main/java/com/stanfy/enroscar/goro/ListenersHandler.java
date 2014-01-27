package com.stanfy.enroscar.goro;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.ArrayList;
import java.util.concurrent.Callable;

/**
 * Handler for calling listener methods. Works in the main thread.
 */
class ListenersHandler extends Handler {

  /** Message code. */
  private static final int MSG_START = 1, MSG_FINISH = 2, MSG_ERROR = 3, MSG_CANCEL = 4;

  /** Task listeners collection. */
  private final ArrayList<GoroListener> taskListeners = new ArrayList<>();

  public ListenersHandler() {
    super(Looper.getMainLooper());
  }

  private void checkThread() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
      throw new IllegalStateException("Listeners cannot be modified outside the main thread");
    }
  }

  public void addTaskListener(final GoroListener listener) {
    checkThread();
    taskListeners.add(listener);
  }

  public void removeTaskListener(final GoroListener listener) {
    checkThread();
    if (!taskListeners.remove(listener)) {
      throw new IllegalArgumentException("Listener " + listener + " is not registered");
    }
  }

  public void postStart(final Callable<?> task) {
    Message msg = obtainMessage(MSG_START);
    msg.obj = new MessageData(task, null, null);
    sendMessage(msg);
  }

  public void postFinish(final Callable<?> task, Object result) {
    Message msg = obtainMessage(MSG_FINISH);
    msg.obj = new MessageData(task, result, null);
    sendMessage(msg);
  }

  public void postError(final Callable<?> task, Throwable error) {
    Message msg = obtainMessage(MSG_ERROR);
    msg.obj = new MessageData(task, error, null);
    sendMessage(msg);
  }

  public void postCancel(final Callable<?> task) {
    Message msg = obtainMessage(MSG_CANCEL);
    msg.obj = new MessageData(task, null, null);
    sendMessage(msg);
  }

  @Override
  public void handleMessage(@SuppressWarnings("NullableProblems") final Message msg) {
    if (taskListeners.isEmpty()) {
      return;
    }

    MessageData data = (MessageData) msg.obj;
    if (data == null) {
      throw new IllegalArgumentException("Data cannot be null");
    }

    switch (msg.what) {
      case MSG_START:
        for (GoroListener listener : taskListeners) {
          listener.onTaskStart(data.task);
        }
        break;

      case MSG_FINISH:
        for (GoroListener listener : taskListeners) {
          listener.onTaskFinish(data.task, data.resultOrError);
        }
        break;

      case MSG_ERROR:
        for (GoroListener listener : taskListeners) {
          listener.onTaskError(data.task, (Throwable) data.resultOrError);
        }
        break;

      case MSG_CANCEL:
        for (GoroListener listener : taskListeners) {
          listener.onTaskCancel(data.task);
        }
        break;

      default:
        throw new IllegalArgumentException("Unexpected message " + msg);
    }

  }

  /** Message data. */
  private static class MessageData {
    /** Queue name. */
    final String queue;
    /** Task instance. */
    final Callable<?> task;
    /** Error instance. */
    final Object resultOrError;

    public MessageData(final Callable<?> task, final Object resultOrError, final String queue) {
      this.task = task;
      this.resultOrError = resultOrError;
      this.queue = queue;
    }
  }

}
