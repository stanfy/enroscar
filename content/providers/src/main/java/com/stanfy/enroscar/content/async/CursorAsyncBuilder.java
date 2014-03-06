package com.stanfy.enroscar.content.async;

import android.content.Context;
import android.database.Cursor;

import com.stanfy.enroscar.content.async.internal.CursorAsync;

import static com.stanfy.enroscar.content.async.ContentProviderQuery.BaseParamsBuilder;
import static com.stanfy.enroscar.content.async.ContentProviderQuery.Builder.makeQuery;

/**
 * Builder for {@link Async} that loads a {@link Cursor}
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public final class CursorAsyncBuilder extends BaseParamsBuilder<Async<Cursor>, CursorAsyncBuilder> {

  /** Observation flag. */
  private boolean observeDescendants;

  CursorAsyncBuilder(final Context context) {
    super(context);
  }

  public CursorAsyncBuilder observeDescendants(final boolean value) {
    this.observeDescendants = value;
    return this;
  }

  @Override
  public Async<Cursor> get() {
    return new CursorAsync(makeQuery(context, params), observeDescendants);
  }

}
