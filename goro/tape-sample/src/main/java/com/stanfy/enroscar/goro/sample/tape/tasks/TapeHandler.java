package com.stanfy.enroscar.goro.sample.tape.tasks;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.squareup.tape.TaskQueue;
import com.stanfy.enroscar.goro.FutureObserver;
import com.stanfy.enroscar.goro.Goro;
import com.stanfy.enroscar.goro.GoroService;
import com.stanfy.enroscar.goro.ServiceContextAware;

import java.util.concurrent.Callable;

import javax.inject.Inject;

import static com.stanfy.enroscar.goro.sample.tape.SampleApplication.graph;

/**
 * Iterates over the queue of {@link com.stanfy.enroscar.goro.sample.tape.tasks.TokenTask}
 * and schedules tasks execution on a dedicated queue.
 */
public class TapeHandler implements Callable<Void>, Parcelable, ServiceContextAware {

  public static final Creator<TapeHandler> CREATOR = new Creator<TapeHandler>() {
    @Override
    public TapeHandler createFromParcel(final Parcel source) {
      return new TapeHandler();
    }

    @Override
    public TapeHandler[] newArray(final int size) {
      return new TapeHandler[size];
    }
  };

  @Inject Goro goro;

  @Inject TaskQueue<TransactionTask> tape;

  @Inject Context context;

  TapeHandler() {
    // hide the constructor
  }

  public static TapeHandler create(final Context context, final Goro goro) {
    TapeHandler handler = new TapeHandler();
    handler.inject(context, new QueueHandlerModule(context.getApplicationContext(), goro));
    return handler;
  }

  @Override
  public void injectServiceContext(final Context context) {
    inject(context, new QueueHandlerModule(context, ((GoroService) context).getGoro()));
  }

  private void inject(final Context context, final Object module) {
    graph(context, module).inject(this);
  }

  @Override
  public Void call() {
    Log.i("TapeHandler", "Handling the tape " + tape);
    TransactionTask task;
    while ((task = tape.peek()) != null) {
      try {
        task.execute(context);
        tape.remove();
      } catch (TransactionException e) {
        Log.w("TapeHandler", "Transaction " + task.getToken() + "failed. Stopping.");
        break;
      }
    }
    return null;
  }

  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(final Parcel dest, final int flags) {

  }

}
