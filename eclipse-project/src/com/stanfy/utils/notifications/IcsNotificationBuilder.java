package com.stanfy.utils.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.Notification.Builder;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.widget.RemoteViews;

/**
 * {@link NotificationBuilder} that wraps latest API.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
public class IcsNotificationBuilder implements NotificationBuilder {

  /** Core builder. */
  final Builder core;

  public IcsNotificationBuilder(final Context context) {
    core = new Builder(context);
  }

  @Override
  public NotificationBuilder setWhen(final long when) {
    core.setWhen(when);
    return this;
  }

  @Override
  public NotificationBuilder setSmallIcon(final int icon) {
    core.setSmallIcon(icon);
    return this;
  }

  @Override
  public NotificationBuilder setSmallIcon(final int icon, final int level) {
    core.setSmallIcon(icon, level);
    return this;
  }

  @Override
  public NotificationBuilder setContentTitle(final CharSequence title) {
    core.setContentTitle(title);
    return this;
  }

  @Override
  public NotificationBuilder setContentText(final CharSequence text) {
    core.setContentText(text);
    return this;
  }

  @Override
  public NotificationBuilder setNumber(final int number) {
    core.setNumber(number);
    return this;
  }

  @Override
  public NotificationBuilder setContentInfo(final CharSequence info) {
    core.setContentInfo(info);
    return this;
  }

  @Override
  public NotificationBuilder setProgress(final int max, final int progress, final boolean indeterminate) {
    core.setProgress(max, progress, indeterminate);
    return this;
  }

  @Override
  public NotificationBuilder setContent(final RemoteViews views) {
    core.setContent(views);
    return this;
  }

  @Override
  public NotificationBuilder setContentIntent(final PendingIntent intent) {
    core.setContentIntent(intent);
    return this;
  }

  @Override
  public NotificationBuilder setDeleteIntent(final PendingIntent intent) {
    core.setDeleteIntent(intent);
    return this;
  }

  @Override
  public NotificationBuilder setFullScreenIntent(final PendingIntent intent, final boolean highPriority) {
    core.setFullScreenIntent(intent, highPriority);
    return this;
  }

  @Override
  public NotificationBuilder setTicker(final CharSequence tickerText) {
    core.setTicker(tickerText);
    return this;
  }

  @Override
  public NotificationBuilder setTicker(final CharSequence tickerText, final RemoteViews views) {
    core.setTicker(tickerText, views);
    return this;
  }

  @Override
  public NotificationBuilder setLargeIcon(final Bitmap icon) {
    core.setLargeIcon(icon);
    return this;
  }

  @Override
  public NotificationBuilder setSound(final Uri sound) {
    core.setSound(sound);
    return this;
  }

  @Override
  public NotificationBuilder setSound(final Uri sound, final int streamType) {
    core.setSound(sound, streamType);
    return this;
  }

  @Override
  public NotificationBuilder setVibrate(final long[] pattern) {
    core.setVibrate(pattern);
    return this;
  }

  @Override
  public NotificationBuilder setLights(final int argb, final int onMs, final int offMs) {
    core.setLights(argb, onMs, offMs);
    return this;
  }

  @Override
  public NotificationBuilder setOngoing(final boolean ongoing) {
    core.setOngoing(ongoing);
    return this;
  }

  @Override
  public NotificationBuilder setOnlyAlertOnce(final boolean onlyAlertOnce) {
    core.setOnlyAlertOnce(onlyAlertOnce);
    return this;
  }

  @Override
  public NotificationBuilder setAutoCancel(final boolean autoCancel) {
    core.setAutoCancel(autoCancel);
    return this;
  }

  @Override
  public NotificationBuilder setDefaults(final int defaults) {
    core.setDefaults(defaults);
    return this;
  }

  @SuppressWarnings("deprecation")
  @Override
  public Notification build() {
    return core.getNotification();
  }

  @Override
  public int getApiVersion() { return Build.VERSION_CODES.ICE_CREAM_SANDWICH; }

}
