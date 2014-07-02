package com.stanfy.enroscar.async.content;

import android.content.ContentResolver;
import android.database.Cursor;

import com.stanfy.enroscar.async.internal.TaskAsync;

import java.util.concurrent.Callable;

import static com.stanfy.enroscar.async.content.BaseCursorAsyncBuilder.Params;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
final class ConvertedCursorAsync<D> extends ContentObserverAsync<D, ConvertedCursorAsync.LoadTask<D>> {

  public ConvertedCursorAsync(final Params params, final CursorConverter<D> converter,
                              final ContentResolver resolver) {
    this(new LoadTask<>(params, converter, resolver));
  }

  private ConvertedCursorAsync(final LoadTask<D> task) {
    super(task);
  }

  @Override
  public ConvertedCursorAsync<D> replicate() {
    return new ConvertedCursorAsync<>(getTask());
  }

  @Override
  protected ContentResolver getResolver() {
    return getTask().resolver;
  }

  @Override
  protected Params getParams() {
    return getTask().params;
  }

  /** Loads a cursor and creates an object. */
  static class LoadTask<D> implements Callable<D> {

    final Params params;
    final CursorConverter<D> converter;
    final ContentResolver resolver;

    LoadTask(final Params params, final CursorConverter<D> converter,
             final ContentResolver resolver) {
      this.params = params;
      this.converter = converter;
      this.resolver = resolver;
    }

    @Override
    public D call() {
      Cursor cursor = new ContentProviderQuery(resolver, params).call();
      if (cursor == null) {
        throw new IllegalStateException("Content resolver didn't respond to URI " + params.uri);
      }
      try {
        return converter.toObject(cursor);
      } finally {
        cursor.close();
      }
    }

  }

}
