package com.stanfy.enroscar.async.content;

import android.content.ContentResolver;
import android.database.Cursor;

import com.stanfy.enroscar.async.Async;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder for {@link com.stanfy.enroscar.async.Async} that loads a {@link Cursor}.
 * Built operation will query a content provider and register an observer for the specified uri.
 * Subscribers will receive {@link com.stanfy.enroscar.async.AsyncObserver#onResult(Object)}
 * each time the content resolver is notified about changes.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public final class CursorAsyncBuilder extends BaseCursorAsyncBuilder<CursorAsyncBuilder, Cursor> {

  public CursorAsyncBuilder(final ContentResolver resolver) {
    super(resolver);
  }

  /**
   * Wrap a provided converter.
   * Returns a converter that is equivalent to
   * <pre>{@code
   *   return cursor.moveToNext()
   *       ? converter.toObject(cursor)
   *       : null;
   * }</pre>
   */
  public static <D> CursorConverter<D> singleRecordConverter(final CursorConverter<D> converter) {
    return new SingleRecordConverter<>(converter);
  }

  /**
   * Wrap a provided converter.
   * Returns a converter that creates of list of elements obtained with a provider converter.
   */
  public static <D> CursorConverter<List<D>> listConverter(final CursorConverter<D> converter) {
    return new ListConverter<>(converter);
  }

  /**
   * Convert {@code Cursor} into a POJO using a {@code CursorConverter}.
   */
  public <D> BaseCursorAsyncBuilder<? extends BaseCursorAsyncBuilder<?, D>, D> convert(
      final CursorConverter<D> converter) {
    return new ConvertedCursorAsyncBuilder<>(resolver, converter, params);
  }

  /**
   * Convert {@code Cursor} into a POJO using a {@code CursorConverter}.
   * Only the first cursor record will be converted. Provided {@code CursorConverter} should not
   * invoke any {@code move} methods on a cursor.
   */
  public <D> BaseCursorAsyncBuilder<? extends BaseCursorAsyncBuilder<?, D>, D> convertFirst(
      final CursorConverter<D> converter) {
    return convert(singleRecordConverter(converter));
  }

  /**
   * Convert {@code Cursor} into a {@code List} creating each item with
   * a provided {@code CursorConverter}.
   * Provided {@code CursorConverter} should not invoke any {@code move} methods on a cursor.
   */
  public <D> BaseCursorAsyncBuilder<? extends BaseCursorAsyncBuilder<?, List<D>>, List<D>>
      convertList(final CursorConverter<D> converter) {
    return convert(listConverter(converter));
  }

  @Override
  public Async<Cursor> get() {
    return new CursorAsync(new ContentProviderQuery(resolver, params), executor);
  }

  /** Converts a single record cursor to an object. */
  private static class SingleRecordConverter<D> implements CursorConverter<D> {
    private final CursorConverter<D> delegate;

    SingleRecordConverter(CursorConverter<D> delegate) {
      this.delegate = delegate;
    }

    @Override
    public D toObject(Cursor cursor) {
      return cursor.moveToNext()
          ? delegate.toObject(cursor)
          : null;
    }
  }

  /** Converts a cursor to a list. */
  private static class ListConverter<D> implements CursorConverter<List<D>> {
    private final CursorConverter<D> delegate;

    ListConverter(final CursorConverter<D> delegate) {
      this.delegate = delegate;
    }

    @Override
    public List<D> toObject(final Cursor cursor) {
      int count = cursor.getCount();
      ArrayList<D> result = new ArrayList<>(count);
      if (count == 0) {
        return result;
      }

      while (cursor.moveToNext()) {
        result.add(delegate.toObject(cursor));
      }

      return result;
    }
  }

  /** Converted cursor builder. */
  static final class ConvertedCursorAsyncBuilder<D>
      extends BaseCursorAsyncBuilder<ConvertedCursorAsyncBuilder<D>, D> {

    private final CursorConverter<D> converter;

    public ConvertedCursorAsyncBuilder(final ContentResolver resolver,
                                       final CursorConverter<D> converter,
                                       final Params params) {
      super(resolver, params);
      this.converter = converter;
    }

    @Override
    public Async<D> get() {
      if (converter == null) {
        throw new IllegalStateException("Converter is not defined");
      }
      return new ConvertedCursorAsync<>(params, converter, resolver, executor);
    }

  }

}
