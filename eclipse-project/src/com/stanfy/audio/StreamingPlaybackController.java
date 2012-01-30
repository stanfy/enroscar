package com.stanfy.audio;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.stanfy.DebugFlags;

/**
 * Base for streaming service controllers.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class StreamingPlaybackController {

  /** Logging tag. */
  protected static final String TAG = "StreamingController";
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_GUI;

  /** Context. */
  private final Context context;

  /** Streaming playback. */
  private StreamingPlayback streamingPlayback;

  /** Flag about requested binding. */
  private boolean bindRequested = false;

  /** State flags. */
  private boolean playing, preparing, hasInfo, paused;

  /** Current audio info. */
  private final Bundle currentAudioInfo = new Bundle();

  /** Streaming playback listener. */
  private final StreamingPlaybackListener streamingPlaybackListener = new StreamingPlaybackListener.Stub() {
    @Override
    public void onVolumeChanged(final int volume) throws RemoteException { StreamingPlaybackController.this.onVolumeChanged(volume); }
    @Override
    public void onError() throws RemoteException { StreamingPlaybackController.this.onError(); }
    @Override
    public void onPreparing() throws RemoteException {
      preparing = true;
      StreamingPlaybackController.this.onPreparingChanged();
    }
    @Override
    public void onPrepared() throws RemoteException {
      preparing = false;
      StreamingPlaybackController.this.onPreparingChanged();
      playing = true;
      StreamingPlaybackController.this.onPlayingChanged();
    }
    @Override
    public void onAudioInfo(final String title, final String author, final String album) throws RemoteException {
      currentAudioInfo.putString(StreamingPlaybackService.EXTRA_TRACK_TITLE, title);
      currentAudioInfo.putString(StreamingPlaybackService.EXTRA_TRACK_ALBUM, album);
      currentAudioInfo.putString(StreamingPlaybackService.EXTRA_TRACK_AUTHOR, author);
      StreamingPlaybackController.this.onAudioInfoChanged();
    }
    @Override
    public void onCompleted() throws RemoteException {
      playing = false;
      onPlayingChanged();
      StreamingPlaybackController.this.onCompleted();
    }
  };

  /** Service connection. */
  private final ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
      streamingPlayback = StreamingPlayback.Stub.asInterface(service);
      try {
        streamingPlayback.registerListener(streamingPlaybackListener);
        final boolean oldPlaying = playing, oldPaused = paused;
        hasInfo = streamingPlayback.getCurrentUrl() != null;
        if (hasInfo) {
          final Bundle currentInfo = streamingPlayback.getCurrentTrackInfo();
          currentAudioInfo.putAll(currentInfo);
        }
        playing = streamingPlayback.isPlaying();
        paused = streamingPlayback.isPaused();
        if (oldPlaying != playing || oldPaused != paused) { onPlayingChanged(); }
        StreamingPlaybackController.this.onServiceConnected();
      } catch (final RemoteException e) {
        if (DEBUG) { Log.e(TAG, "Cannot call playback service", e); }
      }
    }
    @Override
    public void onServiceDisconnected(final ComponentName name) {
      dropListener();
      streamingPlayback = null;
      bindRequested = false;
    }
  };

  public StreamingPlaybackController(final Context context) {
    this.context = context;
  }

  /** @return the playing */
  public boolean isPlaying() { return playing; }
  /** @return the preparing */
  public boolean isPreparing() { return preparing; }
  /** @return the hasInfo */
  public boolean isHasInfo() { return hasInfo; }
  /** @return the paused */
  public boolean isPaused() { return paused; }

  /** @param playing the playing to set */
  protected void setPlaying(final boolean playing) { this.playing = playing; }
  /** @param paused the paused to set */
  protected void setPaused(final boolean paused) { this.paused = paused; }

  public void start() {
    if (context instanceof Activity) {
      ((Activity)context).setVolumeControlStream(AudioManager.STREAM_MUSIC);
    }
    bind();
  }

  public void stop() {
    unbind();
    setPaused(false);
    setPlaying(false);
    onPlayingChanged();
  }

  /** @return the context */
  public final Context getContext() { return context; }
  /** @return the streamingPlayback */
  public final StreamingPlayback getStreamingPlayback() { return streamingPlayback; }

  /** @return the currentAudioInfo */
  public Bundle getCurrentAudioInfo() { return currentAudioInfo; }

  /** @return steaming service class */
  protected Class<? extends StreamingPlaybackService> getServiceClass() { return StreamingPlaybackService.class; }
  /** @return intent to bind to the steaming service */
  protected Intent createBindIntent() { return new Intent(context, getServiceClass()); }
  /** @return intent to call 'play' on the steaming service */
  protected Intent createPlayIntent() { return new Intent(context, getServiceClass()).setAction(StreamingPlaybackService.ACTION_PLAY); }
  /** @return intent to call 'stop' on the steaming service */
  protected Intent createStopIntent() { return new Intent(context, getServiceClass()).setAction(StreamingPlaybackService.ACTION_STOP); }

  /** Play the track. */
  protected void callPlay(final Uri uri, final int volume, final Bundle params) {
    playing = true;
    paused = false;
    context.startService(
        createPlayIntent().setData(uri)
        .putExtra(StreamingPlaybackService.EXTRA_VOLUME, volume)
        .putExtra(StreamingPlaybackService.EXTRA_TRACK_INFO, params)
    );
    bind();
    onPlayingChanged();
    preparing = true;
    onPreparingChanged();
  }

  /** Stop playback. */
  protected void callStop() {
    playing = false;
    paused = false;
    context.startService(createStopIntent());
    onPlayingChanged();
    preparing = false;
    onPreparingChanged();
  }

  protected void callResume() {
    if (streamingPlayback != null) {
      try {
        streamingPlayback.resume();
        playing = true;
        paused = false;
        onPlayingChanged();
      } catch (final RemoteException e) {
        Log.e(TAG, "Cannot call resume", e);
      }
    }
  }

  protected void callPause() {
    if (streamingPlayback != null) {
      try {
        streamingPlayback.pause();
        playing = false;
        paused = true;
        onPlayingChanged();
      } catch (final RemoteException e) {
        Log.e(TAG, "Cannot call pause", e);
      }
    }
  }

  /**
   * Bind to the streaming service.
   */
  protected final void bind() {
    if (bindRequested) { return; }
    final boolean result = context.bindService(createBindIntent(), serviceConnection, 0);
    if (DEBUG) { Log.v(TAG, "Bind to streaming service: " + result); }
    bindRequested = result;
    onBind(result);
  }

  /**
   * Unbind from the streaming service.
   */
  protected final void unbind() {
    if (streamingPlayback != null) { dropListener(); }
    try {
      context.unbindService(serviceConnection);
    } catch (final RuntimeException e) {
      if (DEBUG) { Log.w(TAG, "Cannot unbind radio", e); }
    }
    bindRequested = false;
  }

  /**
   * Drop streaming service listener.
   */
  protected final void dropListener() {
    if (DEBUG) { Log.v(TAG, "Drop listener"); }
    if (streamingPlayback != null) {
      try {
        streamingPlayback.removeListener();
      } catch (final RemoteException e) {
        Log.e(TAG, "Cannot remove listener", e);
      }
      bindRequested = false;
    }
  }

  protected void onCompleted() { /* nothing */ }

  protected void onPlayingChanged() { /* nothing */ }

  protected void onError() { /* nothing */ }

  protected void onAudioInfoChanged() { /* nothing */ }

  protected void onVolumeChanged(final int value) { /* nothing */ }

  protected void onPreparingChanged() { /* nothing */ }

  protected void onServiceConnected() { /* nothing */ }

  protected void onBind(final boolean result) { /* nothing */ }

}
