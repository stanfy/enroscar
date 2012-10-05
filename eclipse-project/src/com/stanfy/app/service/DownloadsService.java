package com.stanfy.app.service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLConnection;
import java.util.LinkedList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.IBinder;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.io.BuffersPool;
import com.stanfy.io.IoUtils;
import com.stanfy.io.PoolableBufferedInputStream;
import com.stanfy.net.UrlConnectionBuilder;
import com.stanfy.utils.AppUtils;

/**
 * Service that can be used instead of {@link android.app.DownloadManager} on older devices.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class DownloadsService extends Service {

  /** Logging tag. */
  protected static final String TAG = "DownloadsService";
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_SERVICES;

  /** Enqueue action. */
  public static final String ACTION_ENQUEUE = "com.stanfy.download.action.ENQUEUE";
  /** Broadcast complete action. */
  public static final String ACTION_DOWNLOAD_COMPLETE = "com.stanfy.download.action.COMPLETE";
  /** Broadcast click on download action. */
  public static final String ACTION_DOWNLOAD_CLICK = "com.stanfy.download.action.CLICK";

  /** Extra name. */
  public static final String EXTRA_REQUEST = "request", EXTRA_ID = "download_id", EXTRA_SUCCESS = "download_success";

  /** Preference name. */
  private static final String PREF_NAME = "downloads_service_counter";
  /** Key to store the counter. */
  private static final String KEY_ID = "id";

  /** Base ID for notifications. */
  private static final int NOTIFICATION_BASE_ID = Integer.MAX_VALUE / 2;

  /** Default buffer size. */
  private static final int DEFAULT_BUFFER_SIZE = 8192;

  /** Buffers pool. */
  private final BuffersPool buffersPool = new BuffersPool(new int[][] {
      {4, DEFAULT_BUFFER_SIZE}
  });

  /** tasks. */
  private final LinkedList<DownloadTask> tasks = new LinkedList<DownloadsService.DownloadTask>();

  /** Notification manager. */
  private NotificationManager notificationManager;

  /** @return next unique download ID */
  public static final synchronized long nextId(final Context context) {
    final SharedPreferences counterStore = context.getSharedPreferences(PREF_NAME, 0);
    final long value = counterStore.getLong(KEY_ID, 0) + 1;
    counterStore.edit().putLong(KEY_ID, value).commit();
    return value;
  }

  /** @return new download task */
  protected DownloadTask createDownloadTask() { return new DownloadTask(); }

  /** @return the buffersPool */
  protected BuffersPool getBuffersPool() { return buffersPool; }

  /** @return the notificationManager */
  protected NotificationManager getNotificationManager() { return notificationManager; }

  @Override
  public void onCreate() {
    super.onCreate();
    notificationManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
  }

  @Override
  public int onStartCommand(final Intent intent, final int flags, final int startId) {
    if (intent == null) { return START_STICKY; }
    final String action = intent.getAction();
    if (action == null) { return START_STICKY; }
    if (DEBUG) { Log.v(TAG, "Start " + action); }

    if (ACTION_ENQUEUE.equals(action)) {
      final Request request = intent.getParcelableExtra(EXTRA_REQUEST);
      enqueue(request);
    }

    return START_STICKY;
  }

  @Override
  public IBinder onBind(final Intent intent) { return null; }

  @Override
  public void onDestroy() {
    if (!tasks.isEmpty()) {
      if (DEBUG) { Log.i(TAG, "Canceling current tasks"); }
      for (final DownloadTask task : tasks) { task.cancel(true); }
    }
    super.onDestroy();
  }

  protected void enqueue(final Request request) {
    final DownloadTask task = createDownloadTask();
    tasks.add(task);
    request.notificationId = NOTIFICATION_BASE_ID + tasks.size();
    task.execute(request);
  }

  protected void onTaskFinish(final DownloadTask task, final Request request) {
    notificationManager.cancel(request.notificationId);
    tasks.remove(task);
    sendBroadcast(new Intent(ACTION_DOWNLOAD_COMPLETE).putExtra(EXTRA_ID, request.id).putExtra(EXTRA_SUCCESS, request.success));
    if (tasks.isEmpty()) { stopSelf(); }
  }

  /**
   * Download request.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public static class Request implements Parcelable {

    /** Creator. */
    public static final Creator<Request> CREATOR = new Creator<DownloadsService.Request>() {
      @Override
      public Request createFromParcel(final Parcel source) { return new Request(source); }
      @Override
      public Request[] newArray(final int size) { return new Request[size]; }
    };

    /** Identifier. */
    private long id;
    /** Title. */
    private String title;
    /** Description. */
    private String description;
    /** URI. */
    private Uri uri;
    /** Destination URI. */
    private Uri destinationUri;
    /** Success flag. */
    private boolean success;

    /** Notification ID. */
    private int notificationId;

    public Request() { }

    protected Request(final Parcel in) {
      ClassLoader cl = getClass().getClassLoader();
      this.id = in.readLong();
      this.title = in.readString();
      this.description = in.readString();
      this.uri = in.readParcelable(cl);
      this.destinationUri = in.readParcelable(cl);
      this.success = in.readInt() == 1;
    }

    @Override
    public int describeContents() { return 0; }
    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
      dest.writeLong(id);
      dest.writeString(title);
      dest.writeString(description);
      dest.writeParcelable(uri, flags);
      dest.writeParcelable(destinationUri, flags);
      dest.writeInt(success ? 1 : 0);
    }

    /** @return the id */
    public long getId() { return id; }
    /** @param id the id to set */
    public void setId(final long id) { this.id = id; }
    /** @return the title */
    public String getTitle() { return title; }
    /** @param title the title to set */
    public void setTitle(final String title) { this.title = title; }
    /** @return the description */
    public String getDescription() { return description; }
    /** @param description the description to set */
    public void setDescription(final String description) { this.description = description; }
    /** @return the uri */
    public Uri getUri() { return uri; }
    /** @param uri the uri to set */
    public void setUri(final Uri uri) { this.uri = uri; }
    /** @return the destinationUri */
    public Uri getDestinationUri() { return destinationUri; }
    /** @param destinationUri the destinationUri to set */
    public void setDestinationUri(final Uri destinationUri) { this.destinationUri = destinationUri; }
    /** @return the success */
    public boolean isSuccess() { return success; }
    /** @param success the success to set */
    public void setSuccess(final boolean success) { this.success = success; }

  }

  /**
   * Download task.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public class DownloadTask extends AsyncTask<Request, Float, Void> {
    /** Notification ID. */
    private Request request;
    /** Click intent. */
    private Intent clickIntent = new Intent(ACTION_DOWNLOAD_CLICK);
    /** Notification time. */
    private long notificationTime = System.currentTimeMillis();

    /** @return the request */
    public Request getRequest() { return request; }

    protected void updateDownloadProgress(final Float progress) {
      final float p = progress == null ? 0 : progress;
      final int max = 1000;

      final Notification n = AppUtils.getSdkDependentUtils().createNotificationBuilder(DownloadsService.this)
          .setWhen(notificationTime)
          .setSmallIcon(android.R.drawable.stat_sys_download)
          .setTicker(request.title)
          .setContentTitle(request.title)
          .setContentText(request.description)
          .setContentIntent(PendingIntent.getBroadcast(DownloadsService.this, 0, clickIntent, PendingIntent.FLAG_CANCEL_CURRENT))
          .setOngoing(true)
          .setProgress(max, (int)(p * max), progress == null)
          .build();

      getNotificationManager().notify(request.notificationId, n);

      publishProgress(p);
    }

    @Override
    protected Void doInBackground(final Request... params) {
      final Request request = params[0];
      request.success = false;

      this.request = request;
      clickIntent.putExtra(EXTRA_ID, request.id);
      updateDownloadProgress(null);

      File destination;
      try {
        destination = new File(new URI(request.destinationUri.toString()));
      } catch (final URISyntaxException e) {
        Log.e(TAG, "Bad URI", e);
        return null;
      }

      InputStream input = null;
      OutputStream output = null;
      try {
        final URLConnection urlConnection = new UrlConnectionBuilder()
          .setUrl(params[0].uri)
          .create();

        urlConnection.connect();

        destination.createNewFile();
        output = new FileOutputStream(destination);
        input = new PoolableBufferedInputStream(urlConnection.getInputStream(), DEFAULT_BUFFER_SIZE, buffersPool);

        final long length = urlConnection.getContentLength();
        if (length > 0) { updateDownloadProgress(0f); }

        int count;
        final byte[] buffer = buffersPool.get(DEFAULT_BUFFER_SIZE);
        int total = 0;
        float prevProgress = 0;
        final float minDelta = 0.05f;
        do {
          count = input.read(buffer);
          if (count > 0) {
            output.write(buffer, 0, count);
            if (length > 0) {
              total += count;
              final float progress = (float)total / length;
              if (progress - prevProgress >= minDelta) {
                updateDownloadProgress(progress);
                prevProgress = progress;
              }
            }
          }
        } while (count != -1);
        request.success = true;
        return null;
      } catch (final Exception e) {
        Log.e(TAG, "Cannot download " + request.uri, e);
        return null;
      } finally {
        IoUtils.closeQuietly(input);
        IoUtils.closeQuietly(output);
      }
    }
    @Override
    protected void onCancelled() {
      onTaskFinish(this, request);
    }
    @Override
    protected void onPostExecute(final Void result) {
      onTaskFinish(this, request);
    }
  }

}
