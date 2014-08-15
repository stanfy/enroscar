package com.stanfy.enroscar.async.internal;

import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.LoaderManager;

import static com.stanfy.enroscar.async.internal.OperatorBase.OperatorContext;

/**
 * Internal utilities.
 */
public final class Utils {

  /** Main thread handler. */
  public static final Handler MAIN_THREAD_HANDLER = new Handler(Looper.getMainLooper());

  private Utils() { }

  static void initLoader(final OperatorContext<?> operatorContext, final int loaderId,
                         final AsyncProvider<?> provider, final boolean destroyOnFinish,
                         final LoaderDescription<?> description) {
    description.invokeStartAction(loaderId);
    LoaderManager lm = operatorContext.loaderManager;
    lm.initLoader(loaderId, null, description.makeCallbacks(loaderId, provider, destroyOnFinish));
  }

}
