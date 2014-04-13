package com.stanfy.enroscar.async.test;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.stanfy.enroscar.async.Action;
import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.Load;
import com.stanfy.enroscar.async.OperatorBuilder;
import com.stanfy.enroscar.async.Send;
import com.stanfy.enroscar.async.Tools;
import com.stanfy.enroscar.async.internal.AsyncProvider;
import com.stanfy.enroscar.async.internal.LoaderDescription;
import com.stanfy.enroscar.async.internal.ObserverBuilder;
import com.stanfy.enroscar.async.internal.OperatorBase;

import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;

/**
 * Activity that uses Async.
 */
public final class AsyncUserActivity extends FragmentActivity {

  private static final String STATE_LAZY_LOAD = "ll";

  /** Text view. */
  TextView textViewLoad;
  /** Send data text view. */
  TextView textViewSend;

  /** A getThing. */
  Thing thing;

  private boolean lazyLoad;

  final CountDownLatch loadSync = new CountDownLatch(1);
  final CountDownLatch lazyLoadSync = new CountDownLatch(1);
  final CountDownLatch sendSync = new CountDownLatch(1);

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    if (savedInstanceState != null) {
      lazyLoad = savedInstanceState.getBoolean(STATE_LAZY_LOAD);
    }

    LinearLayout ll = new LinearLayout(this);
    ll.setOrientation(LinearLayout.VERTICAL);

    textViewLoad = new TextView(this);
    textViewSend = new TextView(this);
    ll.addView(textViewLoad);
    ll.addView(textViewSend);

    setContentView(ll);

    final Data operations = new Data("init");
    operations.setSomething(0.5f);
    final DataOperator data = DataOperator.build()
        .operations(operations)
        .withinActivity(this)
        .get();

    data.when().getThingIsFinished()
        .doOnResult(new Action<Thing>() {
          @Override
          public void act(final Thing data) {
            thing = data;
            textViewLoad.setText(textViewLoad.getText() + data.toString());
            if (lazyLoad) {
              lazyLoadSync.countDown();
            } else {
              loadSync.countDown();
            }
          }
        })
        .doOnError(new Action<Throwable>() {
          @Override
          public void act(final Throwable data) {
            throw new AssertionError(data);
          }
        })

        .alsoWhen().sendThingIsFinished()
        .doOnResult(new Action<Thing>() {
          @Override
          public void act(final Thing data) {
            thing = data;
            //noinspection ConstantConditions
            textViewSend.setText(textViewSend.getText().toString() + data);
            sendSync.countDown();
          }
        });


    textViewSend.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        data.sendThing(2);
      }
    });

    textViewLoad.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        lazyLoad = true;
        data.forceGetThing(3);
      }
    });

    data.getThing(1);
  }

  @Override
  protected void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putBoolean(STATE_LAZY_LOAD, lazyLoad);
  }

  /** Loader. */
  static class Data {

    private final String initParam;
    private float setParam;

    Data(final String initParam) {
      this.initParam = initParam;
    }

    public void setSomething(final float param) {
      setParam = param;
    }

    @Load Async<Thing> getThing(final int param) {
      return getAsync(param);
    }

    @Send Async<Thing> sendThing(final int param) {
      return getAsync(param);
    }

    private Async<Thing> getAsync(final int param) {
      return Tools.async(new Callable<Thing>() {
        @Override
        public Thing call() throws Exception {
          return new Thing(initParam, setParam, param);
        }
      });
    }
  }

  /** Something that is loaded. */
  public static class Thing {
    final String str;
    final float fl;

    /** Parameter. */
    final int param;

    public Thing(final String str, float fl, final int param) {
      this.str = str;
      this.fl = fl;
      this.param = param;
    }

    @Override
    public String toString() {
      return "a thing: " + param + "/" + str + "/" + fl;
    }
  }

  // -------------- GENERATED CODE ----------------

  static final class DataOperator extends OperatorBase<Data, Data$$LoaderDescription> {

    // construction

    DataOperator(final OperatorContext<Data> operatorContext) {
      super(new Data$$LoaderDescription(operatorContext), operatorContext);
    }

    public static OperatorBuilder<DataOperator, Data> build() {
      return new OperatorBuilderBase<DataOperator, Data>() {
        @Override
        protected DataOperator create(final OperatorContext<Data> context) {
          return new DataOperator(context);
        }
      };
    }

    // invocation

    public void getThing(final int param) {
      AsyncProvider<Thing> provider = new AsyncProvider<Thing>() {
        @Override
        public Async<Thing> provideAsync() {
          return getOperations().getThing(param);
        }
      };
      initLoader(1, provider, false);
    }
    public void forceGetThing(final int param) {
      AsyncProvider<Thing> provider = new AsyncProvider<Thing>() {
        @Override
        public Async<Thing> provideAsync() {
          return getOperations().getThing(param);
        }
      };
      restartLoader(1, provider);
    }
    public void sendThing(final int param) {
      AsyncProvider<Thing> provider = new AsyncProvider<Thing>() {
        @Override
        public Async<Thing> provideAsync() {
          return getOperations().sendThing(param);
        }
      };
      initLoader(2, provider, true);
    }

    // cancellation

    public void cancelGetThing() {
      destroyLoader(1);
    }

    public void cancelSendThing() {
      destroyLoader(2);
    }

  }

  /* same visibility */
  static final class Data$$LoaderDescription extends LoaderDescription {

    Data$$LoaderDescription(final OperatorBase.OperatorContext<Data> context) {
      super(context);
    }

    /* same visibility */
    ObserverBuilder<Thing, Data$$LoaderDescription> getThingIsFinished() {
      return new ObserverBuilder<>(1, this);
    }

    /* same visibility */
    ObserverBuilder<Thing, Data$$LoaderDescription> sendThingIsFinished() {
      return new ObserverBuilder<>(2, this);
    }

  }

}
