package com.stanfy.enroscar.views.qa;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Interpolator;
import android.view.animation.TranslateAnimation;
import android.widget.HorizontalScrollView;

import com.stanfy.enroscar.ui.R;

/**
 * {@link QuickActionsWidget} that displays actions on a row.
 * See <a href="https://github.com/cyrilmottier/GreenDroid/blob/master/GreenDroid/src/greendroid/widget/QuickActionBar.java">GreenDroid</a>.
 * @author Benjamin Fellous
 * @author Cyril Mottier
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class QuickActionsBar extends QuickActionsWidget {

  /** Scroll view. */
  private HorizontalScrollView scrollView;
  /** Main animation. */
  private Animation mainAnimation;
  /** Animated panel. */
  private ViewGroup panel;
  /** Main container. */
  private ViewGroup container;

  public QuickActionsBar(final Context context) {
    super(context);

    mainAnimation = createMainAnimation();
    setContentView(R.layout.quick_actions_bar);

    final View v = getContentView();
    panel = (ViewGroup)v.findViewById(R.id.qa_panel);
    container = (ViewGroup)v.findViewById(R.id.qa_container);
    scrollView = (HorizontalScrollView)v.findViewById(R.id.qa_scroll);
  }

  protected Animation createMainAnimation() {
    final TranslateAnimation t = new TranslateAnimation(
        Animation.RELATIVE_TO_PARENT, 1, Animation.RELATIVE_TO_PARENT, 0,
        Animation.RELATIVE_TO_PARENT, 0, Animation.RELATIVE_TO_PARENT, 0
    );
    final long duration = getContext().getResources().getInteger(android.R.integer.config_longAnimTime);
    t.setDuration(duration);
    t.setInterpolator(new BounceInterpolator());
    return t;
  }

  @Override
  protected void onRedrawActions() {
    final QuickActionsAdapter quickActionsAdapter = this.quickActionsAdapter;
    final ViewGroup container = this.container;
    container.removeAllViews();
    final int count = quickActionsAdapter.getCount();
    for (int i = 0; i < count; i++) {
      container.addView(obtainView(i));
    }
  }

  @Override
  public void show(final View anchor) {
    super.show(anchor);
    scrollView.scrollTo(0, 0);
    panel.startAnimation(mainAnimation);
  }

  /**
   * Nice bounce interpolator.
   */
  private static class BounceInterpolator implements Interpolator {
    @Override
    public float getInterpolation(final float t) {
      final float c55 = 1.55f, c1 = 1.1f, c2 = 1.2f;
      final float inner = (t * c55) - c1;
      return c2 - inner * inner;
    }
  }

}
