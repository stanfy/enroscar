package com.stanfy.enroscar.content;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import com.stanfy.enroscar.sdkdep.SdkDepUtils;

import android.app.Activity;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.MediaStore.MediaColumns;
import android.support.v4.app.ShareCompat;
import android.text.TextUtils;

/**
 * Sharing helper class. Be sure to call its methods from the main thread.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public final class SharingHelper {

  /** Sharing extra. */
  public static final String EXTRA_SHARING = "_enroscar_sharing_intent_";

  /** Media query projection. */
  private static final String[] MEDIA_PROJECTION = new String[] {MediaColumns.DISPLAY_NAME, MediaColumns.TITLE, MediaColumns.DATA};

  /** Context instance. */
  private WeakReference<Context> context;
  /** Actor instance. */
  private final SharingActor actor;

  /** Resolver task. */
  private ResolveContentUriTask asyncTask;

  private SharingHelper(final SharingActor actor) {
    this.actor = actor;
    if (actor instanceof Context) {
      this.context = new WeakReference<Context>((Context)actor);
    }
  }

  public static SharingHelper use(final SharingActor actor) {
    return new SharingHelper(actor);
  }

  public static Intent setSharingIntent(final Intent dest, final Intent sharingIntent) {
    return sharingIntent != null ? dest.putExtra(EXTRA_SHARING, sharingIntent) : dest;
  }

  /**
   * @param context context instance
   * @return helper for chaining
   */
  public SharingHelper withContext(final Context context) {
    this.context = new WeakReference<Context>(context);
    return this;
  }

  /**
   * Check whether activity contains a sharing intent previously set with {@link #setSharingIntent(Intent, Intent)}
   * and process it with {@link #processSharingIntent(Intent)} if result is positive.
   * Anyway activity intent will not contain a sharing extra after this method execution.
   * @param activity activity instance
   * @return whether sharing extra has been put to the activity intent
   */
  public boolean checkSharingIntent(final Activity activity) {
    final Intent activityIntent = activity.getIntent();
    if (activityIntent.hasExtra(SharingHelper.EXTRA_SHARING)) {
      processSharingIntent(activityIntent.<Intent>getParcelableExtra(SharingHelper.EXTRA_SHARING));
      activityIntent.removeExtra(SharingHelper.EXTRA_SHARING);
      return true;
    }
    return false;
  }

  /**
   * Process data provided by the sharing intent. Call it from the main thread.
   * @param sharingIntent sharing intent instance
   */
  public void processSharingIntent(final Intent sharingIntent) {
    final SharingData data = createSharingData(sharingIntent);
    deliverSharingData(data);
  }

  /**
   * Stop asynchronous operations. Call it from the main thread.
   */
  public void abort() {
    asyncTask.cancel(false);
  }

  private SharingData createSharingData(final Intent sharingIntent) {
    final SharingData data = TextUtils.isEmpty(sharingIntent.getAction()) ? new PickData() : new SharingData();
    data.intent = sharingIntent;
    final Context context = this.context.get();
    if (context instanceof Activity) {
      data.callerInfo = ((Activity) context).getCallingActivity();
    } else {
      final String pkg = sharingIntent.getStringExtra(ShareCompat.EXTRA_CALLING_PACKAGE);
      final String act = sharingIntent.getStringExtra(ShareCompat.EXTRA_CALLING_ACTIVITY);
      if (!TextUtils.isEmpty(pkg) && !TextUtils.isEmpty(act)) {
        data.callerInfo = new ComponentName(pkg, act);
      }
    }
    return data;
  }

  private void deliverSharingData(final SharingData data) {
    final SharingActor actor = this.actor;
    final Context context = this.context.get();
    if (actor == null || context == null) { return; }
    if (!data.containsStream() || data.isStreamResolved()) {
      asyncTask = null;
      actor.dispatchSharedData(data);
      return;
    }
    asyncTask = new ResolveContentUriTask();
  
    SdkDepUtils.get(context).executeAsyncTaskParallel(asyncTask, data);
  }

  private void deliverError() {
    final SharingActor actor = this.actor;
    final Context context = this.context.get();
    if (actor == null || context == null) { return; }
    actor.dispatchResolverError();
  }

  /**
   * Interface of an object that can react on sharing intent.
   * @author Roman Mazur (Stanfy - http://stanfy.com)
   */
  public interface SharingActor {
    void dispatchSharedData(final SharingData data);
    void dispatchResolverError();
  }

  /**
   *
   * @author Roman Mazur (Stanfy - http://stanfy.com)
   */
  public static class SharingData implements Parcelable {

    /** Creator. */
    public static final Creator<SharingData> CREATOR = new Creator<SharingHelper.SharingData>() {
      @Override
      public SharingData createFromParcel(final Parcel source) { return new SharingData(source); }
      @Override
      public SharingData[] newArray(final int size) { return new SharingData[size]; }
    };

    /** Intent that contains all the information. */
    Intent intent;

    /** Content name. */
    String[] contentNames;

    /** Caller info. */
    ComponentName callerInfo;

    /** Streams. */
    private ArrayList<Uri> cachedStreams;

    SharingData() {
      // nothing
    }

    SharingData(final Parcel in) {
      this.intent = in.readParcelable(getClass().getClassLoader());
      this.contentNames = in.createStringArray();
    }

    @Override
    public int describeContents() { return 0; }
    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
      dest.writeParcelable(intent, flags);
      dest.writeStringArray(contentNames);
    }

    private void cacheStreams() {
      this.cachedStreams = intent.getParcelableArrayListExtra(Intent.EXTRA_STREAM);
    }

    /** @return whether this intent has one of send actions */
    public boolean isShareIntent() {
      return isSingleShare() || isMultipleShare();
    }
    /** @return whether this intent has action {@link Intent#ACTION_SEND} */
    public boolean isSingleShare() {
      return Intent.ACTION_SEND.equals(intent.getAction());
    }
    /** @return whether this intent has action {@link Intent#ACTION_SEND_MULTIPLE} */
    public boolean isMultipleShare() {
      return Intent.ACTION_SEND_MULTIPLE.equals(intent.getAction());
    }

    /** @return subject */
    public String getSubject() { return intent.getStringExtra(Intent.EXTRA_SUBJECT); }
    /** @return data MIME-type */
    public String getType() { return intent.getType(); }
    /** @return recipient address (email) */
    public String[] getRecipient() { return intent.getStringArrayExtra(Intent.EXTRA_EMAIL); }
    /** @return CC field value */
    public String[] getCopyTo() { return intent.getStringArrayExtra(Intent.EXTRA_CC); }
    /** @return BCC field value */
    public String[] getBlindCopyTo() { return intent.getStringArrayExtra(Intent.EXTRA_BCC); }
    /** @return message text (can be styled!) */
    public CharSequence getMessage() { return intent.getCharSequenceExtra(Intent.EXTRA_TEXT); }

    /** @return stream URI (use content resolver to get content details and stream object) */
    public Uri getStreamUri() { return intent.getParcelableExtra(Intent.EXTRA_STREAM); }
    /** @return count of shared streams */
    public int getStreamUriCount() {
      if (isMultipleShare()) {
        if (cachedStreams == null) { cacheStreams(); }
        return cachedStreams == null ? 0 : cachedStreams.size();
      }
      return getStreamUri() == null ? 0 : 1;
    }
    /** @return stream URI (use content resolver to get content details and stream object) */
    public Uri getStreamUri(final int index) {
      if (cachedStreams == null && isMultipleShare()) { cacheStreams(); }
      if (cachedStreams != null) { return cachedStreams.get(index); }
      if (index == 0) { return getStreamUri(); }
      throw new IndexOutOfBoundsException("Trying to get stream[" + index + "] from single share intent. It can have not more than one stream.");
    }

    /** @return whether sharing data describes a content stream */
    public boolean containsStream() { return getStreamUriCount() > 0; }

    /** Caller information. */
    public ComponentName getCallerInfo() { return callerInfo; }

    /** @return whether content stream is resolved */
    public boolean isStreamResolved() { return contentNames != null; }

    /** @return content name */
    public String[] getContentNames() { return contentNames; }

  }

  /**
   * @author Roman Mazur (Stanfy - http://stanfy.com)
   */
  public static class PickData extends SharingData {

    /** Creator. */
    public static final Creator<PickData> CREATOR = new Creator<PickData>() {
      @Override
      public PickData createFromParcel(final Parcel source) { return new PickData(source); }
      @Override
      public PickData[] newArray(final int size) { return new PickData[size]; }
    };

    PickData() {
      // nothing
    }

    PickData(final Parcel in) {
      super(in);
    }

    @Override
    public boolean isShareIntent() { return false; }

    @Override
    public Uri getStreamUri() { return intent.getData(); }

  }

  /**
   * @author Roman Mazur (Stanfy - http://stanfy.com)
   */
  private class ResolveContentUriTask extends AsyncTask<SharingData, Void, SharingData> {

    /** Error state. */
    private boolean resolverErrorState = false;

    /** Resolver instance. */
    private ContentResolver resolver;

    private void resolveContent(final ContentResolver resolver, final SharingData data, final int index) {
      final Uri contentUri = data.getStreamUri(index);

      // resolve content name
      String contentName = null;
      final String scheme = contentUri.getScheme();
      if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {

        // query content resolver for additional info
        final Cursor cursor = resolver.query(contentUri, MEDIA_PROJECTION, null, null, null);
        if (cursor != null) {
          try {
            cursor.moveToFirst();
            // display name
            contentName = cursor.getString(0);
            if (TextUtils.isEmpty(contentName)) {
              // title
              contentName = cursor.getString(1);
            }
            if (TextUtils.isEmpty(contentName)) {
              // file name
              contentName = Uri.parse(cursor.getString(2)).getLastPathSegment();
            }
          } finally {
            cursor.close();
          }
        }

      } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {

        // use file name
        contentName = contentUri.getLastPathSegment();

      }

      data.contentNames[index] = contentName;
    }

    @Override
    protected void onPreExecute() {
      final Context ctx = context.get();
      if (ctx == null) { return; }
      this.resolver = ctx.getApplicationContext().getContentResolver();
    }

    @Override
    protected SharingData doInBackground(final SharingData... params) {
      if (resolver == null) { return null; }

      final SharingData data = params[0];
      final int count = data.getStreamUriCount();
      if (count == 0) { throw new IllegalStateException("Cannot run resolver task with a null content URI"); }

      data.contentNames = new String[count];
      for (int i = 0; i < count; i++) {
        resolveContent(resolver, data, i);
      }

      return data;
    }

    @Override
    protected void onPostExecute(final SharingData result) {
      if (result == null) { return; }
      if (!resolverErrorState) {
        deliverSharingData(result);
      } else {
        deliverError();
      }
    }
  }

}
