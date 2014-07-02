package com.stanfy.enroscar.async.content;

import android.content.ContentResolver;
import android.database.Cursor;

import com.stanfy.enroscar.async.content.BaseCursorAsyncBuilder.Params;

import java.util.concurrent.Callable;

/**
 * Queries content provider.
 */
final class ContentProviderQuery implements Callable<Cursor> {

  /** Resolver instance. */
  final ContentResolver resolver;

  /** Parameters. */
  final Params params;

  ContentProviderQuery(final ContentResolver resolver, final Params params) {
    this.resolver = resolver;
    this.params = params;
  }

  @Override
  public Cursor call() {
    return resolver.query(params.uri, params.projection,
        params.selection, params.selectionArgs, params.sort);
  }

}
