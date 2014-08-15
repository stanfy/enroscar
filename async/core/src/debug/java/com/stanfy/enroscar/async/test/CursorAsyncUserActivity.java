package com.stanfy.enroscar.async.test;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.stanfy.enroscar.async.Action;
import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.Load;
import com.stanfy.enroscar.async.OperatorBuilder;
import com.stanfy.enroscar.async.Tools;
import com.stanfy.enroscar.async.internal.AsyncProvider;
import com.stanfy.enroscar.async.internal.LoaderDescription;
import com.stanfy.enroscar.async.internal.ObserverBuilder;
import com.stanfy.enroscar.async.internal.OperatorBase;

import java.util.concurrent.CountDownLatch;

/**
 * User activity for CursorAsync.
 */
public class CursorAsyncUserActivity extends FragmentActivity {

  Cursor cursor;

  CountDownLatch cursorSync = new CountDownLatch(1);

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Operator op = Operator.build()
        .withinActivity(this)
        .operations(this)
        .get();

    op.when().loadCursorIsFinished()
        .doOnResult(new Action<Cursor>() {
          @Override
          public void act(Cursor data) {
            cursor = data;
            cursorSync.countDown();
          }
        })
        .doOnReset(new Runnable() {
          @Override
          public void run() {
            cursor = null;
          }
        });

    op.loadCursor();
  }

  @Load Async<Cursor> loadCursor() {
    return Tools.asyncCursor(this)
        .uri(Uri.parse("content://com.stanfy.enroscar.async.test/test"))
        .projection(new String[]{"a", "b"})
        .get();
  }

  // -------------- GENERATED CODE ----------------

  static final class Operator extends OperatorBase<CursorAsyncUserActivity, Activity$$LoaderDescription> {

    // construction

    Operator(final OperatorContext<CursorAsyncUserActivity> operatorContext) {
      super(new Activity$$LoaderDescription(operatorContext), operatorContext);
    }

    public static OperatorBuilder<Operator, CursorAsyncUserActivity> build() {
      return new OperatorBuilderBase<Operator, CursorAsyncUserActivity>() {
        @Override
        protected Operator create(final OperatorContext<CursorAsyncUserActivity> context) {
          return new Operator(context);
        }
      };
    }

    // invocation

    public void loadCursor() {
      AsyncProvider<Cursor> provider = new AsyncProvider<Cursor>() {
        @Override
        public Async<Cursor> provideAsync() {
          return getOperations().loadCursor();
        }
      };
      initLoader(1, provider, false);
    }
    public void forceLoadCursor(final int param) {
      AsyncProvider<Cursor> provider = new AsyncProvider<Cursor>() {
        @Override
        public Async<Cursor> provideAsync() {
          return getOperations().loadCursor();
        }
      };
      restartLoader(1, provider, false);
    }

    // cancellation

    public void cancelLoadCursor() {
      destroyLoader(1);
    }

  }

  /* same visibility */
  static final class Activity$$LoaderDescription extends LoaderDescription {

    Activity$$LoaderDescription(final OperatorBase.OperatorContext<CursorAsyncUserActivity> context) {
      super(context);
    }

    /* same visibility */
    ObserverBuilder<Cursor, Activity$$LoaderDescription> loadCursorIsFinished() {
      return new ObserverBuilder<>(1, this, false);
    }

  }

}
