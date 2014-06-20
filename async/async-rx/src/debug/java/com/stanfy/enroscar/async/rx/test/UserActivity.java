package com.stanfy.enroscar.async.rx.test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.stanfy.enroscar.async.Load;
import com.stanfy.enroscar.async.OperatorBuilder;
import com.stanfy.enroscar.async.Send;
import com.stanfy.enroscar.async.internal.AsyncProvider;
import com.stanfy.enroscar.async.internal.LoaderDescription;
import com.stanfy.enroscar.async.internal.ObservableTools;
import com.stanfy.enroscar.async.internal.OperatorBase;
import com.stanfy.enroscar.async.internal.ObservableAsyncProvider;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Example of Async Rx usage.
 */
public class UserActivity extends FragmentActivity {

  static final int LOADER_LOAD = 1, LOADER_SEND = 2;

  final CountDownLatch loadSync = new CountDownLatch(1);
  final CountDownLatch sendSync = new CountDownLatch(1);

  TextView view;

  boolean completeCalled;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    final TextView view = new TextView(this);
    this.view = view;
    setContentView(view);

    final UserActivityOperator operator = UserActivityOperator.build()
        .operations(this)
        .withinActivity(this)
        .get();

    operator.when().loadThingIsFinished()
        .doOnCompleted(new Action0() {
          @Override
          public void call() {
            completeCalled = true;
          }
        })
        .subscribe(new Action1<Thing>() {
          @Override
          public void call(final Thing thing) {
            view.setText(thing.toString());
            loadSync.countDown();
          }
        });

    operator.when().sendThingIsFinished()
        .subscribe(new Action1<Thing>() {
          @Override
          public void call(final Thing thing) {
            view.setText(view.getText() + thing.toString());
            sendSync.countDown();
          }
        });

    operator.loadThing(1);
    view.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        operator.sendThing(2);
      }
    });
  }

  @Load Observable<Thing> loadThing(final int v) {
    return Observable.from(new Thing(v));
  }

  @Send Observable<Thing> sendThing(final int v) {
    return Observable.from(Executors.newCachedThreadPool().submit(new Callable<Thing>() {
      @Override
      public Thing call() throws Exception {
        return new Thing(v);
      }
    }));
  }

  /** Data type. */
  public static class Thing {

    private final int v;

    public Thing(final int v) {
      this.v = v;
    }

    @Override
    public String toString() {
      return "thing " + v;
    }
  }

  // -------------- GENERATED CODE ----------------

  static final class UserActivityOperator extends OperatorBase<UserActivity, UserActivity$$LoaderDescription> {

    // construction

    UserActivityOperator(final OperatorContext<UserActivity> operatorContext) {
      super(new UserActivity$$LoaderDescription(operatorContext), operatorContext);
    }

    public static OperatorBuilder<UserActivityOperator, UserActivity> build() {
      return new OperatorBuilderBase<UserActivityOperator, UserActivity>() {
        @Override
        protected UserActivityOperator create(final OperatorContext<UserActivity> context) {
          return new UserActivityOperator(context);
        }
      };
    }

    // invocation

    public void loadThing(final int param) {
      AsyncProvider<Thing> provider = new ObservableAsyncProvider<Thing>() {
        @Override
        protected Observable<Thing> provideObservable() {
          return getOperations().loadThing(param);
        }
      };
      initLoader(1, provider, false);
    }
    public void forceLoadThing(final int param) {
      AsyncProvider<Thing> provider = new ObservableAsyncProvider<Thing>() {
        @Override
        protected Observable<Thing> provideObservable() {
          return getOperations().loadThing(param);
        }
      };
      restartLoader(1, provider);
    }
    public void sendThing(final int param) {
      AsyncProvider<Thing> provider = new ObservableAsyncProvider<Thing>() {
        @Override
        protected Observable<Thing> provideObservable() {
          return getOperations().sendThing(param);
        }
      };
      initLoader(2, provider, true);
    }

    // cancellation

    public void cancelLoadThing() {
      destroyLoader(LOADER_LOAD);
    }

    public void cancelSendThing() {
      destroyLoader(LOADER_SEND);
    }

  }

  /* same visibility */
  static final class UserActivity$$LoaderDescription extends LoaderDescription {

    UserActivity$$LoaderDescription(final OperatorBase.OperatorContext<UserActivity> context) {
      super(context);
    }

    /* same visibility */
    Observable<Thing> loadThingIsFinished() {
      return ObservableTools.loaderObservable(LOADER_LOAD, this);
    }

    /* same visibility */
    Observable<Thing> sendThingIsFinished() {
      return ObservableTools.loaderObservable(LOADER_SEND, this);
    }

  }

}
