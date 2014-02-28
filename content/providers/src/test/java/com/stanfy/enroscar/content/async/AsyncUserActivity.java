package com.stanfy.enroscar.content.async;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.TextView;

/**
 * Activity that uses Async.
 */
class AsyncUserActivity extends Activity {

  /** Text view. */
  TextView textView;

  /** A thing. */
  Thing thing;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    textView = new TextView(this);
    setContentView(textView);

    new Data().thing().subscribe(new AsyncObserver<Thing>() {
      @Override
      public void onError(final Throwable e) {
        throw new AssertionError(e);
      }
      @Override
      public void onResult(final Thing data) {
        thing = data;
        textView.setText(data.toString());
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

    @Load Async<Thing> thing() {
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

}
