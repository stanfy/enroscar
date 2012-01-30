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

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Utilities for device registration.
 *
 * Will keep track of the registration token in a private preference.
 */
public final class C2DMessaging {
  /** Extras. */
  public static final String EXTRA_SENDER = "sender",
                             EXTRA_APPLICATION_PENDING_INTENT = "app";
  /** Unregister action. */
  public static final String REQUEST_UNREGISTRATION_INTENT = "com.google.android.c2dm.intent.UNREGISTER";
  /** Register action. */
  public static final String REQUEST_REGISTRATION_INTENT = "com.google.android.c2dm.intent.REGISTER";

  /** Pref keys. */
  public static final String LAST_REGISTRATION_CHANGE = "last_registration_change",
                             BACKOFF = "backoff",
                             GSF_PACKAGE = "com.google.android.gsf";


  /** Package. */
  static final String PREFERENCE = "com.google.android.c2dm";

  /** Default backoff value. */
  private static final long DEFAULT_BACKOFF = 30000;

  private C2DMessaging() { /* hide */ }

  /**
   * Initiate c2d messaging registration for the current application.
   */
  public static void register(final Context context,
      final String senderId) {
    final Intent registrationIntent = new Intent(REQUEST_REGISTRATION_INTENT);
    registrationIntent.setPackage(GSF_PACKAGE);
    registrationIntent.putExtra(EXTRA_APPLICATION_PENDING_INTENT,
        PendingIntent.getBroadcast(context, 0, new Intent(), 0));
    registrationIntent.putExtra(EXTRA_SENDER, senderId);
    context.startService(registrationIntent);
    // TODO if intent not found, notification on need to have GSF
  }

  /**
   * Unregister the application. New messages will be blocked by server.
   */
  public static void unregister(final Context context) {
    final Intent regIntent = new Intent(REQUEST_UNREGISTRATION_INTENT);
    regIntent.setPackage(GSF_PACKAGE);
    regIntent.putExtra(EXTRA_APPLICATION_PENDING_INTENT, PendingIntent.getBroadcast(context,
        0, new Intent(), 0));
    context.startService(regIntent);
  }

  /**
   * Return the current registration id.
   *
   * If result is empty, the registration has failed.
   *
   * @return registration id, or empty string if the registration is not complete.
   */
  public static String getRegistrationId(final Context context) {
    final SharedPreferences prefs = context.getSharedPreferences(
        PREFERENCE,
        Context.MODE_PRIVATE);
    final String registrationId = prefs.getString("dm_registration", "");
    return registrationId;
  }

  public static long getLastRegistrationChange(final Context context) {
    final SharedPreferences prefs = context.getSharedPreferences(
        PREFERENCE,
        Context.MODE_PRIVATE);
    return prefs.getLong(LAST_REGISTRATION_CHANGE, 0);
  }

  static long getBackoff(final Context context) {
    final SharedPreferences prefs = context.getSharedPreferences(
        PREFERENCE,
        Context.MODE_PRIVATE);
    return prefs.getLong(BACKOFF, DEFAULT_BACKOFF);
  }

  static void setBackoff(final Context context, final long backoff) {
    final SharedPreferences prefs = context.getSharedPreferences(
        PREFERENCE,
        Context.MODE_PRIVATE);
    final Editor editor = prefs.edit();
    editor.putLong(BACKOFF, backoff);
    editor.commit();

  }

  // package
  static void clearRegistrationId(final Context context) {
    final SharedPreferences prefs = context.getSharedPreferences(
        PREFERENCE,
        Context.MODE_PRIVATE);
    final Editor editor = prefs.edit();
    editor.putString("dm_registration", "");
    editor.putLong(LAST_REGISTRATION_CHANGE, System.currentTimeMillis());
    editor.commit();

  }

  // package
  static void setRegistrationId(final Context context, final String registrationId) {
    final SharedPreferences prefs = context.getSharedPreferences(
        PREFERENCE,
        Context.MODE_PRIVATE);
    final Editor editor = prefs.edit();
    editor.putString("dm_registration", registrationId);
    editor.commit();

  }
}
