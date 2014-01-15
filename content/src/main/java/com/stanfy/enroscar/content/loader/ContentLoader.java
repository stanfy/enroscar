package com.stanfy.enroscar.content.loader;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

/**
 * Loads data via content provider.
 */
public class ContentLoader<T> extends BaseAsyncTaskLoader<ResponseData<T>> {

  /** Parameters. */
  private final Params params;

  /** Factory instance. */
  private DataInstanceFactory<T> factory;

  /** Content observer. */
  private Observer contentObserver;

  /** Processor. */
  private PostProcessor<T> postProcessor;

  /** Error handler. */
  private ErrorHandler errorHandler;

  /** Error instance. */
  private Throwable error;

  ContentLoader(final Context context, final Params params) {
    super(context);
    this.params = params;
  }

  public static <T> Builder<T> of(final DataInstanceFactory<T> factory) {
    return new Builder<T>(factory);
  }

  @Override
  public ResponseData<T> loadInBackground() {
    ContentResolver resolver = getContext().getContentResolver();

    Cursor cursor = resolver.query(params.uri, params.projection, params.selection, params.selectionArgs, params.sort);

    if (cursor == null) {
      throw new IllegalStateException("Content provider hasn't responded to " + params.uri);
    }

    synchronized (this) {
      if (contentObserver == null) {
        contentObserver = new Observer();
        resolver.registerContentObserver(params.uri, params.observeDescendentsChanges, contentObserver);
      }
    }

    try {

      T data = factory.createWithCursor(cursor);
      if (postProcessor != null) {
        data = postProcessor.process(getContext(), data);
      }
      return new ResponseData<T>(data);

    } catch (SQLiteException e) {

      this.error = e;

      ResponseData<T> result = new ResponseData<T>();
      result.setErrorCode(params.errorCode);
      result.setMessage(params.errorMessage);
      return result;

    } finally {
      cursor.close();
    }
  }

  @Override
  public void deliverResult(final ResponseData<T> data) {
    if (isReset()) {
      ensureContentObserverUnregistered();
      return;
    }

    if (error != null && errorHandler != null) {
      errorHandler.handleError(error);
    }

    super.deliverResult(data);
  }

  @Override
  protected void onReset() {
    super.onReset();
    ensureContentObserverUnregistered();
  }

  private void ensureContentObserverUnregistered() {
    synchronized (this) {
      if (contentObserver != null) {
        getContext().getContentResolver().unregisterContentObserver(contentObserver);
        contentObserver = null;
      }
    }
  }

  /** Content observer. */
  private class Observer extends ContentObserver {
    public Observer() {
      super(new Handler(Looper.getMainLooper()));
    }

    @Override
    public boolean deliverSelfNotifications() {
      return true;
    }

    @Override
    public void onChange(final boolean selfChange) {
      onContentChanged();
    }

  }

  /** Converts cursor representation to some other model. */
  public static interface DataInstanceFactory<T> {
    T createWithCursor(Cursor cursor);
  }

  /** Loader builder. */
  public static class Builder<T> {

    /** Instance. */
    private final Params params = new Params();

    /** Factory. */
    private final DataInstanceFactory<T> factory;

    /** 'After' processor. */
    private PostProcessor<T> after;

    /** Error handler. */
    private ErrorHandler errorHandler;

    Builder(final DataInstanceFactory<T> factory) {
      if (factory == null) { throw new NullPointerException(); }
      this.factory = factory;
    }

    public Builder<T> uri(final Uri uri) {
      params.uri = uri;
      return this;
    }

    public Builder<T> projection(final String[] projection) {
      params.projection = projection;
      return this;
    }

    public Builder<T> selection(final String selection) {
      params.selection = selection;
      return this;
    }

    public Builder<T> selectionArgs(final String[] selectionArgs) {
      params.selectionArgs = selectionArgs;
      return this;
    }

    public Builder<T> sort(final String sort) {
      params.sort = sort;
      return this;
    }

    public Builder<T> observeDescendents(final boolean value) {
      params.observeDescendentsChanges = value;
      return this;
    }

    /**
     * Set error code that should be set to ResponseData if error happens.
     */
    public Builder<T> errorCode(final int errorCode) {
      params.errorCode = errorCode;
      return this;
    }

    /**
     * Set error message that should be set to ResponseData if error happens.
     */
    public Builder<T> errorMessage(final String message) {
      params.errorMessage = message;
      return this;
    }

    public Builder<T> after(final PostProcessor<T> processor) {
      this.after = processor;
      return this;
    }

    public Builder<T> after(final ErrorHandler handler) {
      this.errorHandler = handler;
      return this;
    }

    public ContentLoader<T> get(final Context context) {
      if (params.uri == null) {
        throw new IllegalArgumentException("URI is not specified");
      }
      ContentLoader<T> loader = new ContentLoader<T>(context, params);
      loader.factory = factory;
      loader.postProcessor = after;
      loader.errorHandler = errorHandler;
      return loader;
    }

  }

  /** Loader params. */
  static class Params {

    /** Request URI. */
    Uri uri;

    /** Projection. */
    String[] projection;

    /** Selection. */
    String selection;

    /** Selection arguments. */
    String[] selectionArgs;

    /** Sort order. */
    String sort;

    /** Error message. */
    String errorMessage;

    /** Error code. */
    int errorCode = -1;

    /** Whether to observe changes in descendent URIs. */
    boolean observeDescendentsChanges;

  }

  /** Post loading processor (for background thread). */
  public interface PostProcessor<T> {
    T process(Context context, T data);
  }

  /** Error handler (invoked in the main thread). */
  public interface ErrorHandler {
    void handleError(Throwable e);
  }

}
