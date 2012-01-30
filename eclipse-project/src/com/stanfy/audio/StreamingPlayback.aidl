package com.stanfy.audio;

import android.os.Bundle;
import android.net.Uri;
import com.stanfy.audio.StreamingPlaybackListener;

interface StreamingPlayback {

  void play(in Uri streamUri);
  void stop();
  void pause();
  void resume();
  void seekTo(in int position);

  int getBitrate();
  Uri getCurrentUrl();
  int getVolume();
  void setVolume(in int value);
  boolean isPreparing();
  boolean isPlaying();
  boolean isPaused();

  int getDuration();
  int getPosition();

  void registerListener(in StreamingPlaybackListener listener);
  void removeListener();

  Bundle getCurrentTrackInfo();

}
