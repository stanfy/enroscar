package com.stanfy.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.stanfy.serverapi.response.ResponseData;

/**
 * Helper class for handling states.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class StateHelper {

  /** Element view states. */
  public static final int STATE_NORMAL = 0, STATE_EMPTY = 1, STATE_MESSAGE = 2, STATE_LOADING = 3;

  /** Default loading. */
  private static final StateViewCreator[] DEFAULT_STATES = new StateViewCreator[STATE_LOADING + 1];
  static {
    DEFAULT_STATES[STATE_LOADING] = new DefaultLoadingStateViewCreator();
    final DefaultMessageStateViewCreator messageCreator = new DefaultMessageStateViewCreator();
    DEFAULT_STATES[STATE_EMPTY] = messageCreator;
    DEFAULT_STATES[STATE_MESSAGE] = messageCreator;
  }

  public static void setDefaultStateViewCreator(final int state, final StateViewCreator creator) {
    DEFAULT_STATES[state] = creator;
  }

  /** Special views. */
  private final StateViewCreator[] viewCreators = constructCreatorsArray();

  protected StateViewCreator[] constructCreatorsArray() {
    final StateViewCreator[] result = new StateViewCreator[DEFAULT_STATES.length];
    System.arraycopy(DEFAULT_STATES, 0, result, 0, result.length);
    return result;
  }

  public void setStateViewCreator(final int state, final StateViewCreator creator) {
    if (state != STATE_NORMAL && state > 0 && state < viewCreators.length) {
      viewCreators[state] = creator;
    }
  }

  public View getCustomStateView(final int state, final Context context, final Object lastResponseData, final ViewGroup parent) {
    final StateViewCreator creator = state > 0 && state < viewCreators.length ? viewCreators[state] : null;
    if (creator == null) { return null; }
    final View view = creator.getView(context, lastResponseData, parent);
    configureStateViewHeight(parent, view);
    return view;
  }

  public boolean hasState(final int state) { return viewCreators[state] != null; }

  public void configureStateViewHeight(final ViewGroup parent, final View stateView) {
    if (!(parent instanceof ListView)) { return; }
    final ListView listView = (ListView) parent;

    if (stateView.getLayoutParams().height != ViewGroup.LayoutParams.MATCH_PARENT) { return; }

    // check for one child only that wants to be as tall as we are
    final int childCount = listView.getChildCount();

    final int headersCount = listView.getHeaderViewsCount();

    final int dHeight = listView.getDividerHeight();
    int h = listView.getHeight() - listView.getPaddingTop() - listView.getPaddingBottom();

    for (int i = 0; i < headersCount; i++) {
      final View header = listView.getChildAt(i);
      if (header != null) {
        h -= header.getHeight() + dHeight;
      }
    }
    final int footersCount = listView.getFooterViewsCount();
    for (int i = 0; i < footersCount; i++) {
      final View footer = listView.getChildAt(childCount - footersCount - 1);
      if (footer != null) {
        h -= footer.getHeight() + dHeight;
      }
    }

    if (h > stateView.getHeight()) {
      stateView.setMinimumHeight(h);
    }
  }

  /**
   * State view creator.
   */
  public abstract static class StateViewCreator {

    /** View instance. */
    private View view;

    protected abstract View createView(final Context context, final ViewGroup parent);
    protected abstract void bindView(final Context context, final View view, final Object lastResponseData, final ViewGroup parent);

    View getView(final Context context, final Object lastResponseData, final ViewGroup parent) {
      if (view == null) {
        view = createView(context, parent);
      }
      bindView(context, view, lastResponseData, parent);
      return view;
    }

  }

  /** Default loading state. */
  public static class DefaultLoadingStateViewCreator extends StateViewCreator {
    @Override
    protected View createView(final Context context, final ViewGroup parent) {
      return LayoutInflater.from(context).inflate(R.layout.progress_panel, parent, false);
    }
    @Override
    protected void bindView(final Context context, final View view, final Object lastResponseData, final ViewGroup parent) {
      // nothing
    }
  }

  /** Default message state. */
  public static class DefaultMessageStateViewCreator extends StateViewCreator {
    @Override
    protected View createView(final Context context, final ViewGroup parent) {
      return LayoutInflater.from(context).inflate(R.layout.message_panel, parent, false);
    }
    @Override
    protected void bindView(final Context context, final View view, final Object lastResponseData, final ViewGroup parent) {
      if (lastResponseData instanceof ResponseData) {
        ((TextView)view).setText(((ResponseData<?>) lastResponseData).getMessage());
      }
    }
  }

}
