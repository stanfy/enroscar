package com.stanfy.audio;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.media.AudioManager;

/**
 * A helper class that deals with audio focus (used for Android >=2.2).
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
class AudioFocusHelper implements AudioManager.OnAudioFocusChangeListener, AudioFocusProcessor {

  /** Audio manager. */
  private final AudioManager audioManager;

  /** Service. */
  private final WeakReference<StreamingPlaybackService> service;

  public AudioFocusHelper(final StreamingPlaybackService service) {
    audioManager = (AudioManager)service.getSystemService(Context.AUDIO_SERVICE);
    this.service = new WeakReference<StreamingPlaybackService>(service);
  }

  @Override
  public void requestFocus() {
    audioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
  }

  @Override
  public void onAudioFocusChange(final int focusChange) {
    final StreamingPlaybackService service = this.service.get();
    if (service == null) { return; }
    switch (focusChange) {
    case AudioManager.AUDIOFOCUS_LOSS:
      service.stop();
      break;
    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
      service.calmDown();
      break;
    case AudioManager.AUDIOFOCUS_GAIN:
      service.restoreVolume();
      break;
    default:
    }
  }

}
