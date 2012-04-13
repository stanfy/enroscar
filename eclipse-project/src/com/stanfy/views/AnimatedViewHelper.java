package com.stanfy.views;

import android.view.View;

import com.stanfy.app.Application;

/**
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public final class AnimatedViewHelper {

  /** Application instance. */
  private final Application application;

  /** Owner view. */
  private final View owner;

  /** Worker that notifies about finished operation. */
  private final Runnable crucialGuiOperationFinishedWorker = new Runnable() {
    @Override
    public void run() {
      application.dispatchCrucialGUIOperationFinish();
    }
  };

  public AnimatedViewHelper(final View owner) {
    this.owner = owner;
    this.application = (Application) owner.getContext().getApplicationContext();
  }

  public void notifyCrucialGuiStart() {
    owner.removeCallbacks(crucialGuiOperationFinishedWorker);
    application.dispatchCrucialGUIOperationStart();
  }

  public void notifyCrucialGuiFinish() {
    final int delay = 500;
    owner.postDelayed(crucialGuiOperationFinishedWorker, delay);
  }

  public void onDetach() {
    owner.removeCallbacks(crucialGuiOperationFinishedWorker);
    application.dispatchCrucialGUIOperationFinish();
  }

}
