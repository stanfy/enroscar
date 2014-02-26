package com.stanfy.enroscar.sample.other;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.os.Bundle;
import android.os.SystemClock;
import android.widget.TextView;

import com.stanfy.enroscar.net.operation.SimpleRequestBuilder;
import com.stanfy.enroscar.net.operation.executor.ServiceRequestPerformer;
import com.stanfy.enroscar.utils.Time;


/**
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class PendingRequestExample extends Activity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    TextView description = new TextView(this);
    description.setText("Request is scheduled to be executed in 1 minute, see logs");
    setContentView(description);

    SimpleRequestBuilder<String> request = new SimpleRequestBuilder<String>(this) {}
        .setUrl("https://api.twitter.com/1/statuses/user_timeline.json")
        .addParam("screen_name", "stanfy");

    ((AlarmManager) getSystemService(Context.ALARM_SERVICE)).set(
        AlarmManager.ELAPSED_REALTIME,                     // type
        SystemClock.elapsedRealtime() + 1 * Time.MINUTES,  // triggerAt
        new ServiceRequestPerformer(this).getPendingIntent(request, PendingIntent.FLAG_ONE_SHOT)
    );


  }

}
