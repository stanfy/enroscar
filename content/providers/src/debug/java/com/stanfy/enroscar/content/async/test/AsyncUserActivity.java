package com.stanfy.enroscar.content.async.test;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.stanfy.enroscar.content.async.Async;
import com.stanfy.enroscar.content.async.AsyncObserver;
import com.stanfy.enroscar.content.async.Tools;
import com.stanfy.enroscar.content.async.internal.AsyncContext;
import com.stanfy.enroscar.content.async.internal.LoadAsync;
import com.stanfy.enroscar.content.async.internal.SendAsync;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * Activity that uses Async.
 */
public final class AsyncUserActivity extends FragmentActivity {

  /** Text view. */
  TextView textView;

  /** A getThing. */
  Thing thing;

  final CountDownLatch loadSync = new CountDownLatch(1);
  final CountDownLatch sendSync = new CountDownLatch(1);

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    textView = new TextView(this);
    setContentView(textView);

    final Data data = new DataGenerated(AsyncUserActivity.this);

    textView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        data.sendThing().subscribe(new AsyncObserver<Thing>() {
          @Override
          public void onError(final Throwable e) {
            throw new AssertionError(e);
          }
          @Override
          public void onResult(final Thing data) {
            thing = data;
            textView.setText(textView.getText() + "Send result: " + data.toString());
            sendSync.countDown();
          }
        });
      }
    });

    data.getThing().subscribe(new AsyncObserver<Thing>() {
      @Override
      public void onError(final Throwable e) {
        throw new AssertionError(e);
      }

      @Override
      public void onResult(final Thing data) {
        thing = data;
        textView.setText(textView.getText() + data.toString());
        loadSync.countDown();
      }
    });
  }

  /** Loader. */
  static class Data {

    Async<Thing> getThing() {
      return getAsync();
    }

    Async<Thing> sendThing() {
      return getAsync();
    }

    private Async<Thing> getAsync() {
      return Tools.async(new Callable<Thing>() {
        @Override
        public Thing call() throws Exception {
          return new Thing();
        }
      });
    }
  }

  /** Something that is loaded. */
  public static class Thing {
    @Override
    public String toString() {
      return "a getThing";
    }
  }

  // -------------- GENERATED CODE ----------------

  static final class DataGenerated extends Data {

    private final AsyncUserActivity activity;

    private final SendAsync<Thing> sendThingAsync;

    public DataGenerated(final AsyncUserActivity activity) {
      this.activity = activity;
      AsyncContextImpl context = new AsyncContextImpl(activity, super.sendThing());
      sendThingAsync = new SendAsync<>(activity.getSupportLoaderManager(), context, 1);
    }

    @Override
    Async<Thing> getThing() {
      AsyncContextImpl context = new AsyncContextImpl(activity, super.getThing());
      return new LoadAsync<>(activity.getSupportLoaderManager(), context, 2);
    }

    @Override
    Async<Thing> sendThing() {
      return sendThingAsync;
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
