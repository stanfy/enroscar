package com.stanfy.views;

import java.util.TreeMap;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.stanfy.utils.AppUtils;

/**
 * State window helper.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public final class StateWindowHelper {

  /** State. */
  private static final int STATE_MAIN = 0, STATE_PROGRESS = 1, STATE_MESSAGE = 2, NOT_INITED = -1;

  /** State container. */
  private final View statePanel;

  /** State container. */
  private final View stateContainer;

  /** Progress view. */
  private final View progressView;

  /** Main view. */
  private final View mainView;
  /** Additional view. */
  private View additionalView;

  /** Text view. */
  private final TextView messageViewText;
  /** Message image. */
  private final ImageView messageImage;
  /** Action view. */
  private final View messageAction;

  /** Possible actions. */
  private final TreeMap<Integer, ActionDefiner> actions = new TreeMap<Integer, ActionDefiner>();

  /** Fade animation. */
  private int fadeInAnimation = android.R.anim.fade_in, fadeOutAnimation = android.R.anim.fade_out;

  /** Current state. */
  private int currentState = NOT_INITED;

  public StateWindowHelper(final View statePanel, final View mainView) {
    this(statePanel, mainView, null);
  }
  public StateWindowHelper(final View statePanel, final View mainView, final View additionalView) {
    this.statePanel = statePanel;
    this.stateContainer = statePanel.findViewById(R.id.state_message);
    final View stateContainer = this.stateContainer;
    if (stateContainer != null) {
      messageViewText = (TextView)stateContainer.findViewById(R.id.message_text);
      messageImage = (ImageView)stateContainer.findViewById(R.id.message_image);
      messageAction = stateContainer.findViewById(R.id.message_action);
    } else {
      messageViewText = null;
      messageImage = null;
      messageAction = null;
    }
    this.progressView = statePanel.findViewById(R.id.state_progress);
    this.mainView = mainView;
    this.additionalView = additionalView;
    showMain();
  }

  /** @param additionalView the additionalView to set */
  public void setAdditionalView(final View additionalView) { this.additionalView = additionalView; }
  /** @return the mainView */
  public View getMainView() { return mainView; }
  /** @return the additionalView */
  public View getAdditionalView() { return additionalView; }

  /**
   * @param fadeInAnimation fade in animation
   */
  public void setFadeInAnimation(final int fadeInAnimation) {
    this.fadeInAnimation = fadeInAnimation;
  }
  /**
   * @param fadeOutAnimation fade out animation
   */
  public void setFadeOutAnimation(final int fadeOutAnimation) {
    this.fadeOutAnimation = fadeOutAnimation;
  }

  public void registerAction(final int state, final ActionDefiner action) {
    actions.put(state, action);
  }

  public void resolveState(final int state, final String message) {
    if (stateContainer == null) { return; }
    currentState = STATE_MESSAGE;
    statePanel.setVisibility(View.VISIBLE);
    progressView.setVisibility(View.GONE);
    stateContainer.setVisibility(View.VISIBLE);
    mainView.setVisibility(View.GONE);
    if (additionalView != null) { additionalView.setVisibility(View.GONE); }

    if (messageViewText != null) {
      messageViewText.setText(message);
    }
    if (messageImage != null) {
      messageImage.setImageLevel(state);
    }
    if (messageAction != null) {
      final ActionDefiner action = actions.get(state);
      if (action != null) {
        messageAction.setVisibility(View.VISIBLE);
        action.configureView(messageAction);
        messageAction.setOnClickListener(action);
      } else {
        messageAction.setVisibility(View.GONE);
      }
    }
  }

  public boolean isProgressVisible() { return progressView.getVisibility() == View.VISIBLE; }
  public boolean isMainVisible() { return mainView.getVisibility() == View.VISIBLE; }

  public void showProgress() {
    currentState = STATE_PROGRESS;
    statePanel.setVisibility(View.VISIBLE);
    progressView.setVisibility(View.VISIBLE);
    if (stateContainer != null) { stateContainer.setVisibility(View.GONE); }
    mainView.setVisibility(View.GONE);
    if (additionalView != null) { additionalView.setVisibility(View.GONE); }
  }
  public void showMain() {
    showMain(false);
  }
  public void showMain(final boolean animate) {
    final int prevState = currentState;
    currentState = STATE_MAIN;
    if (prevState == currentState) { return; }

    final View outView = prevState == STATE_PROGRESS ? progressView : statePanel;
    final Context context = outView.getContext();
    if (animate) {
      outView.startAnimation(AnimationUtils.loadAnimation(context, fadeOutAnimation));
      mainView.startAnimation(AnimationUtils.loadAnimation(context, fadeInAnimation));
      if (additionalView != null) { additionalView.startAnimation(AnimationUtils.loadAnimation(context, fadeInAnimation)); }
    } else {
      outView.clearAnimation();
      mainView.clearAnimation();
      if (additionalView != null) { additionalView.clearAnimation(); }
    }

    statePanel.setVisibility(View.GONE);
    progressView.setVisibility(View.GONE);
    if (stateContainer != null) { stateContainer.setVisibility(View.GONE); }
    mainView.setVisibility(View.VISIBLE);
    if (additionalView != null) { additionalView.setVisibility(View.VISIBLE); }
  }

  /**
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public interface ActionDefiner extends OnClickListener {
    /** Button height. */
    int BUTTON_HEIGHT = 55;
    /**
     * @param v view to configure
     */
    void configureView(final View v);
  }

  /**
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  abstract static class ActionDefinerAdapter implements ActionDefiner {
    /** Context. */
    final Resources resources;
    /** Diplay metrics. */
    final DisplayMetrics dm;
    public ActionDefinerAdapter(final Context context) {
      this.resources = context.getResources();
      dm = context.getResources().getDisplayMetrics();
    }
    @Override
    public void configureView(final View v) {
      final Button b = (Button)v;
      final int dh = AppUtils.pixelsWidth(dm, BUTTON_HEIGHT);
      b.setMinHeight(dh);
      b.setMaxHeight(dh);
      b.setMinWidth(0);
      b.setMaxWidth(Integer.MAX_VALUE);
    }
  }


  /**
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public abstract static class TextButtonActionDefiner extends ActionDefinerAdapter {
    /** Text. */
    private final int text;
    public TextButtonActionDefiner(final Context context, final int text) {
      super(context);
      this.text = text;
    }
    @Override
    public void configureView(final View v) {
      super.configureView(v);
      final Button b = (Button)v;
      b.setText(text);
      final int padding = 6, dp = AppUtils.pixelsOffset(dm, padding), dp2 = dp << 1;
      b.setPadding(dp2, dp, dp2, dp);
      b.setCompoundDrawables(null, null, null, null);
    }
  }

}
