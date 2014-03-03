package com.stanfy.enroscar.content.async.test;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.stanfy.enroscar.content.async.Async;
import com.stanfy.enroscar.content.async.AsyncObserver;
import com.stanfy.enroscar.content.async.internal.AsyncContext;
import com.stanfy.enroscar.content.async.internal.LoadAsync;

import java.util.concurrent.CountDownLatch;

/**
 * Activity that uses Async.
 */
public final class AsyncUserActivity extends FragmentActivity {

  /** Text view. */
  TextView textView;

  /** A thing. */
  Thing thing;

  final CountDownLatch loadSync = new CountDownLatch(1);

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    textView = new TextView(this);
    setContentView(textView);

    new DataGenerated(this).thing().subscribe(new AsyncObserver<Thing>() {
      @Override
      public void onError(final Throwable e) {
        throw new AssertionError(e);
      }
      @Override
      public void onResult(final Thing data) {
        thing = data;
        textView.setText(data.toString());
        loadSync.countDown();
      }
    });
  }

  /** Loader. */
  static class Data {

    /** Observer instance. */
    private AsyncObserver<Thing> observer;

    /** Loading task. */
    private final AsyncTask<Void, Void, Thing> task = new AsyncTask<Void, Void, Thing>() {
      @Override
      protected Thing doInBackground(final Void... params) {
        return new Thing();
      }

      @Override
      protected void onPostExecute(final Thing thing) {
        AsyncObserver<Thing> o = observer;
        if (o != null) {
          o.onResult(thing);
        }
      }
    };

    Async<Thing> thing() {
      return new Async<Thing>() {
        @Override
        public void subscribe(final AsyncObserver<Thing> observer) {
          if (Data.this.observer != null) {
            throw new IllegalStateException();
          }
          Data.this.observer = observer;
          task.execute();
        }
        @Override
        public void cancel() {
          observer = null;
          task.cancel(true);
        }
      };
    }

  }

  /** Something that is loaded. */
  static class Thing {
    @Override
    public String toString() {
      return "a thing";
    }
  }

  // -------------- GENERATED CODE ----------------

  static final class DataGenerated extends Data {

    private final AsyncUserActivity activity;

    AsyncContextImpl lastCreatedContext;

    public DataGenerated(final AsyncUserActivity activity) {
      this.activity = activity;
    }

    @Override
    Async<Thing> thing() {
      AsyncContextImpl context = new AsyncContextImpl(activity, super.thing());
      lastCreatedContext = context;
      return new LoadAsync<>(activity.getSupportLoaderManager(), context, 1);
    }

    static final class AsyncContextImpl extends AsyncContext<Thing> {

      /** For recording and tests. */
      Thing releasedThing;

      public AsyncContextImpl(final Activity activity, final Async<Thing> async) {
        super(async, activity);
      }

      @Override
      public void releaseData(final Thing data) {
        releasedThing = data;
      }
    }

  }

}
