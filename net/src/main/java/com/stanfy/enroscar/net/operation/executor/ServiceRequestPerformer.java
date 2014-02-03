package com.stanfy.enroscar.net.operation.executor;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.stanfy.enroscar.net.operation.RequestDescription;
import com.stanfy.enroscar.rest.executor.ApplicationService;
import com.stanfy.enroscar.net.operation.RequestBuilder;

/**
 * Requests performer that sends {@link RequestDescription} to the service as an {@link Intent}.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class ServiceRequestPerformer implements RequestExecutor  {

  /** Application context. */
  private final Context context;

  /** Used service class. */
  private final Class<?> serviceClass; // TODO should extend GoroService

  public ServiceRequestPerformer(final Context context) {
    this(context, ApplicationService.class);
  }

  public ServiceRequestPerformer(final Context context, final Class<?> serviceClass) {
    this.context = context;
    this.serviceClass = serviceClass;
  }

  @Override
  public void performRequest(final RequestDescription description) {
    context.startService(constructIntent(description));
  }

  /**
   * @param description request description
   * @return intent that contains the request description
   */
  protected Intent constructIntent(final RequestDescription description) {
    // TODO use GoroService method
    // XXX we wrap our parcelable into Bundle, see http://code.google.com/p/android/issues/detail?id=6822
    Bundle descriptionBundle = new Bundle(1);
    descriptionBundle.putParcelable(ApplicationService.EXTRA_REQUEST_DESCRIPTION, description);

    return new Intent(context, serviceClass)
      .setAction(ApplicationService.ACTION_SEND_REQUEST)
      .setData(Uri.parse("request://" + description.getId()))
      .putExtra(ApplicationService.EXTRA_REQUEST_DESCRIPTION_BUNDLE, descriptionBundle);
  }

  /**
   * @param requestBuilder request builder instance
   * @return intent ready to to be sent for request description processing
   */
  protected final Intent getIntent(final RequestBuilder<?> requestBuilder) {
    final RequestDescription[] requestDescription = new RequestDescription[1];
    requestBuilder.setExecutor(new RequestExecutor() {
      @Override
      public void performRequest(final RequestDescription rd) {
        requestDescription[0] = rd;
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
