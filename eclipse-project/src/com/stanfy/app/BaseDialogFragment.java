package com.stanfy.app;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

/**
 * Base dialog fragment.
 * @param <AT> application type
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class BaseDialogFragment<AT extends Application> extends DialogFragment {

  /**
   * @see Activity#runOnUiThread(Runnable)
   * @param work work for GUI thread
   */
  public void runOnUiThread(final Runnable work) {
    final Activity a = getActivity();
    if (a != null) { a.runOnUiThread(work); }
  }

}
