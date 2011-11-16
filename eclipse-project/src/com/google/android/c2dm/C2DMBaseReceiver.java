/*
 * Copyright 2010 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.android.c2dm;

import java.io.IOException;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.PowerManager;
import android.util.Log;

import com.stanfy.DebugFlags;

/**
 * Base class for C2D message receiver. Includes constants for the
 * strings used in the protocol.
 */
public abstract class C2DMBaseReceiver extends IntentService {

  /** Debug flag. */
  protected static final boolean DEBUG = DebugFlags.DEBUG_C2DM;

  /** Callback intent action. */
  public static final String REGISTRATION_CALLBACK_INTENT = "com.google.android.c2dm.intent.REGISTRATION";

  /** Logging tag. */
  protected static final String TAG = "C2DM";

  /** Intent actions. */
  private static final String C2DM_RETRY = "com.google.android.c2dm.intent.RETRY",
                              C2DM_INTENT = "com.google.android.c2dm.intent.RECEIVE";


  /** Extras in the registration callback intents. */
  public static final String EXTRA_UNREGISTERED = "unregistered",
                             EXTRA_ERROR = "error",
                             EXTRA_REGISTRATION_ID = "registration_id";

  /** Errors. */
  public static final String ERR_SERVICE_NOT_AVAILABLE = "SERVICE_NOT_AVAILABLE",
                             ERR_ACCOUNT_MISSING = "ACCOUNT_MISSING",
                             ERR_AUTHENTICATION_FAILED = "AUTHENTICATION_FAILED",
                             ERR_TOO_MANY_REGISTRATIONS = "TOO_MANY_REGISTRATIONS",
                             ERR_INVALID_PARAMETERS = "INVALID_PARAMETERS",
                             ERR_INVALID_SENDER = "INVALID_SENDER",
                             ERR_PHONE_REGISTRATION_ERROR = "PHONE_REGISTRATION_ERROR";

  /** wakelock. */
  private static final String WAKELOCK_KEY = "C2DM_LIB";

  /** Wakelock instance. */
  private static PowerManager.WakeLock mWakeLock;

  /** Sender ID. */
  private final String senderId;

  /**
   * The C2DMReceiver class must create a no-arg constructor and pass the
   * sender id to be used for registration.
   */
  public C2DMBaseReceiver(final String senderId) {
    // senderId is used as base name for threads, etc.
    super(senderId);
    this.senderId = senderId;
  }

  /**
   * Called when a cloud message has been received.
   */
  protected abstract void onMessage(Context context, Intent intent);

  /**
   * Called on registration error. Override to provide better
   * error messages.
   *
   * This is called in the context of a Service - no dialog or UI.
   */
  public abstract void onError(Context context, String errorId);

  /**
   * Called when a registration token has been received.
   */
  public void onRegistered(final Context context, final String registrationId) throws IOException {
    // registrationId will also be saved
  }

  /**
   * Called when the device has been unregistered.
   */
  public void onUnregistered(final Context context) {
  }


  @Override
  public final void onHandleIntent(final Intent intent) {
    try {
      final Context context = getApplicationContext();
      if (intent.getAction().equals(REGISTRATION_CALLBACK_INTENT)) {
        handleRegistration(context, intent);
      } else if (intent.getAction().equals(C2DM_INTENT)) {
        onMessage(context, intent);
      } else if (intent.getAction().equals(C2DM_RETRY)) {
        C2DMessaging.register(context, senderId);
      }
    } finally {
      //  Release the power lock, so phone can get back to sleep.
      // The lock is reference counted by default, so multiple
      // messages are ok.

      // If the onMessage() needs to spawn a thread or do something else,
      // it should use it's own lock.
      mWakeLock.release();
    }
  }


  /**
   * Called from the broadcast receiver.
   * Will process the received intent, call handleMessage(), registered(), etc.
   * in background threads, with a wake lock, while keeping the service
   * alive.
   */
  static void runIntentInService(final Context context, final Intent intent) {
    if (mWakeLock == null) {
      // This is called from BroadcastReceiver, there is no init.
      final PowerManager pm =
        (PowerManager) context.getSystemService(Context.POWER_SERVICE);
      mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
          WAKELOCK_KEY);
    }
    mWakeLock.acquire();

    // Use a naming convention, similar with how permissions and intents are
    // used. Alternatives are introspection or an ugly use of statics.
    final String receiver = context.getPackageName() + ".C2DMReceiver";
    intent.setClassName(context, receiver);

    context.startService(intent);

  }


  private void handleRegistration(final Context context, final Intent intent) {
    final String registrationId = intent.getStringExtra(EXTRA_REGISTRATION_ID);
    final String error = intent.getStringExtra(EXTRA_ERROR);
    final String removed = intent.getStringExtra(EXTRA_UNREGISTERED);

    if (DEBUG) {
      Log.d(TAG, "dmControl: registrationId = " + registrationId + ", error = " + error + ", removed = " + removed);
    }

    if (removed != null) {
      // Remember we are unregistered
      C2DMessaging.clearRegistrationId(context);
      onUnregistered(context);
      return;
    } else if (error != null) {
      // we are not registered, can try again
      C2DMessaging.clearRegistrationId(context);
      // Registration failed
      Log.e(TAG, "Registration error " + error);
      onError(context, error);
      if ("SERVICE_NOT_AVAILABLE".equals(error)) {
        long backoffTimeMs = C2DMessaging.getBackoff(context);

        if (DEBUG) { Log.d(TAG, "Scheduling registration retry, backoff = " + backoffTimeMs); }
        final Intent retryIntent = new Intent(C2DM_RETRY);
        final PendingIntent retryPIntent = PendingIntent.getBroadcast(context,
            0 /*requestCode*/, retryIntent, 0 /*flags*/);

        final AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.set(AlarmManager.ELAPSED_REALTIME,
            backoffTimeMs, retryPIntent);

        // Next retry should wait longer.
        backoffTimeMs *= 2;
        C2DMessaging.setBackoff(context, backoffTimeMs);
      }
    } else {
      try {
        onRegistered(context, registrationId);
        C2DMessaging.setRegistrationId(context, registrationId);
      } catch (final IOException ex) {
        Log.e(TAG, "Registration error " + ex.getMessage());
      }
    }
  }
}
