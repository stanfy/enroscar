package com.stanfy.enroscar.rest.loader;

import java.util.List;

import android.util.Log;

import com.stanfy.enroscar.content.OffsetInfoProvider;
import com.stanfy.enroscar.content.loader.LoadmoreLoader;
import com.stanfy.enroscar.content.ResponseData;
import com.stanfy.enroscar.net.operation.ListRequestBuilder;

/**
 * Request builder loader that can load more data.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 *
 * @param <MT> model type
 * @param <LT> list type
 */
public class LoadMoreListLoader<MT, LT extends List<MT>> extends RequestBuilderLoader<LT> implements LoadmoreLoader {

  /** Incremenetor. */
  private ValueIncrementor offsetIncrementor, limitIncrementor;

  /** Items list. */
  private LT itemsList;

  /** Current offset. */
  private String offset, limit;

  /** Last loaded count. */
  private int lastLoadedCount;

  /** Stop load more state indicator. */
  private boolean stopLoadMore;

  /** Use limit parameter flag. */
  private boolean useLimitFlag;

  public LoadMoreListLoader(final ListRequestBuilder<LT, MT> requestBuilder) {
    super(requestBuilder);
    this.offset = requestBuilder.getOffset();
    this.limit = requestBuilder.getLimit();
  }

  @SuppressWarnings("unchecked")
  @Override
  public ListRequestBuilder<LT, MT> getRequestBuilder() { return (ListRequestBuilder<LT, MT>) super.getRequestBuilder(); }

  public LoadMoreListLoader<MT, LT> setOffsetIncrementor(final ValueIncrementor incrementor) {
    this.offsetIncrementor = incrementor;
    return this;
  }
  public LoadMoreListLoader<MT, LT> setLimitIncrementor(final ValueIncrementor incrementor) {
    this.limitIncrementor = incrementor;
    return this;
  }

  public LoadMoreListLoader<MT, LT> useLimit(final boolean flag) {
    this.useLimitFlag = flag;
    return this;
  }

  /** @return next 'offset' value */
  protected final String nextOffset() {
    if (offsetIncrementor == null) { return String.valueOf(Integer.parseInt(offset) + 1); }
    return offsetIncrementor.nextValue(offset, lastLoadedCount, itemsList != null ? itemsList.size() : 0);
  }
  /** @return next 'limit' value */
  protected final String nextLimit() {
    if (limitIncrementor == null) { return String.valueOf(Integer.parseInt(limit) + 1); }
    return limitIncrementor.nextValue(limit, lastLoadedCount, itemsList != null ? itemsList.size() : 0);
  }

  /**
   * @param offsetInfoProvider one who knows about current position
   * @return whether load more process should be stopped
   */
  protected boolean shouldStopLoading(final OffsetInfoProvider offsetInfoProvider) {
    return !offsetInfoProvider.moreElementsAvailable(offset);
  }

  @Override
  protected ResponseData<LT> onAcceptData(final ResponseData<LT> oldData, final ResponseData<LT> data) {
    final LT list = data.getEntity();
    if (!data.isSuccessful() || list == null) {

      if (data.isSuccessful() && list == null) {
        // FIXME strange case
        Log.e(TAG, "onAcceptData: response is successfull but model is null!");
      }

      // error case
      stopLoadMore = true;
      return data;
    }

    lastLoadedCount = list.size();
    if (lastLoadedCount == 0) {

      stopLoadMore = true;
      if (itemsList != null) {
        //data.setEntity(itemsList);
      }

    } else {

      if (itemsList != null) {
        list.addAll(0, itemsList);
      }
      itemsList = list;

      OffsetInfoProvider oiProvider = null;
      if (data instanceof OffsetInfoProvider) {
        oiProvider = (OffsetInfoProvider) data;
      } else if (list instanceof OffsetInfoProvider) {
        oiProvider = (OffsetInfoProvider) list;
      }
      if (oiProvider != null) {
        stopLoadMore = shouldStopLoading(oiProvider);
      }

    }
    return data;
  }

  @Override
  public void forceLoadMore() {
    final String nOffset = nextOffset();
    if (nOffset == null) {
      if (DEBUG) { Log.d(TAG, "Null offset, cancel loadmore"); }
      stopLoadMore = true;
      return;
    }

    offset = nOffset;
    limit = nextLimit();

    getRequestBuilder().setOffset(offset);
    if (useLimitFlag) {
      getRequestBuilder().setLimit(limit);
    }

    forceLoad();
  }

  @Override
  public boolean moreElementsAvailable() {
    // TODO implement load more offset tracking
    return !stopLoadMore;
  }

  /**
   * Offset/limit incrementor.
   */
  public interface ValueIncrementor {
    String nextValue(String currentValue, int lastLoadedCount, int currentCount);
  }

}
