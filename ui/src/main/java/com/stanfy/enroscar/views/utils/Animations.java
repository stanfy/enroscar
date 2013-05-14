package com.stanfy.enroscar.views.utils;

import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.LayoutAnimationController;
import android.view.animation.TranslateAnimation;

/**
 * Animations set.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class Animations {

  /** Hidden constructor. */
  protected Animations() { /* just hide */ }

  /**
   * @return an animation controller
   */
  public static LayoutAnimationController goDownAnimationController() {
    final float delay = 0.5f;
    return new LayoutAnimationController(goDownAnimation(), delay);
  }

  private static Animation moveAnimation(final float fromX, final float toX, final float fromY, final float toY) {
    final long moveDuration = 200,
    alphaDuration = 100;

    final AnimationSet main = new AnimationSet(false);

    Animation animation = new AlphaAnimation(0.0f, 1.0f);
    animation.setDuration(alphaDuration);
    main.addAnimation(animation);

    animation = new TranslateAnimation(
        Animation.RELATIVE_TO_SELF, fromX, Animation.RELATIVE_TO_SELF, toX,
        Animation.RELATIVE_TO_SELF, fromY, Animation.RELATIVE_TO_SELF, toY
    );
    animation.setDuration(moveDuration);
    main.addAnimation(animation);


    return main;
  }

  /**
   * @return an animation controller
   */
  public static Animation goDownAnimation() { return moveAnimation(0, 0, -1, 0); }
  /**
   * @return an animation controller
   */
  public static Animation goUpAnimation() {
    final Animation a = moveAnimation(0, 0, 0, -1);
    a.setFillBefore(true);
    return a;
  }

  /**
   * Empty listener.
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public static class AnimationListner implements Animation.AnimationListener {
    @Override
    public void onAnimationEnd(final Animation animation) { /* empty */ }
    @Override
    public void onAnimationRepeat(final Animation animation) { /* empty */ }
    @Override
    public void onAnimationStart(final Animation animation) { /* empty */ }
  }

}
