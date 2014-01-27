package com.stanfy.enroscar.goro.sample;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;

import com.stanfy.enroscar.goro.Goro;
import com.stanfy.enroscar.goro.GoroListener;
import com.stanfy.enroscar.goro.GoroService;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
  private Goro goro;

  private GoroView goroView;
  private View restButton;
  private View dbButton;

  /** Tasks data. */
  private LinkedHashMap<String, List<Integer>> data = new LinkedHashMap<>();

  private final ServiceConnection serviceConnection = new ServiceConnection() {
    @Override
    public void onServiceConnected(final ComponentName name, final IBinder service) {
      goro = Goro.from(service);
      goro.addTaskListener(listener);
      setupButtons();
    }

    @Override
    public void onServiceDisconnected(final ComponentName name) {
    }
  };

  /** Goro tasks listener. */
  private final GoroListener listener = new GoroListener() {
    @Override
    public void onTaskStart(Callable<?> task) { }

    @Override
    public void onTaskFinish(Callable<?> task, Object result) {
      int n = ((SimpleTask) task).getNumber();
      for (Map.Entry<String, List<Integer>> entry : data.entrySet()) {
        entry.getValue().remove(Integer.valueOf(n));
      }
      update();
    }

    @Override
    public void onTaskCancel(Callable<?> task) { }

    @Override
    public void onTaskError(Callable<?> task, Throwable error) { }
  };

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.goro);

    goroView = (GoroView) findViewById(R.id.goro);

    restButton = findViewById(R.id.button_post_rest);
    restButton.setOnClickListener(new Clicker(QUEUE_REST));
    dbButton = findViewById(R.id.button_post_db);
    dbButton.setOnClickListener(new Clicker(QUEUE_DB));

    findViewById(R.id.button_post_notification).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(final View v) {
        List<Integer> tasks = data.get(QUEUE_REST);
        if (tasks == null) {
          tasks = new LinkedList<>();
          data.put(QUEUE_REST, tasks);
        }
        tasks.add(counter);
        update();

        Intent intent = GoroService.taskIntent(GoroActivity.this, QUEUE_REST,
            new PendingTask(counter++));
        PendingIntent pendingIntent = PendingIntent.getService(GoroActivity.this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT);
        ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).notify(
            1,
            new NotificationCompat.Builder(GoroActivity.this)
                .setTicker("Click to post to REST")
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(true)
                .setContentTitle("Click to post to REST")
                .setContentText("Intent will be sent to the service")
                .setContentIntent(pendingIntent)
                .build()
        );
      }
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
    GoroService.bind(this, serviceConnection);
  }

  @Override
  protected void onStop() {
    super.onStop();
    GoroService.unbind(this, serviceConnection);
    goro = null;
    setupButtons();
  }

  private void setupButtons() {
    restButton.setEnabled(goro != null);
    dbButton.setEnabled(goro != null);
  }

  private void update() {
    goroView.setData(new LinkedHashMap<>(data));
  }

  /** Schedules tasks. */
  private class Clicker implements View.OnClickListener {
    private final String queue;

    public Clicker(String queue) {
      this.queue = queue;
    }

    @Override
    public void onClick(View v) {
      List<Integer> tasks = data.get(queue);
      if (tasks == null) {
        tasks = new LinkedList<>();
        data.put(queue, tasks);
      }
      tasks.add(counter);
      update();
      goro.schedule(queue, new SimpleTask(counter++));
    }
  }

  /** Stub task. */
  public static class SimpleTask implements Callable<Integer> {
    final int number;

    private SimpleTask(int number) {
      this.number = number;
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
        int number = source.readInt();
        return new PendingTask(number);
      }

      @Override
      public PendingTask[] newArray(int size) {
        return new PendingTask[size];
      }
    };

    private PendingTask(int number) {
      super(number);
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      dest.writeInt(number);
    }
  }

}
