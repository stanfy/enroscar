package com.stanfy.audio;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 *
 */
class OldApiAudioFocusHelper implements AudioFocusProcessor {

  /** Service. */
  private final WeakReference<StreamingPlaybackService> service;

  /** Listener. */
  private final PhoneStateListener listener = new PhoneStateListener() {
    @Override
    public void onCallStateChanged(final int state, final String incomingNumber) {
      final StreamingPlaybackService service = OldApiAudioFocusHelper.this.service.get();
      if (service == null) { return; }
      if (state == TelephonyManager.CALL_STATE_IDLE) {
        service.restoreVolume();
      } else {
        service.calmDown();
      }
    }
  };

  public OldApiAudioFocusHelper(final StreamingPlaybackService service) {
    this.service = new WeakReference<StreamingPlaybackService>(service);
  }

  @Override
  public void requestFocus() {
    final StreamingPlaybackService service = this.service.get();
    if (service == null) { return; }
    final TelephonyManager telephonyManager = (TelephonyManager)service.getSystemService(Context.TELEPHONY_SERVICE);
    telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
  }

}
