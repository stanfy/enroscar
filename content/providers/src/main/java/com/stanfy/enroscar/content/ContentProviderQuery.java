package com.stanfy.enroscar.content;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import java.util.concurrent.Callable;

/**
 * Makes a query to content provider.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public final class ContentProviderQuery implements Callable<Cursor> {

  /** Resolver instance. */
  private final ContentResolver resolver;

  /** Parameters. */
  private final Params params;

  ContentProviderQuery(final ContentResolver resolver, final Params params) {
    this.resolver = resolver;
    this.params = params;
  }

  @Override
  public Cursor call() {
    return resolver.query(params.uri, params.projection, params.selection, params.selectionArgs,
        params.sort);
  }

  public ContentResolver getResolver() {
    return resolver;
  }

  public Uri getUri() {
    return params.uri;
  }

  /** Base parameters builder. */
  @SuppressWarnings("unchecked")
  abstract static class BaseParamsBuilder<R, BR extends BaseParamsBuilder> {

    /** Context instance. */
    final Context context;

    /** Instance. */
    final Params params = new Params();

    public BaseParamsBuilder(final Context context) {
      this.context = context.getApplicationContext();
    }

    protected Context getContext() {
      return context;
    }

    protected Params getParams() {
      return params;
    }

    public BR uri(final Uri uri) {
      params.uri = uri;
      return (BR) this;
    }

    public BR projection(final String[] projection) {
      params.projection = projection;
      return (BR) this;
    }

    public BR selection(final String selection) {
      params.selection = selection;
      return (BR) this;
    }

    public BR selectionArgs(final String[] selectionArgs) {
      params.selectionArgs = selectionArgs;
      return (BR) this;
    }

    public BR sort(final String sort) {
      params.sort = sort;
      return (BR) this;
    }

    public abstract R get();

  }

  /** Loader builder. */
  public static class Builder extends BaseParamsBuilder<ContentProviderQuery, Builder> {

    public Builder(final Context context) {
      super(context);
    }

    static ContentProviderQuery makeQuery(final Context context, final Params params) {
      if (params.uri == null) {
        throw new IllegalArgumentException("URI is not specified");
      }
      return new ContentProviderQuery(context.getContentResolver(), params);
    }

    @Override
    public ContentProviderQuery get() {
      return makeQuery(context, params);
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

  }

}
