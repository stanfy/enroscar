/*
 */
package com.google.android.c2dm;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Helper class to handle BroadcastReciver behavior.<br/>
 * - can only run for a limited amount of time - it must start a real service
 * for longer activity<br/>
 * - must get the power lock, must make sure it's released when all done.
 */
public class C2DMBroadcastReceiver extends BroadcastReceiver {

  @Override
  public final void onReceive(final Context context, final Intent intent) {
    // To keep things in one place.
    C2DMBaseReceiver.runIntentInService(context, intent);
    setResult(Activity.RESULT_OK, null /* data */, null /* extra */);
  }
}
