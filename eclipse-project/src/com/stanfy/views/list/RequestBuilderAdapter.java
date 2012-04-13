package com.stanfy.views.list;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.app.Application.CrucialGUIOperationListener;
import com.stanfy.content.UniqueObject;
import com.stanfy.serverapi.request.Operation;
import com.stanfy.serverapi.request.RequestBuilder;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.utils.ApiMethodsSupport.ApiSupportRequestCallback;

/**
 * @param <MT> model type
 * @param <RBT> request builder type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public abstract class RequestBuilderAdapter<MT extends UniqueObject, RBT extends RequestBuilder> extends ModelListAdapter<MT> implements FetchableListAdapter, CrucialGUIOperationListener {

  /** Default states. */
  public static final int STATE_NOTHING_FOUND = 0, STATE_CONNECTION_ERROR = 1;

  /** Logging tag. */
  static final String TAG = "RBAdapter";

  /** Debug flag. */
  static final boolean DEBUG = DebugFlags.DEBUG_GUI;

  /** Request builder. */
  RBT requestBuilder;

  /** State. */
  State state = createState();

  /** Busy flag. */
  private volatile boolean busy = false;

  /** Token. */
  final int token;

  /** Pending operations. */
  private final ArrayList<Runnable> afterAnimationOperations = new ArrayList<Runnable>(3);
  /** Animations running flag. */
  private boolean animationsRunning = false;

  public RequestBuilderAdapter(final Context context, final ModelListAdapter.ElementRenderer<MT> renderer, final int token) {
    super(context, renderer);
    this.token = token;
  }

  public RequestBuilderAdapter(final RequestBuilderAdapter<MT, RBT> requestBuilder) {
    super(requestBuilder);
    this.token = requestBuilder.token;
    this.state = requestBuilder.getState();
  }

  /**
   * @param requestBuilder the requestBuilder to set
   * @param forceClear flag that indicates that clearing is required
   */
  public void setRequestBuilder(final RBT requestBuilder, final boolean forceClear) {
    this.requestBuilder = requestBuilder;
    if (forceClear) { clear(); }
  }

  @Override
  public boolean isBusy() { return busy || requestBuilder == null; }
  /** @param busy the busy to set */
  protected void setBusy(final boolean busy) { this.busy = busy; }

  @Override
  public void loadMoreRecords() {
    if (requestBuilder == null) { return; }
    busy = true;
    requestBuilder.execute(token);
  }

  public Operation getOperation() { return requestBuilder == null ? null : requestBuilder.getOperation(); }

  @Override
  public void restoreState(final State state) { this.state = state; }
  @Override
  public State getState() { return state; }
  protected State createState() { return new State(); }

  public void onStop() {
    busy = false;
    afterAnimationOperations.clear();
  }

  protected int getNothingFoundState() { return STATE_NOTHING_FOUND; }

  /** @return the requestBuilder */
  public RBT getRequestBuilder() { return requestBuilder; }

  @Override
  public void onStartCrucialGUIOperation() {
    animationsRunning = true;
  }
  @Override
  public void onFinishCrucialGUIOperation() {
    animationsRunning = false;
    final ArrayList<Runnable> afterAnimationOperations = this.afterAnimationOperations;
    final int count = afterAnimationOperations.size();
    for (int i = 0; i < count; i++) {
      afterAnimationOperations.get(i).run();
    }
    afterAnimationOperations.clear();
  }

  /**
   * Fetcher request callback.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  @SuppressWarnings("rawtypes")
  public static class RBAdapterCallback<MT extends UniqueObject, RBT extends RequestBuilder, AT extends RequestBuilderAdapter<MT, RBT>> extends ApiSupportRequestCallback<ArrayList> {

    /** Adapter. */
    AT adapter;

    /** A list. */
    private FetchableListView list;

    /** @param fetcher the fetcher to set */
    @SuppressWarnings("unchecked")
    public void setList(final FetchableListView list) {
      this.list = list;
      try {
        this.adapter = (AT) list.getAdapter();
      } catch (final ClassCastException e) {
        throw new IllegalArgumentException("List adapter is not request builder adapter");
      }
    }

    @Override
    public Class<?> getModelClass(final int token, final int operation) { return ArrayList.class; }

    protected RequestBuilder getCurrentRequestBuilder() { return adapter.requestBuilder; }

    @Override
    protected void onOperationPending(final int token, final int operation) {
      postToGUI(new Runnable() {
        @Override
        public void run() { list.setupWait(); }
      });

    }

    protected void postToGUI(final Runnable r) {
      final Context c = list.getContext();
      if (c instanceof Activity) {
        ((Activity)c).runOnUiThread(r);
      } else {
        list.post(r);
      }
    }

    protected void onEmptyResponse(final String message) {
      if (DEBUG) { Log.d(TAG, "Empty response"); }
      adapter.state.message = message;
      adapter.state.level = adapter.getNothingFoundState();
      if (adapter.getCount() == 0) {
        postToGUI(new Runnable() {
          @Override
          public void run() {
            if (list != null) {
              adapter.setBusy(false);
              list.setupMessageView(adapter.state.level, message);
              list.itemsLoaded(true);
            }
          }
        });
      } else {
        postToGUI(new Runnable() {
          @Override
          public void run() {
            if (list != null) {
              adapter.setBusy(false);
              list.setupListView();
            }
          }
        });
      }
    }

    private void postListMessage(final int state, final String message) {
      adapter.state.message = message;
      adapter.state.level = state;
      if (DEBUG) { Log.d(TAG, "Post message " + state + " / " + message); }
      postToGUI(new Runnable() {
        @Override
        public void run() {
          adapter.setBusy(false);
          list.setupMessageView(state, message);
        }
      });
    }

    @Override
    protected void processConnectionError(final int token, final int operation, final ResponseData responseData) {
      if (DEBUG) { Log.d(TAG, "Connection error in list: " + responseData.getMessage()); }
      if (adapter.getCount() == 0) {
        postListMessage(STATE_CONNECTION_ERROR, responseData.getMessage());
      } else {
       postToGUI(new Runnable() {
         @Override
         public void run() {
           adapter.state.hasMoreElements = false;
           adapter.setBusy(false);
           list.setupListView();
         }
        });
      }
    }

    @Override
    protected void processServerError(final int token, final int operation, final ResponseData responseData) {
      postListMessage(responseData.getErrorCode(), responseData.getMessage());
    }

    protected void onResponse(final ArrayList model) {
      postToGUI(new Runnable() {
        @SuppressWarnings("unchecked")
        @Override
        public void run() {
          if (adapter.animationsRunning) {
            adapter.afterAnimationOperations.add(this);
            return;
          }
          adapter.setBusy(false);
          adapter.addAll(model);
          list.setupListView();
          list.itemsLoaded(false);
        }
      });
    }

    @Override
    protected void processSuccess(final int token, final int operation, final ResponseData responseData, final ArrayList model) {
      if (model == null || model.isEmpty()) {
        onEmptyResponse(responseData.getMessage());
        return;
      }
      if (DEBUG) { Log.d(TAG, "model: " + model); }
      onResponse(model);
    }

    @Override
    protected void onOperationFinished(final int token, final int operation) {
    }

    public FetchableListView getListView() { return list; }

    @Override
    public boolean filterOperation(final int token, final int o) {
      if (adapter == null) { return false; }
      final int t = adapter.token;
      if (t != -1 && t != token) { return false; }
      final RequestBuilder rb = getCurrentRequestBuilder();
      return rb != null && rb.getOperation().getCode() == o;
    }

  }
}
