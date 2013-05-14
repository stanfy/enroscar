package com.stanfy.audio;

import android.net.Uri;

interface StreamingPlaybackListener {

  void onAudioInfo(in String title, in String author, in String album);
  
  void onVolumeChanged(in int volume);
  
  void onPreparing();
  
  void onPrepared();

  void onError();

  void onCompleted();

}
