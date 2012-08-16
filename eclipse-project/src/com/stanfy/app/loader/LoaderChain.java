package com.stanfy.app.loader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.util.SparseIntArray;


/**
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
// TODO write test
public final class LoaderChain extends Loader<Object[]> {

  /** Index. */
  Map<Loader<?>, Integer> loaderIndexMapping;

  /** Array of core loaders. */
  private Description[] descriptions;

  /** Results. */
  private final Object[] results;

  /** Results counter. */
  private int resultsCounter = 0;

  /** Loader manager. */
  private final LoaderManager loaderManager;

  /** Arguments. */
  private Bundle arguments;

  /** Loader builder. */
  public static class Builder {
    /** Context. */
    private final Context context;
    /** Descriptions. */
    private final ArrayList<Description> descriptions = new ArrayList<LoaderChain.Description>();

    /** Loader manager. */
    private LoaderManager loaderManager;

    /** Arguments. */
    private Bundle arguments;

    /** Internal counter. */
    private int counter = 0;

    Builder(final Context context) {
      this.context = context;
    }

    public Builder withArguments(final Bundle arguments) {
      this.arguments = arguments;
      return this;
    }

    public Builder withManager(final LoaderManager loaderManaer) {
      this.loaderManager = loaderManaer;
      return this;
    }

    /**
     * @param callbacks loader callbacks instance
     * @return identifiers of loaders processed by the defined callbacks
     */
    public Builder withCallbacks(final LoaderCallbacks<?> callbacks, final int... ids) {
      descriptions.add(new Description(callbacks, ids, counter));
      counter += ids.length;
      return this;
    }

    /** @return loader chain instance */
    public LoaderChain create() {
      final Description[] desc = new Description[descriptions.size()];
      return new LoaderChain(context, descriptions.toArray(desc), counter, loaderManager, arguments);
    }
  }

  private LoaderChain(final Context context, final Description[] desc, final int totalCount, final LoaderManager loaderManager, final Bundle arguments) {
    super(context);
    if (desc == null || desc.length == 0) { throw new IllegalArgumentException("Loaders are not provided"); }
    this.descriptions = desc;
    for (final Description d : desc) { d.attach(this); }

    this.arguments = arguments;
    this.results = new Object[totalCount];

    this.loaderManager = loaderManager;

    this.loaderIndexMapping = new HashMap<Loader<?>, Integer>();
  }

  /**
   * Start loader building.
   * @param context context instance
   * @return builder for constructing a chain
   */
  public static Builder build(final Context context) { return new Builder(context); }

  public static LoaderChain init(final LoaderManager loaderManager, final int id, final Bundle arguments, final LoaderCallbacks<Object[]> callbacks) {
    final LoaderChain present = (LoaderChain) loaderManager.<Object[]>getLoader(id);
    final LoaderChain result = (LoaderChain) loaderManager.restartLoader(id, arguments, callbacks);
    if (present != null) {
      result.loaderIndexMapping = present.loaderIndexMapping;
    }
    return result;
  }

  @Override
  protected void onStartLoading() {
    forceLoad();
  }

  @Override
  protected void onForceLoad() {
    resultsCounter = 0;
    final Description[] descriptions = this.descriptions;
    for (final Description desc : descriptions) {
      for (final int id : desc.ids) {
        loaderManager.initLoader(id, arguments, desc.callbacks);
      }
    }
  }

  void onLoadFinished(final Loader<Object> loader, final Object data) {
    final int index = loaderIndexMapping.get(loader);
    results[index] = data;
    if (resultsCounter < results.length) {
      resultsCounter++;
    }

    if (resultsCounter == results.length && isStarted()) {
      if (isStarted()) {
        deliverResult(results);
      }
    }
  }

  void onLoaderReset(final Loader<Object> loader) {
    final Integer index = loaderIndexMapping.get(loader);
    if (index != null) {
      results[index] = null;
      loaderIndexMapping.remove(loader);
    }
  }

  /** Another callbacks wrapper. */
  @SuppressWarnings({ "rawtypes", "unchecked" })
  private static class CallbacksWrapper implements LoaderCallbacks<Object> {

    /** Another callbacks. */
    private LoaderCallbacks another;

    /** IDs mapping. */
    private SparseIntArray idsMapping;

    /** Chain instance. */
    LoaderChain chain;

    public CallbacksWrapper(final LoaderCallbacks<?> another, final SparseIntArray idsMapping) {
      this.another = another;
      this.idsMapping = idsMapping;
    }

    @Override
    public Loader onCreateLoader(final int id, final Bundle args) {
      final Loader result = another.onCreateLoader(id, args);
      if (result != null) {
        chain.loaderIndexMapping.put(result, idsMapping.get(id));
      }
      return result;
    }

    @Override
    public void onLoadFinished(final Loader<Object> loader, final Object data) {
      another.onLoadFinished(loader, data);
      chain.onLoadFinished(loader, data);
    }

    @Override
    public void onLoaderReset(final Loader<Object> loader) {
      another.onLoaderReset(loader);
      chain.onLoaderReset(loader);
    }

  }

  /** Another loader description. */
  private static class Description {
    /** Callbacks. */
    final CallbacksWrapper callbacks;
    /** Loader identifiers. */
    final int[] ids;

    Description(final LoaderCallbacks<?> callbacks, final int[] ids, final int startIndex) {
      final SparseIntArray indecies = new SparseIntArray(ids.length);

      for (int i = 0; i < ids.length; i++) {
        indecies.put(ids[i], startIndex + i);
      }

      this.callbacks = new CallbacksWrapper(callbacks, indecies);
      this.ids = ids;
    }

    public void attach(final LoaderChain instance) {
      callbacks.chain = instance;
    }
  }

  /**
   * Adapter to simplify laoder callbacks description.
   * @param <D> data type
   */
  public abstract static class ChainCallbacksAdapter<D> implements LoaderCallbacks<D> {

    @Override
    public void onLoadFinished(final Loader<D> loader, final D data) {
      // nothing
    }

    @Override
    public void onLoaderReset(final Loader<D> loader) {
      // nothing
    }

  }

}
