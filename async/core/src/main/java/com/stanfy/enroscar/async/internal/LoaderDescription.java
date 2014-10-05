package com.stanfy.enroscar.async.internal;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.SparseArray;

import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.AsyncObserver;
import com.stanfy.enroscar.async.internal.OperatorBase.OperatorContext;
import com.stanfy.enroscar.async.internal.WrapAsyncLoader.Result;

/**
 * Describes what actions should be taken when some operation completes.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public abstract class LoaderDescription<T extends LoaderDescription<?>> {

  /** Stub provider used to init already started loaders. */
  private static final AsyncProvider<?> STUB_PROVIDER = new AsyncProvider<Object>() {
    @Override
    public Async<Object> provideAsync() {
      throw new IllegalStateException("Loader started without providing an Async");
    }
  };

  /** Observers mapping: loader id <-> associated structure (observer, start action, options). */
  private final SparseArray<LoaderCookies> listenersMap = new SparseArray<>();

  private final OperatorContext<?> operatorContext;

  protected LoaderDescription(final OperatorContext<?> operatorContext) {
    this.operatorContext = operatorContext;
  }

  @SuppressWarnings("unchecked")
  public final T alsoWhen() {
    return (T) this;
  }

  <D> void addObserver(final int loaderId, final AsyncObserver<D> observer,
                       final boolean destroyOnFinish) {
    LoaderCookies listener = getListeners(loaderId);
    listener.observer = observer;
    if (destroyOnFinish) {
      listener.options |= LoaderCookies.DESTROY_ON_FINISH;
    } else {
      listener.options &= ~LoaderCookies.DESTROY_ON_FINISH;
    }
  }

  protected void addStartAction(final int loaderId, final Runnable action) {
    getListeners(loaderId).startAction = action;
  }

  <D> LoaderManager.LoaderCallbacks<Result<D>> makeCallbacks(final int loaderId,
                                                             final AsyncProvider<D> provider,
                                                             final boolean destroyOnFinish) {
    return new ObserverCallbacks<>(provider, operatorContext, this, loaderId, destroyOnFinish);
  }

  @SuppressWarnings("unchecked")
  <D> AsyncObserver<D> getObserver(final int id) {
    return (AsyncObserver<D>) getListeners(id).observer;
  }

  void invokeStartAction(final int id) {
    LoaderCookies cookies = getListeners(id);
    cookies.options |= LoaderCookies.ALREADY_INIT;
    if (cookies.startAction != null) {
      cookies.startAction.run();
    }
  }

  /**
   * Initialize already started loaders.
   */
  void initStartedLoaders() {
    LoaderManager lm = operatorContext.loaderManager;
    SparseArray<LoaderCookies> listenersMap = this.listenersMap;

    int count = listenersMap.size();
    for (int i = 0; i < count; i++) {
      int loaderId = listenersMap.keyAt(i);
      Loader<?> loader = lm.getLoader(loaderId);
      if (loader != null && loader.isStarted()) {
        LoaderCookies cookies = listenersMap.valueAt(i);
        boolean notInit = (cookies.options & LoaderCookies.ALREADY_INIT) == 0;
        if (notInit) {
          Utils.initLoader(
              operatorContext,
              loaderId,
              STUB_PROVIDER,
              (cookies.options & LoaderCookies.DESTROY_ON_FINISH) == LoaderCookies.DESTROY_ON_FINISH,
              this
          );
        }
      }
    }
  }

  private LoaderCookies getListeners(final int id) {
    LoaderCookies l = listenersMap.get(id);
    if (l == null) {
      l = new LoaderCookies();
      listenersMap.put(id, l);
    }
    return l;
  }

  /**
   * Structure associated with a loader.
   */
  private static final class LoaderCookies {

    static final int DESTROY_ON_FINISH = 1;
    static final int ALREADY_INIT = 2;

    /** Async observer. Listens for finish/reset events. */
    AsyncObserver<?> observer;
    /** Action to invoke on start event. */
    Runnable startAction;
    /** Bit set of options. */
    int options;
  }

}
