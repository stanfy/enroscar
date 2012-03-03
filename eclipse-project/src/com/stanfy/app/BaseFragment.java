package com.stanfy.app;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.NoSaveStateFrameLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewParent;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.stanfy.DebugFlags;
import com.stanfy.views.ProgressWrapperFrameLayout;
import com.stanfy.views.ScrollView;

/**
 * Base fragment class.
 * @param <AT> application type
 * @see BaseActivityBehavior
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class BaseFragment<AT extends Application> extends Fragment {

  /** Indicates that container is linear layout. */
  public static final int CONTAINER_LINEAR_LAYOUT = 1, CONTAINER_RELATIVE_LAYOUT = 2, CONTAINER_FRAME_LAYOUT = 3;

  /** Layout extras. */
  public static final String EXTRA_LAYOUT_TYPE = "layout_type",
                             EXTRA_LAYOUT_WIDTH = "layout_width",
                             EXTRA_LAYOUT_HEIGHT = "layout_height";

  /** State variables. */
  private static final String STATE_SCROLL_X = "_state_scroll_x",
                              STATE_SCROLL_Y = "_state_scroll_y";

  /** First start flag. */
  private boolean firstStart = false;

  /** Scroll position from the state. */
  private float scrollX = -1, scrollY = -1;

  @Override
  public void onAttach(final Activity activity) {
    if (activity instanceof BaseActivity) {
      throw new IllegalStateException("Incorrect activity class. Enroscar fragments can be attached to activities that extend "
          + BaseActivity.class.getCanonicalName() + " only");
    }
    super.onAttach(activity);
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    firstStart = true;
  }

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    if (savedInstanceState != null) {
      scrollX = savedInstanceState.getFloat(STATE_SCROLL_X, -1);
      scrollY = savedInstanceState.getFloat(STATE_SCROLL_Y, -1);
    }
  }

  protected View getMainView() {
    View view = getView();
    if (view != null && view instanceof NoSaveStateFrameLayout) {
      view = ((NoSaveStateFrameLayout)view).getChildAt(0);
    }
    if (view != null && view instanceof ProgressWrapperFrameLayout) {
      view = ((ProgressWrapperFrameLayout) view).getMainView();
    }
    return view;
  }

  protected void onRestart() {
    // nothing
  }

  @Override
  public void onStart() {
    if (!firstStart) { onRestart(); }
    firstStart = false;
    super.onStart();
    /*
      XXX I don't know the reason but sometimes after coming back here from other activity all layout requests are blocked. :(
      It looks like some concurrency issue or a views tree traversal bug
     */
    final View contentView = getActivity().findViewById(android.R.id.content);
    if (contentView != null) {
      final ViewParent root = contentView.getParent();
      if (contentView.isLayoutRequested() && !root.isLayoutRequested()) {
        if (DebugFlags.DEBUG_GUI) { Log.i("View", "fix layout request"); }
        root.requestLayout();
      }
    }
  }

  @SuppressWarnings("deprecation")
  @Override
  public void onResume() {
    super.onResume();
    final View view = getMainView();
    restoreScrollPosition(view);
  }

  @Override
  public void onPause() {
    final View view = getMainView();
    if (shouldSaveScroll(view)) { fixScroll(view); }
    super.onPause();
  }

  protected boolean shouldSaveScroll(final View view) {
    return view != null && view instanceof ScrollView && view.getId() == View.NO_ID && ((ScrollView)view).isFrozeScrollPosition();
  }

  /**
   * @deprecated it's dangerous
   */
  @Deprecated
  protected void restoreScrollPosition(final View view) {
    final float scrollX = this.scrollX;
    final float scrollY = this.scrollY;
    if (view != null && scrollX >= 0 && scrollY >= 0) {
      view.postDelayed(new Runnable() {
        @Override
        public void run() {
          if (!isAdded() || isDetached()) { return; }
          final int toX = (int)(getMainViewContentWidth(view) * scrollX);
          final int toY = (int)(getMainViewContentHeight(view) * scrollY);
          if (toX >= 0 && toY >= 0) { view.scrollTo(toX, toY); }
        }
      }, getRestoreScrollDelay());
      this.scrollX = -1;
      this.scrollY = -1;
    }
  }

  protected int getMainViewContentHeight(final View view) {
    return view instanceof ScrollView ? ((ScrollView)view).getContentHeight() : view.getHeight();
  }
  protected int getMainViewContentWidth(final View view) {
    return view instanceof ScrollView ? ((ScrollView)view).getContentWidth() : view.getWidth();
  }
  /**
   * @deprecated it's dangerous
   */
  @Deprecated
  protected int getRestoreScrollDelay() { return 0; }

  private void fixScroll(final View view) {
    final int w = getMainViewContentWidth(view);
    final int h = getMainViewContentHeight(view);
    scrollX = w > 0 ? (float)view.getScrollX() / w : 0;
    scrollY = h > 0 ? (float)view.getScrollY() / h : 0;
  }

  @Override
  public void onSaveInstanceState(final Bundle outState) {
    final View view = getMainView();
    if (shouldSaveScroll(view)) {
      fixScroll(view);
      outState.putFloat(STATE_SCROLL_X, scrollX);
      outState.putFloat(STATE_SCROLL_Y, scrollY);
    }
    super.onSaveInstanceState(outState);
  }

  /**
   * @return owning fragment activity
   * @see #getActivity()
   */
  @SuppressWarnings("unchecked")
  protected BaseFragmentActivity<AT> getOwnerActivity() { return (BaseFragmentActivity<AT>)getActivity(); }

  /**
   * @see Activity#runOnUiThread(Runnable)
   * @param work work for GUI thread
   */
  public void runOnUiThread(final Runnable work) {
    final Activity a = getActivity();
    if (a != null) { a.runOnUiThread(work); }
  }

  protected static LayoutParams createLayoutParams(final int type, final int width, final int height) {
    switch (type) {
    case CONTAINER_LINEAR_LAYOUT: return new LinearLayout.LayoutParams(width, height);
    case CONTAINER_RELATIVE_LAYOUT: return new RelativeLayout.LayoutParams(width, height);
    case CONTAINER_FRAME_LAYOUT: return new FrameLayout.LayoutParams(width, height);
    default: return null;
    }
  }

  /**
   * Configure layout parameters for the view that is returned by onCreateView.
   * @param mainView view to edit
   * @param container container (optional)
   */
  protected void setupLayoutParameters(final View mainView, final ViewGroup container) {
    final Bundle args = getArguments();
    // first check argument
    final int width, height;
    int layoutType;
    if (args == null) {
      width = LayoutParams.MATCH_PARENT;
      height = LayoutParams.MATCH_PARENT;
      layoutType = -1;
    } else {
      width = args.getInt(EXTRA_LAYOUT_WIDTH, LayoutParams.MATCH_PARENT);
      height = args.getInt(EXTRA_LAYOUT_HEIGHT, LayoutParams.MATCH_PARENT);
      layoutType = args.getInt(EXTRA_LAYOUT_TYPE, -1);
    }
    // arguments didn't say us anything about container nature, try to analyze container
    if (layoutType == -1 && container != null) {
      final Class<?> cClass = container.getClass();
      if (cClass == LinearLayout.class) {
        layoutType = CONTAINER_LINEAR_LAYOUT;
      } else if (cClass == FrameLayout.class) {
        layoutType = CONTAINER_FRAME_LAYOUT;
      } else if (cClass == RelativeLayout.class) {
        layoutType = CONTAINER_RELATIVE_LAYOUT;
      }
    }
    if (layoutType != -1) {
      mainView.setLayoutParams(createLayoutParams(layoutType, width, height));
    }
  }

}
