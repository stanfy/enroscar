package com.stanfy.enroscar.goro.sample;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.stanfy.enroscar.goro.BoundGoro;
import com.stanfy.enroscar.goro.Goro;
import com.stanfy.enroscar.goro.GoroService;

import java.util.concurrent.Callable;

/**
 * Activity that demonstrates Goro.
 */
public class GoroActivity extends Activity {

  /** REST API queue. */
  private static final String QUEUE_REST = "rest";

  /** DB queue. */
  private static final String QUEUE_DB = "db";

  /** Counter for tasks. */
  private int counter = 1;

  /** Goro instance. */
  private final BoundGoro goro = Goro.bindWith(this);

  private GoroStateObserver observer;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.goro);

    if (savedInstanceState != null) {
      counter = savedInstanceState.getInt("counter");
    }

    GoroView goroView = (GoroView) findViewById(R.id.goro);
    observer = new GoroStateObserver(goroView, savedInstanceState);

    View restButton = findViewById(R.id.button_post_rest);
    restButton.setOnClickListener(new Clicker(QUEUE_REST));
    View dbButton = findViewById(R.id.button_post_db);
    dbButton.setOnClickListener(new Clicker(QUEUE_DB));
  }

  @Override
  protected void onStart() {
    super.onStart();
    goro.addTaskListener(observer);
    goro.bind();
  }

  @Override
  protected void onStop() {
    super.onStop();
    goro.removeTaskListener(observer);
    goro.unbind();
  }

  @Override
  public boolean onCreateOptionsMenu(final Menu menu) {
    getMenuInflater().inflate(R.menu.activity_goro, menu);
    return true;
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    switch (item.getItemId()) {
      case R.id.action_notification:
        notificationSample();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }

  private void notificationSample() {
    Intent intent = GoroService.taskIntent(GoroActivity.this, QUEUE_REST,
        new PendingTask(counter++, QUEUE_REST));
    PendingIntent pendingIntent = PendingIntent.getService(GoroActivity.this, 0, intent,
        PendingIntent.FLAG_UPDATE_CURRENT);
    ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(
        1,
        new Notification.Builder(GoroActivity.this)
            .setTicker("Click to post to REST")
            .setSmallIcon(R.drawable.ic_launcher)
            .setAutoCancel(true)
            .setContentTitle("Click to post to REST")
            .setContentText("Intent will be sent to the service")
            .setContentIntent(pendingIntent)
            .getNotification()
    );
  }

  @Override
  protected void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);
    observer.save(outState);
    outState.putInt("counter", counter);
  }

  /** Schedules tasks. */
  private class Clicker implements View.OnClickListener {
    private final String queue;

    public Clicker(String queue) {
      this.queue = queue;
    }

    @Override
    public void onClick(View v) {
      goro.schedule(queue, new SimpleTask(counter++, queue));
    }
  }

  /** Stub task. */
  public static class SimpleTask implements Callable<Integer> {
    final int number;
    final String queue;

    private SimpleTask(int number, String queue) {
      this.number = number;
      this.queue = queue;
    }

    public int getNumber() {
      return number;
    }

    @Override
    public Integer call() {
      Log.d("Goro", "Task " + number + " started");
      try {
        Thread.sleep((long) (Math.random() * 5000));
      } catch (InterruptedException e) {
        throw new AssertionError(e);
      }
      Log.d("Goro", "Task " + number + " completed");
      return number;
    }
  }

  /** Task instance that can be packed into an intent. */
  public static class PendingTask extends SimpleTask implements Parcelable {

    public static final Creator<PendingTask> CREATOR = new Creator<PendingTask>() {
      @Override
      public PendingTask createFromParcel(final Parcel source) {
        return new PendingTask(source.readInt(), source.readString());
      }

      @Override
      public PendingTask[] newArray(int size) {
        return new PendingTask[size];
      }
    };

    private PendingTask(int number, String queue) {
      super(number, queue);
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(number);
      dest.writeString(queue);
    }
  }

}
