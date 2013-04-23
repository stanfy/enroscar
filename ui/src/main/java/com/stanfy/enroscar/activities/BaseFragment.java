package com.stanfy.enroscar.activities;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewParent;
import android.widget.FrameLayout;

/**
 * Base fragment class.
 * @see BaseActivityBehavior
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class BaseFragment extends Fragment {

  /** Class name of NoSaveStateFrameLayout. */
  private static final String CLASS_NOSAVE_STATE_FRAME_LAYOUT = "NoSaveStateFrameLayout";

  protected View getMainView() {
    View view = getView();
    if (view != null && view instanceof FrameLayout && CLASS_NOSAVE_STATE_FRAME_LAYOUT.equals(view.getClass().getSimpleName())) {
      view = ((FrameLayout)view).getChildAt(0);
    }
    return view;
  }

  @Override
  public void onStart() {
    super.onStart();
    /*
      XXX I don't know the reason but sometimes after coming back here from other activity all layout requests are blocked. :(
      It looks like some concurrency issue or a views tree traversal bug
     */
    final View contentView = getActivity().findViewById(android.R.id.content);
    if (contentView != null) {
      final ViewParent root = contentView.getParent();
      if (contentView.isLayoutRequested() && !root.isLayoutRequested()) {
        root.requestLayout();
      }
    }
  }

  /**
   * @see Activity#runOnUiThread(Runnable)
   * @param work work for GUI thread
   */
  public void runOnUiThread(final Runnable work) {
    final Activity a = getActivity();
    if (a != null) { a.runOnUiThread(work); }
  }

}
