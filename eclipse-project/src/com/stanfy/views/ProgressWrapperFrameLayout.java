package com.stanfy.views;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

/**
 * Container that is used to wrap views with {@link R.layout#progress_panel}.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ProgressWrapperFrameLayout extends FrameLayout {

  /**
   * Wraps root with custom view.
   * @param root root view
   * @param layout custom layout(this is progress panel or state panel in most of cases)
   * @return wrapped layout
   */
  public static ProgressWrapperFrameLayout wrap(final View root, final int layout) {
    final ProgressWrapperFrameLayout result;
    if (root instanceof ProgressWrapperFrameLayout) {
      result = (ProgressWrapperFrameLayout)root;
    } else {
      result = new ProgressWrapperFrameLayout(root.getContext());
      final ViewGroup.LayoutParams lParams = root.getLayoutParams();
      if (lParams != null) { result.setLayoutParams(lParams); }
      root.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
      result.addView(root);
    }
    inflate(result.getContext(), layout, result);
    return result;
  }

  /**
   * Wraps root with custom {@link R.layout#progress_panel}.
   * @param root root view
   * @return wrapped layout
   */
  public static ProgressWrapperFrameLayout wrap(final View root) {
    return wrap(root, R.layout.state_panel);
  }

  public static StateWindowHelper createStateHelper(final ProgressWrapperFrameLayout wrappedView) {
    return new StateWindowHelper(wrappedView.findViewById(R.id.state_panel), wrappedView.getMainView());
  }

  public ProgressWrapperFrameLayout(final Context context) {
    super(context);
  }

  /** @return main (wrapped) view */
  public View getMainView() { return getChildAt(0); }

}
