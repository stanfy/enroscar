package com.stanfy.views.list;


import java.io.Serializable;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.stanfy.content.OffsetInfoTag;
import com.stanfy.content.TaggedArrayList;
import com.stanfy.content.UniqueObject;
import com.stanfy.serverapi.request.ListRequestBuilder;
import com.stanfy.serverapi.response.ResponseData;

/**
 * @param <T> model type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class Fetcher<T extends UniqueObject> extends RequestBuilderAdapter<T, ListRequestBuilder> {

  /** Logging tag. */
  static final String TAG = "Fetcher";

  /** Default page size. */
  private static final int DEFAULT_PAGE_SIZE = 20;

  /** Page size. */
  final int pageSize;

  /** Client offset tracking mode flag. */
  final boolean clientOffsetMode;

  public Fetcher(final Context context, final ElementRenderer<T> renderer, final int token) {
    this(context, 0, DEFAULT_PAGE_SIZE, renderer, token);
  }
  public Fetcher(final Context context, final ElementRenderer<T> renderer, final int token, final boolean clientOffsetMode) {
    this(context, 0, DEFAULT_PAGE_SIZE, renderer, token, clientOffsetMode);
  }
  public Fetcher(final Context context, final int offset, final int pageSize, final ElementRenderer<T> renderer, final int token) {
    this(context, offset, pageSize, renderer, token, true);
  }
  public Fetcher(final Context context, final int offset, final int pageSize, final ElementRenderer<T> renderer, final int token, final boolean clientOffsetMode) {
    super(context, renderer, token);
    this.pageSize = pageSize;
    ((FetcherState)state).offset = offset;
    this.clientOffsetMode = clientOffsetMode;
  }
  public Fetcher(final Fetcher<T> fetcher) {
    super(fetcher);
    this.pageSize = fetcher.pageSize;
    this.clientOffsetMode = fetcher.clientOffsetMode;
  }

  /** @param requestBuilder the requestBuilder to set */
  @Override
  public void setRequestBuilder(final ListRequestBuilder requestBuilder, final boolean forceClear) {
    requestBuilder.setLimit(pageSize);
    super.setRequestBuilder(requestBuilder, forceClear);
  }

  /** @return the requestBuilder */
  public ListRequestBuilder getRequestBuilder() { return requestBuilder; }

  @Override
  public void clear() {
    super.clear();
    ((FetcherState)state).offset = 0;
    ((FetcherState)state).hasMoreElements = true;
  }

  protected void setupRequestBuilderOffset() {
    requestBuilder.setOffset(((FetcherState)state).offset);
    ((FetcherState)state).offset += pageSize;
  }

  @Override
  public void loadMoreRecords() {
    if (requestBuilder == null) { return; }
    if (DEBUG) { Log.d(TAG, "Load more " + ((FetcherState)state).offset + ", " + pageSize); }
    setupRequestBuilderOffset();
    super.loadMoreRecords();
  }

  @Override
  public boolean moreElementsAvailable() { return ((FetcherState)state).hasMoreElements; }

  @Override
  public void replace(final ArrayList<T> list) {
    super.replace(list);
    if (DEBUG) { Log.d(TAG, "Replace fetcher elements"); }
    //((State)state).offset = list.size();
  }

  /** @return the offset */
  public int getOffset() { return ((FetcherState)state).offset; }

  @Override
  protected State createState() { return new FetcherState(); }

  /**
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public static class FetcherState extends RequestBuilderAdapter.State {
    /** Current offset. */
    int offset;
  }

  /**
   * Fetcher request callback.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public static class FetcherRequestCallback<MT extends UniqueObject> extends RBAdapterCallback<MT, ListRequestBuilder, Fetcher<MT>> {

    /** Fixed page size flag. */
    boolean fixedPageSize = false;

    @Override
    public void setList(final FetchableListView list) {
      super.setList(list);
      fixedPageSize = !(adapter instanceof PageFetcher);
    }

    protected boolean decideAboutMoreElements(@SuppressWarnings("rawtypes") final ArrayList model) {
      if (model == null || model.size() == 0) { return false; }
      if (fixedPageSize) { return model.size() < adapter.pageSize; }
      if (model instanceof TaggedArrayList) {
        final TaggedArrayList<?> list = (TaggedArrayList<?>)model;
        final Serializable tag = list.getTag();
        if (tag != null && tag instanceof OffsetInfoTag) {
          final OffsetInfoTag info = (OffsetInfoTag)tag;
          final int max = info.getMaxOffsetCount();
          final int offset = adapter.clientOffsetMode ? ((FetcherState)adapter.state).offset : info.getCurrentOffset();
          if (DEBUG) { Log.d(TAG, "decideAboutMoreElements: " + offset + " vs " + max); }
          if (max > 0 && offset >= max) { return false; }
        }
      }
      return true;
    }

    @Override
    protected void processSuccess(final int token, final int operation, final ResponseData responseData, @SuppressWarnings("rawtypes") final ArrayList model) {
      super.processSuccess(token, operation, responseData, model);
      adapter.state.hasMoreElements = decideAboutMoreElements(model);
    }

  }

}
