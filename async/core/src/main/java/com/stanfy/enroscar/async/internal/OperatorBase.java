package com.stanfy.enroscar.async.internal;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;

import com.stanfy.enroscar.async.OperatorBuilder;

import static com.stanfy.enroscar.async.internal.Utils.MAIN_THREAD_HANDLER;

/**
 * Base class for generated operators.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public abstract class OperatorBase<W, D extends LoaderDescription> {

  /** Callbacks description. */
  private final D description;

  /** Operator context. */
  private final OperatorContext<W> operatorContext;

  /** Check what loaders are started, invoke initLoader for them. */
  private final Runnable checkStartedLoaders = new Runnable() {
    @Override
    public void run() {
      description.initStartedLoaders();
    }
  };

  protected OperatorBase(final D description,
                        final OperatorContext<W> operatorContext) {
    this.operatorContext = operatorContext;
    this.description = description;
  }

  public final D when() {
    MAIN_THREAD_HANDLER.post(checkStartedLoaders);
    return description;
  }

  protected final W getOperations() {
    return operatorContext.getOperations();
  }

  protected final void initLoader(final int loaderId, final AsyncProvider<?> provider,
                                  final boolean destroyOnFinish) {
    Utils.initLoader(operatorContext, loaderId, provider, destroyOnFinish, description);
  }

  protected final void restartLoader(final int loaderId, final AsyncProvider<?> provider,
                                     final boolean destroyOnFinish) {
    description.invokeStartAction(loaderId);
    LoaderManager lm = operatorContext.loaderManager;
    lm.restartLoader(
        loaderId,
        null,
        description.makeCallbacks(loaderId, provider, destroyOnFinish)
    );
  }

  protected final void destroyLoader(final int loaderId) {
    LoaderManager lm = operatorContext.loaderManager;
    lm.destroyLoader(loaderId);
  }

  /**
   * @param <T> operations object type (where {@code @Load} and {@code @Send} methods are defined)
   */
  public static final class OperatorContext<T> {

    T operations;
    Context context;
    LoaderManager loaderManager;

    OperatorContext() {  }

    void validate() {
      if (context == null) {
        throw new IllegalStateException("Context is not defined");
      }
      if (loaderManager == null) {
        throw new IllegalStateException("Loader manager is not defined");
      }
      if (operations == null) {
        throw new IllegalStateException("Operations object is not defined");
      }
    }

    public T getOperations() {
      return operations;
    }
  }

  /**
   * Base implementation of {@link com.stanfy.enroscar.async.OperatorBuilder}.
   * Generated code has to implement {@link #create(OperatorBase.OperatorContext)} method only.
   */
  public abstract static class OperatorBuilderBase<T, W> implements OperatorBuilder<T, W> {

    /** Context for the built object. */
    private final OperatorContext<W> operatorContext = new OperatorContext<>();

    @Override
    public final OperatorBuilder<T, W> operations(final W operations) {
      this.operatorContext.operations = operations;
      return this;
    }

    @Override
    public final OperatorBuilder<T, W> loaderManager(final LoaderManager loaderManager) {
      this.operatorContext.loaderManager = loaderManager;
      return this;
    }

    @Override
    public final OperatorBuilder<T, W> context(final Context context) {
      this.operatorContext.context = context;
      return this;
    }

    @Override
    public final OperatorBuilder<T, W> withinActivity(final FragmentActivity activity) {
      this.operatorContext.context = activity;
      this.operatorContext.loaderManager = activity.getSupportLoaderManager();
      return this;
    }

    @Override
    public final OperatorBuilder<T, W> withinFragment(final Fragment fragment) {
      this.operatorContext.context = fragment.getActivity();
      this.operatorContext.loaderManager = fragment.getLoaderManager();
      return this;
    }

    @Override
    public final T get() {
      operatorContext.validate();
      return create(operatorContext);
    }

    /**
     * @return operator instance
     */
    protected abstract T create(final OperatorContext<W> operatorContext);

  }
}
