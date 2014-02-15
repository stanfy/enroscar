package com.stanfy.enroscar.goro;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.concurrent.Callable;

/**
 * Handler for calling listener methods. Works in the main thread.
 */
class ListenersHandler extends BaseListenersHandler {

  /** Message code. */
  private static final int MSG_START = 1, MSG_FINISH = 2, MSG_ERROR = 3, MSG_CANCEL = 4,
                           MSG_SCHEDULE = 5;

  /** Initial capacity. */
  private static final int INIT_CAPACITY = 5;

  /** Handler implementation. */
  private final H h = new H();

  public ListenersHandler() {
    super(INIT_CAPACITY);
  }

  public void postSchedule(final Callable<?> task, final String queue) {
    Message msg = h.obtainMessage(MSG_SCHEDULE);
    msg.obj = new MessageData(task, null, queue);
    h.sendMessage(msg);
  }

  public void postStart(final Callable<?> task) {
    Message msg = h.obtainMessage(MSG_START);
    msg.obj = new MessageData(task, null, null);
    h.sendMessage(msg);
  }

  public void postFinish(final Callable<?> task, Object result) {
    Message msg = h.obtainMessage(MSG_FINISH);
    msg.obj = new MessageData(task, result, null);
    h.sendMessage(msg);
  }

  public void postError(final Callable<?> task, Throwable error) {
    Message msg = h.obtainMessage(MSG_ERROR);
    msg.obj = new MessageData(task, error, null);
    h.sendMessage(msg);
  }

  public void postCancel(final Callable<?> task) {
    Message msg = h.obtainMessage(MSG_CANCEL);
    msg.obj = new MessageData(task, null, null);
    h.sendMessage(msg);
  }

  /** Handler implementation. */
  private class H extends Handler {

    public H() {
      super(Looper.getMainLooper());
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
        case MSG_SCHEDULE:
          for (GoroListener listener : taskListeners) {
            listener.onTaskSchedule(data.task, data.queue);
          }
          break;

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
