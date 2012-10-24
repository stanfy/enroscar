package com.stanfy.utils;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.stanfy.app.beans.BeansManager;
import com.stanfy.app.service.ApplicationService;
import com.stanfy.serverapi.request.RequestBuilder;
import com.stanfy.serverapi.request.RequestDescription;

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

  private Intent constructIntent(final RequestDescription description) {
    Class<?> serviceClass = BeansManager.get(context).getRemoteServerApiConfiguration().getApplicationServiceClass();
    return new Intent(context, serviceClass)
      .setAction(ApplicationService.ACTION_SEND_REQUEST)
      .putExtra(ApplicationService.EXTRA_REQUEST_DESCRIPTION, description);
  }

  /**
   * Construct a pending intent that can be used for starting request processing.
   * Note that after this method is executed executor of the request builder is always null.
   * @param requestBuilder request builder instance
   * @param flags flags to pass to {@link PendingIntent#getService(Context, int, Intent, int)}
   * @return pending intent for starting request processing
   */
  public PendingIntent getPendingIntent(final RequestBuilder<?> requestBuilder, final int flags) {
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
    return PendingIntent.getService(context, 0, constructIntent(requestDescription[0]), flags);
  }

}
