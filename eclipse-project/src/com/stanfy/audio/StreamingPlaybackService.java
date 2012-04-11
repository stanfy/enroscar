package com.stanfy.audio;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.net.wifi.WifiManager.WifiLock;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.utils.AppUtils;
import com.stanfy.views.R;

/**
 * Service for playing music in background. Successfully used for streaming radio.
 * Requires <code>android.permission.WAKE_LOCK"</code> permission.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class StreamingPlaybackService extends Service implements OnPreparedListener, OnErrorListener, OnCompletionListener {

  /** Logging tag. */
  protected static final String TAG = "StreamService";
  /** Debug flag. */
  protected static final boolean DEBUG = DebugFlags.DEBUG_SERVICES;

  /** Metadata pattern. */
  private static final Pattern METADATA_PATTERN = Pattern.compile(".*StreamTitle='(.+?)\\s-\\s(.+?)\\s?(\\[(.+)\\])?';.*");
  /** Message code. */
  private static final int MSG_META_UPDATED = 1;

  /** Volume base. */
  private static final double VOLUME_BASE = Math.log(0.95);

  /** Notification ID. */
  public static final int NOTIFICATION_ID = R.id.audio_notification;

  /** Play action. */
  public static final String ACTION_PLAY = "com.stanfy.audio.streaming.PLAY";
  /** Stop action. */
  public static final String ACTION_STOP = "com.stanfy.audio.streaming.STOP";
  /** Pause action. */
  public static final String ACTION_PAUSE = "com.stanfy.audio.streaming.PAUSE";
  /** Resume action. */
  public static final String ACTION_RESUME = "com.stanfy.audio.streaming.RESUME";

  /** Extras name. */
  public static final String EXTRA_BITRATE = "bitrate",
                             EXTRA_VOLUME = "volume",
                             EXTRA_TRACK_INFO = "track_info",
                             EXTRA_TRACK_TITLE = "title",
                             EXTRA_TRACK_ALBUM = "album",
                             EXTRA_TRACK_AUTHOR = "author",
                             EXTRA_TRACK_URL = "url";

  /** Default volume. */
  public static final int DEFAULT_VOLUME = 75;

  /** Binder. */
  private final StreamingPlaybackImpl interfaceImpl = createPlaybackImpl();

  /** Stream URL. */
  Uri streamUrl;
  /** Current bitrate. */
  int bitrate = -1;
  /** Current volume. */
  int volume = DEFAULT_VOLUME, lastSavedVolume = -1;
  /** Preparing flag. */
  boolean preparing = false;
  /** Paused flag. */
  private boolean paused = false;
  /** Last stream volume. */
  private int lastStreamVolume = -1;

  /** Media player. */
  private MediaPlayer mediaPlayer;
  /** Wi-Fi lock. */
  private WifiLock wifiLock;
  /** Notifications manager. */
  private NotificationManager notificationManager;

  /** Listener. */
  private StreamingPlaybackListener listener;

  /** Status bar notification. */
  private final boolean notifyStatusBar;
  /** Get info option. */
  private final boolean getInfo;

  /** Allow pause flag. */
  private boolean allowPause;

  /** Current data. */
  private String title, author, album;

  /** Info getter. */
  private GetAudioInfoThread infoGetter;

  /** Thread handler. */
  private InternalHandler handler;

  /** Audio focus helper. */
  private AudioFocusProcessor audioHelper;

  /** Current volume task. */
  FadeVolumeTask currentVolumeTask;

  public StreamingPlaybackService() {
    this(true, true);
  }

  protected StreamingPlaybackService(final boolean notifyStatusBar, final boolean getInfo) {
    this.notifyStatusBar = notifyStatusBar;
    this.getInfo = getInfo;
  }

  @Override
  public void onCreate() {
    if (DEBUG) { Log.v(TAG, "Streaming service is being created"); }
    super.onCreate();
    wifiLock = ((WifiManager)getSystemService(Context.WIFI_SERVICE)).createWifiLock(WifiManager.WIFI_MODE_FULL, TAG);
    notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
    handler = new InternalHandler();
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.FROYO) {
      audioHelper = new AudioFocusHelper(this);
    } else {
      audioHelper = new OldApiAudioFocusHelper(this);
    }
    final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
    volume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    final int c100 = 100;
    volume = (int)((float)volume / audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC) * c100);
    if (DEBUG) { Log.d(TAG, "Volume: " + volume); }
  }

  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId) {
    if (DEBUG) { Log.v(TAG, "Start command " + intent); }
    if (intent == null) { return START_STICKY; }
    final String action = intent.getAction();
    if (action == null) { return START_STICKY; }

    if (ACTION_PLAY.equals(action)) {
      final int bitrate = intent.getIntExtra(EXTRA_BITRATE, -1);
      if (bitrate != -1) { this.bitrate = bitrate; }
      final int volume = intent.getIntExtra(EXTRA_VOLUME, -1);
      if (volume != -1) { this.volume = volume; }
      if (intent.hasExtra(EXTRA_TRACK_INFO)) {
        final Bundle trackInfo = intent.getBundleExtra(EXTRA_TRACK_INFO);
        this.album = trackInfo.getString(EXTRA_TRACK_ALBUM);
        this.author = trackInfo.getString(EXTRA_TRACK_AUTHOR);
        this.title = trackInfo.getString(EXTRA_TRACK_TITLE);
      }
      play(intent.getData());
    } else if (ACTION_STOP.equals(action)) {
      stop();
    } else if (ACTION_PAUSE.equals(action)) {
      pause();
    } else if (ACTION_RESUME.equals(action)) {
      resume();
    }

    return START_STICKY;
  }

  @Override
  public void onDestroy() {
    if (DEBUG) { Log.v(TAG, "Steaming service is being destroyed"); }
    stop();
    listener = null;
    audioHelper = null;
    super.onDestroy();
  }

  @Override
  public IBinder onBind(final Intent intent) {
    if (intent != null) {
      return interfaceImpl.asBinder();
    }
    return null;
  }

  protected StreamingPlaybackImpl createPlaybackImpl() { return new StreamingPlaybackImpl(this); }

  /** @param allowPause the allowPause to set */
  public void setAllowPause(final boolean allowPause) { this.allowPause = allowPause; }

  @Override
  public void onPrepared(final MediaPlayer mp) {
    if (mp == mediaPlayer) {
      start();
      if (listener != null) {
        try {
          listener.onPrepared();
        } catch (final RemoteException e) {
          Log.e(TAG, "Cannot notify", e);
        }
      }
    }
  }

  @Override
  public boolean onError(final MediaPlayer mp, final int what, final int extra) {
    if (mp == mediaPlayer) {
      if (listener != null) {
        try {
          listener.onError();
        } catch (final RemoteException e) {
          Log.e(TAG, "Cannot notify", e);
        }
      }
      stop();
    }
    return false;
  }

  @Override
  public void onCompletion(final MediaPlayer mp) {
    if (mp == mediaPlayer) {
      if (listener != null) {
        try {
          listener.onCompleted();
        } catch (final RemoteException e) {
          Log.e(TAG, "Cannot notify", e);
        }
      }
      stop();
    }
  }

  protected void notifyAudioTrack() {
    if (listener != null) {
      try {
        listener.onAudioInfo(title, author, album);
      } catch (final RemoteException e) {
        Log.e(TAG, "Cannot notify", e);
      }
    }
    if (notifyStatusBar) {
      notificationManager.cancel(NOTIFICATION_ID);
      notificationManager.notify(NOTIFICATION_ID, setupNotification(buildNotification()));
    }
  }

  protected Notification buildNotification() {
    final String text = title == null ? "Playing" : "Playing " + title;
    final ApplicationInfo appInfo = getApplicationInfo();
    return AppUtils.getSdkDependentUtils().createNotificationBuilder(this)
        .setSmallIcon(appInfo.icon)
        .setTicker(text)
        .setOngoing(true)
        .setContentTitle(getPackageManager().getApplicationLabel(appInfo))
        .setContentText(text)
        .getNotification();
  }

  /** @return the mediaPlayer */
  protected MediaPlayer getMediaPlayer() { return mediaPlayer; }

  protected Intent getControlActivityIntent() { return null; }

  protected Notification setupNotification(final Notification n) {
    final Intent controlIntent = getControlActivityIntent();
    if (controlIntent != null) {
      n.contentIntent = PendingIntent.getActivity(this, 0, controlIntent, PendingIntent.FLAG_CANCEL_CURRENT);
      return n;
    }

    final String action = mediaPlayer.isPlaying() ? ACTION_PAUSE : ACTION_RESUME;
    n.contentIntent = PendingIntent.getService(this, 0, new Intent(this, getClass()).setAction(action), PendingIntent.FLAG_CANCEL_CURRENT);
    return n;
  }

  private void ensureMediaPlayer() {
    if (mediaPlayer == null) {
      mediaPlayer = new MediaPlayer();
      mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
      mediaPlayer.setOnErrorListener(this);
      mediaPlayer.setOnCompletionListener(this);
      mediaPlayer.setOnPreparedListener(this);
      mediaPlayer.setWakeMode(getApplicationContext(), PowerManager.PARTIAL_WAKE_LOCK);
    }
  }

  protected float trasformVolume() {
    final int max = 100;
    return (float)Math.exp((max - volume) * VOLUME_BASE);
  }

  protected void volumeSetup() {
    if (mediaPlayer != null) {
      final float v = trasformVolume();
      if (DEBUG) { Log.v(TAG, "Set volume " + v + "/" + volume); }
      mediaPlayer.setVolume(v, v);
      if (listener != null) {
        try {
          listener.onVolumeChanged(volume);
        } catch (final RemoteException e) {
          Log.e(TAG, "Cannot notify volume change", e);
        }
      }
    }
  }

  protected void start() {
    preparing = false;
    volumeSetup();
    mediaPlayer.start();
    startForeground(NOTIFICATION_ID, setupNotification(buildNotification()));
  }

  protected void stopInfoGetter() {
    if (infoGetter != null) {
      infoGetter.interrupt();
      infoGetter = null;
    }
  }

  protected void startInfoGetter() {
    if (getInfo && infoGetter == null) {
      infoGetter = new GetAudioInfoThread(this);
      infoGetter.start();
    }
  }

  protected String prepareUrl(final Uri uri) {
    String convertedUrl = uri.toString();
    // It's strange but media player can't play some encoded URIs
    if (convertedUrl.startsWith("file://")) {
      convertedUrl = Uri.decode(convertedUrl);
      if (DEBUG) { Log.d(TAG, "Decoded URI: " + convertedUrl); }
    }
    return convertedUrl;
  }

  public void play(final Uri url) {
    killCurrentMedaiPlayer();
    ensureMediaPlayer();
    streamUrl = url;
    stopInfoGetter();
    if (url == null) { return; }
    try {
      mediaPlayer.setDataSource(prepareUrl(url));
      mediaPlayer.prepareAsync();
      preparing = true;
      if (!wifiLock.isHeld()) { wifiLock.acquire(); }
      if (listener != null) { listener.onPreparing(); }
      if (DEBUG) { Log.i(TAG, "Start " + url); }
      startInfoGetter();
      if (audioHelper != null) { audioHelper.requestFocus(); }
    } catch (final IOException e) {
      Log.e(TAG, "Cannot setup url " + url, e);
    } catch (final RemoteException e) {
      Log.e(TAG, "Cannot notify", e);
    }
  }

  protected void killCurrentMedaiPlayer() {
    preparing = false;
    paused = false;
    if (mediaPlayer != null) {
      mediaPlayer.release();
      mediaPlayer = null;
    }
  }

  public void stop() {
    if (mediaPlayer != null) {
      stopForeground(true);
      if (mediaPlayer.isPlaying()) { mediaPlayer.stop(); }
      if (wifiLock.isHeld()) { wifiLock.release(); }
      resetAudioInfo();
      killCurrentMedaiPlayer();
      stopInfoGetter();
      stopSelf();
    }
  }

  public void pause() {
    if (!isPlayerReady()) { return; }
    if (allowPause) {
      paused = true;
      mediaPlayer.pause();
    } else {
      paused = false;
      mediaPlayer.stop();
    }
    notifyAudioTrack();
    stopForeground(false);
    stopInfoGetter();
  }

  public void resume() {
    if (!isPlayerReady()) { return; }
    paused = false;
    if (allowPause) {
      mediaPlayer.start();
      startInfoGetter();
    } else {
      play(streamUrl);
    }
  }

  protected boolean isPlayerReady() { return mediaPlayer != null && streamUrl != null && !preparing; }

  public void seekTo(final int position) {
    if (isPlayerReady()) {
      mediaPlayer.seekTo(position);
    }
  }

  public void setListener(final StreamingPlaybackListener listener) { this.listener = listener; }

  public void resetAudioInfo() {
    title = null;
    author = null;
    album = null;
  }

  public void updateAudioInfo(final String metadata) {
    if (DEBUG) { Log.i(TAG, metadata); }
    final Matcher m = METADATA_PATTERN.matcher(metadata);
    if (m.matches()) {
      author = m.group(1);
      title = m.group(2);
      final int c4 = 4;
      album = m.group(c4);
    } else {
      resetAudioInfo();
    }
    handler.sendEmptyMessage(MSG_META_UPDATED);
  }

  public void notifyListener() {
    if (listener != null) {
      try {
        listener.onAudioInfo(title, author, album);
      } catch (final RemoteException e) {
        Log.e(TAG, "Cannot notify", e);
      }
    }
  }

  public void calmDown() {
    if (mediaPlayer != null) {
      lastSavedVolume = volume;
      final int duration = 1500;
      if (currentVolumeTask != null) { currentVolumeTask.cancel(); }
      currentVolumeTask = new FadeVolumeTask(FadeVolumeTask.MODE_FADE_OUT, duration);
    }
  }
  protected void restoreVolume() {
    if (mediaPlayer != null) {
      if (currentVolumeTask != null) { currentVolumeTask.cancel(); }
      if (lastSavedVolume > 0) {
        final int duration = 5000;
        currentVolumeTask = new FadeVolumeTask(FadeVolumeTask.MODE_FADE_IN, duration);
      }
    }
  }

  public void setVolume(final int value) {
    this.volume = value;
    volumeSetup();
  }

  /** @return the title */
  public String getTitle() { return title; }
  /** @return the album */
  public String getAlbum() { return album; }
  /** @return the author */
  public String getAuthor() { return author; }

  /** @return duration in milliseconds */
  public int getDuration() { return isPlayerReady() ? mediaPlayer.getDuration() : 0; }
  /** @return current position in milliseconds */
  public int getPosition() { return isPlayerReady() ? mediaPlayer.getCurrentPosition() : 0; }

  /** @return  */
  public boolean isPlaying() { return isPlayerReady() && mediaPlayer.isPlaying(); }
  /** @return the paused */
  public boolean isPaused() { return isPlayerReady() && !mediaPlayer.isPlaying() && paused; }

  @Override
  public String toString() {
    return TAG + "[" + streamUrl + "]";
  }

  /**
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  protected class InternalHandler extends Handler {
    @Override
    public void handleMessage(final Message msg) {
      switch (msg.what) {
      case MSG_META_UPDATED:
        notifyListener();
        break;
      default:
      }
    }
  }

  /**
   * Fade volume task. Inspired by lastfm:
   * https://github.com/c99koder/lastfm-android/blob/master/app/src/fm/last/android/player/RadioPlayerService.java.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  protected class FadeVolumeTask extends TimerTask {

    /** Fade in mode. */
    public static final int MODE_FADE_IN = 0;
    /** Fade out mode. */
    public static final int MODE_FADE_OUT = 1;

    /** Current step. */
    private int currentStep = 0;
    /** Step count. */
    private int steps;
    /** Current mode. */
    private int mode;
    /** Initial volume. */
    private int initialVolume;

    public FadeVolumeTask(final int mode, final int millis) {
      this.initialVolume = mode == MODE_FADE_IN ? lastSavedVolume : volume;
      final int perSecond = 20;
      this.mode = mode;
      this.steps = millis / perSecond;
      new Timer().scheduleAtFixedRate(this, 0, millis / steps);
    }

    @Override
    public void run() {
      if (currentStep == 0 && mode == MODE_FADE_IN && lastStreamVolume > 0) {
        final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, lastStreamVolume, 0);
      }

      int volumeValue = initialVolume;

      switch (mode) {
      case MODE_FADE_IN:
        volumeValue *= (float)currentStep / steps;
        break;
      case MODE_FADE_OUT:
        volumeValue *= (float)(steps - currentStep) / steps;
        break;
      default:
        throw new IllegalArgumentException("Unknown mode " + mode);
      }

      setVolume(volumeValue);

      if (currentStep >= steps || mediaPlayer == null) {
        if (mode == MODE_FADE_OUT) {
          final AudioManager audioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
          setVolume(0);
          lastStreamVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
          audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
        }
        this.cancel();
      }

      currentStep++;
    }

    @Override
    public boolean cancel() {
      final boolean res = super.cancel();
      if (mode == MODE_FADE_IN) {
        lastSavedVolume = -1;
        lastStreamVolume = -1;
      }
      currentVolumeTask = null;
      return res;
    }

  }

}
