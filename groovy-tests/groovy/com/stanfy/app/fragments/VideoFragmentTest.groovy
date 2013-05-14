package com.stanfy.app.fragments

import org.junit.Test

import com.stanfy.test.AbstractGroovyEnroscarTest

import android.net.Uri

/**
 * Video fragment tests.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
class VideoFragmentTest extends AbstractGroovyEnroscarTest {

  @Test
  void notLocalUriShouldNotRequireNetwork() {
    VideoPlayFragment fragment = VideoPlayFragment.create(Uri.parse("file:///android_assets"))
    assertThat fragment.networkRequired, equalTo(false)
  }

  @Test
  void httpUriShouldRequireNetwork() {
    VideoPlayFragment fragment = VideoPlayFragment.create(Uri.parse("http://kinopoisk.ru/somevideo.mp4"))
    assertThat fragment.networkRequired, equalTo(true)
  }

}
