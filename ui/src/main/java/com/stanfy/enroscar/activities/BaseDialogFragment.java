package com.stanfy.enroscar.activities;

import android.app.Activity;
import android.support.v4.app.DialogFragment;

/**
 * Base dialog fragment.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class BaseDialogFragment extends DialogFragment {

  /**
   * @see Activity#runOnUiThread(Runnable)
   * @param work work for GUI thread
   */
  public void runOnUiThread(final Runnable work) {
    final Activity a = getActivity();
    if (a != null) { a.runOnUiThread(work); }
  }

}
