package com.stanfy.audio;

import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;

import com.stanfy.audio.StreamingPlaybackService.FadeVolumeTask;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
class StreamingPlaybackImpl extends StreamingPlayback.Stub {

  /** Service instance. */
  private final StreamingPlaybackService service;

  public StreamingPlaybackImpl(final StreamingPlaybackService service) {
    this.service = service;
  }

  @Override
  public void play(final Uri streamUri) throws RemoteException {
    service.play(streamUri);
  }

  @Override
  public void stop() throws RemoteException {
    service.stop();
  }

  @Override
  public void pause() throws RemoteException {
    service.pause();
  }

  @Override
  public void seekTo(final int position) throws RemoteException {
    service.seekTo(position);
  }

  @Override
  public void registerListener(final StreamingPlaybackListener listener) throws RemoteException {
    service.setListener(listener);
    service.notifyListener();
  }

  @Override
  public void resume() throws RemoteException {
    service.resume();
  }

  @Override
  public void removeListener() throws RemoteException {
    service.setListener(null);
  }

  @Override
  public int getBitrate() throws RemoteException { return service.bitrate; }

  @Override
  public Uri getCurrentUrl() throws RemoteException { return service.streamUrl; }

  @Override
  public int getVolume() throws RemoteException { return service.volume; }

  @Override
  public void setVolume(final int value) throws RemoteException {
    service.setVolume(value);
    final FadeVolumeTask task = service.currentVolumeTask;
    if (task != null) { task.cancel(); }
  }

  @Override
  public boolean isPreparing() throws RemoteException { return service.preparing; }

  @Override
  public Bundle getCurrentTrackInfo() throws RemoteException {
    final Bundle bundle = new Bundle();
    if (service.getAlbum() != null) {
      bundle.putString(StreamingPlaybackService.EXTRA_TRACK_ALBUM, service.getAlbum());
    }
    if (service.getAuthor() != null) {
      bundle.putString(StreamingPlaybackService.EXTRA_TRACK_AUTHOR, service.getAuthor());
    }
    if (service.getTitle() != null) {
      bundle.putString(StreamingPlaybackService.EXTRA_TRACK_TITLE, service.getTitle());
    }
    final Uri uri = getCurrentUrl();
    if (uri != null) {
      bundle.putParcelable(StreamingPlaybackService.EXTRA_TRACK_URL, uri);
    }
    return bundle;
  }

  @Override
  public int getDuration() throws RemoteException { return service.getDuration(); }
  @Override
  public int getPosition() throws RemoteException { return service.getPosition(); }

  @Override
  public boolean isPlaying() throws RemoteException { return service.isPlaying(); }

  @Override
  public boolean isPaused() throws RemoteException { return service.isPaused(); }

}
