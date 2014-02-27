package com.stanfy.enroscar.sample.data.remote;

import com.stanfy.enroscar.sample.data.Tweet;

import java.util.List;

import retrofit.http.GET;
import retrofit.http.Headers;
import retrofit.http.Query;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public interface TwitterApi {

  @Headers({
      "Authorization: Bearer AAAAAAAAAAAAAAAAAAAAAJ3PVgAAAAAA%2BPcuieQDv6hOE9SyWM2AOWjIloc%3DU2fCluURs5dnG5A3WaaVhNgiBjXKkV5lynvoquGu7ediOCRWiF"
  })
  @GET("/statuses/user_timeline.json")
  List<Tweet> tweets(@Query("screen_name") String username);

}
