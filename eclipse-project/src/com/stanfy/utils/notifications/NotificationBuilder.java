package com.stanfy.utils.notifications;

import android.app.Notification;
import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.widget.RemoteViews;

/**
 * Builder class for {@link Notification} objects.  Allows easier control over
 * all the flags, as well as help constructing the typical notification layouts.
 */
public interface NotificationBuilder {

  /**
   * Set the time that the event occurred.  Notifications in the panel are
   * sorted by this time.
   */
  NotificationBuilder setWhen(long when);

  /**
   * Set the small icon to use in the notification layouts.  Different classes of devices
   * may return different sizes.  See the UX guidelines for more information on how to
   * design these icons.
   *
   * @param icon A resource ID in the application's package of the drawble to use.
   */
  NotificationBuilder setSmallIcon(int icon);

  /**
   * A variant of {@link #setSmallIcon(int) setSmallIcon(int)} that takes an additional
   * level parameter for when the icon is a {@link android.graphics.drawable.LevelListDrawable
   * LevelListDrawable}.
   *
   * @param icon A resource ID in the application's package of the drawble to use.
   * @param level The level to use for the icon.
   *
   * @see android.graphics.drawable.LevelListDrawable
   */
  NotificationBuilder setSmallIcon(int icon, int level);

  /**
   * Set the title (first row) of the notification, in a standard notification.
   */
  NotificationBuilder setContentTitle(CharSequence title);

  /**
   * Set the text (second row) of the notification, in a standard notification.
   */
  NotificationBuilder setContentText(CharSequence text);

  /**
   * Set the large number at the right-hand side of the notification.  This is
   * equivalent to setContentInfo, although it might show the number in a different
   * font size for readability.
   */
  NotificationBuilder setNumber(int number);

  /**
   * Set the large text at the right-hand side of the notification.
   */
  NotificationBuilder setContentInfo(CharSequence info);

  /**
   * Set the progress this notification represents, which may be
   * represented as a {@link android.widget.ProgressBar}.
   */
  NotificationBuilder setProgress(int max, int progress, boolean indeterminate);

  /**
   * Supply a custom RemoteViews to use instead of the standard one.
   */
  NotificationBuilder setContent(RemoteViews views);

  /**
   * Supply a {@link PendingIntent} to send when the notification is clicked.
   * If you do not supply an intent, you can now add PendingIntents to individual
   * views to be launched when clicked by calling {@link RemoteViews#setOnClickPendingIntent
   * RemoteViews.setOnClickPendingIntent(int,PendingIntent)}.
   */
  NotificationBuilder setContentIntent(PendingIntent intent);

  /**
   * Supply a {@link PendingIntent} to send when the notification is cleared by the user
   * directly from the notification panel.  For example, this intent is sent when the user
   * clicks the "Clear all" button, or the individual "X" buttons on notifications.  This
   * intent is not sent when the application calls {@link android.app.NotificationManager#cancel(int)}.
   */
  NotificationBuilder setDeleteIntent(PendingIntent intent);

  /**
   * An intent to launch instead of posting the notification to the status bar.
   * Only for use with extremely high-priority notifications demanding the user's
   * <strong>immediate</strong> attention, such as an incoming phone call or
   * alarm clock that the user has explicitly set to a particular time.
   * If this facility is used for something else, please give the user an option
   * to turn it off and use a normal notification, as this can be extremely
   * disruptive.
   *
   * @param intent The pending intent to launch.
   * @param highPriority Passing true will cause this notification to be sent
   *          even if other notifications are suppressed.
   */
  NotificationBuilder setFullScreenIntent(PendingIntent intent, boolean highPriority);

  /**
   * Set the text that is displayed in the status bar when the notification first
   * arrives.
   */
  NotificationBuilder setTicker(CharSequence tickerText);

  /**
   * Set the text that is displayed in the status bar when the notification first
   * arrives, and also a RemoteViews object that may be displayed instead on some
   * devices.
   */
  NotificationBuilder setTicker(CharSequence tickerText, RemoteViews views);

  /**
   * Set the large icon that is shown in the ticker and notification.
   */
  NotificationBuilder setLargeIcon(Bitmap icon);

  /**
   * Set the sound to play.  It will play on the default stream.
   */
  NotificationBuilder setSound(Uri sound);

  /**
   * Set the sound to play.  It will play on the stream you supply.
   *
   * @see #STREAM_DEFAULT
   * @see android.media.AudioManager for the <code>STREAM_</code> constants.
   */
  NotificationBuilder setSound(Uri sound, int streamType);

  /**
   * Set the vibration pattern to use.
   *
   * @see android.os.Vibrator for a discussion of the <code>pattern</code>
   * parameter.
   */
  NotificationBuilder setVibrate(long[] pattern);

  /**
   * Set the argb value that you would like the LED on the device to blnk, as well as the
   * rate.  The rate is specified in terms of the number of milliseconds to be on
   * and then the number of milliseconds to be off.
   */
  NotificationBuilder setLights(int argb, int onMs, int offMs);

  /**
   * Set whether this is an ongoing notification.
   *
   * <p>Ongoing notifications differ from regular notifications in the following ways:
   * <ul>
   *   <li>Ongoing notifications are sorted above the regular notifications in the
   *   notification panel.</li>
   *   <li>Ongoing notifications do not have an 'X' close button, and are not affected
   *   by the "Clear all" button.
   * </ul>
   */
  NotificationBuilder setOngoing(boolean ongoing);

  /**
   * Set this flag if you would only like the sound, vibrate
   * and ticker to be played if the notification is not already showing.
   */
  NotificationBuilder setOnlyAlertOnce(boolean onlyAlertOnce);

  /**
   * Setting this flag will make it so the notification is automatically
   * canceled when the user clicks it in the panel.  The PendingIntent
   * set with {@link #setDeleteIntent} will be broadcast when the notification
   * is canceled.
   */
  NotificationBuilder setAutoCancel(boolean autoCancel);

  /**
   * Set the default notification options that will be used.
   * <p>
   * The value should be one or more of the following fields combined with
   * bitwise-or:
   * {@link #DEFAULT_SOUND}, {@link #DEFAULT_VIBRATE}, {@link #DEFAULT_LIGHTS}.
   * <p>
   * For all default values, use {@link #DEFAULT_ALL}.
   */
  NotificationBuilder setDefaults(int defaults);

  /**
   * Combine all of the options that have been set and return a new {@link Notification}
   * object.
   */
  Notification getNotification();

  /** @return Android API version that is used by this builder */
  int getApiVersion();

}
