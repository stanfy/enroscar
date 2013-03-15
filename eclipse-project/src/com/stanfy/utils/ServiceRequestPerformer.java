package com.stanfy.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.stanfy.app.service.ApplicationService;
import com.stanfy.enroscar.rest.RequestExecutor;
import com.stanfy.enroscar.rest.request.RequestBuilder;
import com.stanfy.enroscar.rest.request.RequestDescription;

/**
 * Requests performer that sends {@link RequestDescription} to the service as an {@link Intent}.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ServiceRequestPerformer implements RequestExecutor  {

  /** Application context. */
  private final Context context;

  public ServiceRequestPerformer(final Context a) {
    this.context = a.getApplicationContext();
  }

  @Override
  public int performRequest(final RequestDescription description) {
    context.startService(constructIntent(description));
    return description.getId();
  }

  protected Intent constructIntent(final RequestDescription description) {
    // TODO make it configurable
    Class<?> serviceClass = ApplicationService.class;

    // XXX we wrap our parcelable into Bundle, see http://code.google.com/p/android/issues/detail?id=6822
    Bundle descriptionBundle = new Bundle(1);
    descriptionBundle.putParcelable(ApplicationService.EXTRA_REQUEST_DESCRIPTION, description);

    return new Intent(context, serviceClass)
      .setAction(ApplicationService.ACTION_SEND_REQUEST)
      .setData(Uri.parse("request://" + description.getId()))
      .putExtra(ApplicationService.EXTRA_REQUEST_DESCRIPTION_BUNDLE, descriptionBundle);
  }

  protected final Intent getIntent(final RequestBuilder<?> requestBuilder) {
    final RequestDescription[] requestDescription = new RequestDescription[1];
    requestBuilder.setExecutor(new RequestExecutor() {
      @Override
      public int performRequest(final RequestDescription rd) {
        requestDescription[0] = rd;
        return 0;
      }
    });
    requestBuilder.execute();
    requestBuilder.setExecutor(null);

    return constructIntent(requestDescription[0]);
  }

  /**
   * Construct a pending intent that can be used for starting request processing.
   * Note that after this method is executed executor of the request builder is always null.
   * @param requestBuilder request builder instance
   * @param flags flags to pass to {@link PendingIntent#getService(Context, int, Intent, int)}
   * @return pending intent for starting request processing
   */
  public PendingIntent getPendingIntent(final RequestBuilder<?> requestBuilder, final int flags) {
    return PendingIntent.getService(context, 0, getIntent(requestBuilder), flags);
  }

}
