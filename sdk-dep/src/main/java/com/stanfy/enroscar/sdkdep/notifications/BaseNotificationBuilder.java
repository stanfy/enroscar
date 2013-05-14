package com.stanfy.enroscar.sdkdep.notifications;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import com.stanfy.enroscar.sdkdep.R;

/**
 * Builder for older versions.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@TargetApi(Build.VERSION_CODES.ECLAIR_MR1)
public class BaseNotificationBuilder implements NotificationBuilder {

  /** Logging tag. */
  protected static final String TAG = "NotificationBuilder";

  /** Context instance. */
  private final Context context;

  /** Notification time. */
  private long when;
  /** Icon resource. */
  private int smallIcon;
  /** Icon drawable level. */
  private int smallIconLevel;
  /** Count of events. */
  private int number;
  /** Content title. */
  private CharSequence contentTitle;
  /** Description. */
  private CharSequence contentText;
  /** Action intent. */
  private PendingIntent contentIntent;
  /** Remote views instance. */
  private RemoteViews contentView;
  /** Delete action intent. */
  private PendingIntent deleteIntent;
  /** Ticker text. */
  private CharSequence tickerText;
  /** Notification sound. */
  private Uri sound;
  /** Notification stream type. */
  private int audioStreamType;
  /** Vibration info. */
  private long[] vibrate;
  /** LED color. */
  private int ledArgb;
  /** LED duration. */
  private int ledOnMs;
  /** LED duration. */
  private int ledOffMs;
  /** Defaults. */
  private int defaults;
  /** Flags. */
  private int flags;
  /** Progress - maximum value. */
  private int progressMax = -1;
  /** Progress - current value. */
  private int progress = -1;
  /** Indeterminate progress. */
  private boolean progressIndeterminate;

  /**
   * Constructor.
   *
   * Automatically sets the when field to {@link System#currentTimeMillis()
   * System.currentTimeMllis()} and the audio stream to the {@link #STREAM_DEFAULT}.
   *
   * @param context A {@link Context} that will be used to construct the
   *      RemoteViews. The Context will not be held past the lifetime of this
   *      Builder object.
   */
  public BaseNotificationBuilder(final Context context) {
    this.context = context;

    // Set defaults to match the defaults of a Notification
    this.when = System.currentTimeMillis();
    this.audioStreamType = Notification.STREAM_DEFAULT;
  }

  @Override
  public NotificationBuilder setWhen(final long when) {
    this.when = when;
    return this;
  }

  @Override
  public NotificationBuilder setSmallIcon(final int icon) {
    this.smallIcon = icon;
    return this;
  }

  @Override
  public NotificationBuilder setSmallIcon(final int icon, final int level) {
    this.smallIcon = icon;
    this.smallIconLevel = level;
    return this;
  }

  @Override
  public NotificationBuilder setContentTitle(final CharSequence title) {
    this.contentTitle = title;
    return this;
  }

  @Override
  public NotificationBuilder setContentText(final CharSequence text) {
    this.contentText = text;
    return this;
  }

  @Override
  public NotificationBuilder setNumber(final int number) {
    this.number = number;
    return this;
  }

  @Override
  public NotificationBuilder setContentInfo(final CharSequence info) {
    Log.w(TAG, "setContentInfo is not supported till Honeycomb");
    return this;
  }

  @Override
  public NotificationBuilder setProgress(final int max, final int progress, final boolean indeterminate) {
    this.progressMax = max;
    this.progress = progress;
    this.progressIndeterminate = indeterminate;
    return this;
  }

  @Override
  public NotificationBuilder setContent(final RemoteViews views) {
    this.contentView = views;
    return this;
  }

  @Override
  public NotificationBuilder setContentIntent(final PendingIntent intent) {
    this.contentIntent = intent;
    return this;
  }

  @Override
  public NotificationBuilder setDeleteIntent(final PendingIntent intent) {
    this.deleteIntent = intent;
    return this;
  }

  @Override
  public NotificationBuilder setFullScreenIntent(final PendingIntent intent, final boolean highPriority) {
    Log.w(TAG, "setFullScreenIntent is not supported by Eclair");
    return this;
  }

  @Override
  public NotificationBuilder setTicker(final CharSequence tickerText) {
    this.tickerText = tickerText;
    return this;
  }

  @Override
  public NotificationBuilder setTicker(final CharSequence tickerText, final RemoteViews views) {
    this.tickerText = tickerText;
    Log.w(TAG, "Ticker view is not supported till Honeycomb");
    return this;
  }

  @Override
  public NotificationBuilder setLargeIcon(final Bitmap icon) {
    Log.w(TAG, "setLargeIcon is not supported till Honeycomb");
    return this;
  }

  @Override
  public NotificationBuilder setSound(final Uri sound) {
    this.sound = sound;
    this.audioStreamType = Notification.STREAM_DEFAULT;
    return this;
  }

  @Override
  public NotificationBuilder setSound(final Uri sound, final int streamType) {
    this.sound = sound;
    this.audioStreamType = streamType;
    return this;
  }

  @Override
  public NotificationBuilder setVibrate(final long[] pattern) {
    this.vibrate = pattern;
    return this;
  }

  @Override
  public NotificationBuilder setLights(final int argb, final int onMs, final int offMs) {
    this.ledArgb = argb;
    this.ledOnMs = onMs;
    this.ledOffMs = offMs;
    return this;
  }

  @Override
  public NotificationBuilder setOngoing(final boolean ongoing) {
    setFlag(Notification.FLAG_ONGOING_EVENT, ongoing);
    return this;
  }

  @Override
  public NotificationBuilder setOnlyAlertOnce(final boolean onlyAlertOnce) {
    setFlag(Notification.FLAG_ONLY_ALERT_ONCE, onlyAlertOnce);
    return this;
  }

  @Override
  public NotificationBuilder setAutoCancel(final boolean autoCancel) {
    setFlag(Notification.FLAG_AUTO_CANCEL, autoCancel);
    return this;
  }

  @Override
  public NotificationBuilder setDefaults(final int defaults) {
    this.defaults = defaults;
    return this;
  }

  protected void setFlag(final int mask, final boolean value) {
    if (value) {
      flags |= mask;
    } else {
      flags &= ~mask;
    }
  }

  @SuppressWarnings("deprecation")
  protected void setContentView(final Notification n) {
    if (contentView != null) {
      n.contentView = contentView;
      return;
    }
    if (progress == -1 && progressMax == -1) {
      n.setLatestEventInfo(context, contentTitle, contentText, contentIntent);
      return;
    }

    final RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.notification_download);
    remoteViews.setTextViewText(R.id.notification_title, contentTitle);
    remoteViews.setTextViewText(R.id.notification_description, contentText);
    remoteViews.setProgressBar(R.id.notification_progress, progressMax, progress, progressIndeterminate);
    n.contentView = remoteViews;
  }

  @Override
  public NotificationBuilder addAction(final int icon, final CharSequence title, final PendingIntent intent) {
    // TODO implement
    return this;
  }

  @Override
  public Notification build() {
    final Notification n = new Notification();
    n.when = when;
    n.icon = smallIcon;
    n.iconLevel = smallIconLevel;
    n.number = number;
    setContentView(n);
    n.contentIntent = contentIntent;
    n.deleteIntent = deleteIntent;
    n.tickerText = tickerText;
    n.sound = sound;
    n.audioStreamType = audioStreamType;
    n.vibrate = vibrate;
    n.ledARGB = ledArgb;
    n.ledOnMS = ledOnMs;
    n.ledOffMS = ledOffMs;
    n.defaults = defaults;
    n.flags = flags;
    if (ledOnMs != 0 && ledOffMs != 0) {
      n.flags |= Notification.FLAG_SHOW_LIGHTS;
    }
    if ((defaults & Notification.DEFAULT_LIGHTS) != 0) {
      n.flags |= Notification.FLAG_SHOW_LIGHTS;
    }
    return n;
  }

  @Override
  public int getApiVersion() { return Build.VERSION_CODES.ECLAIR_MR1; }

}
