package com.stanfy.utils;

import android.content.Context;
import android.content.Intent;

import com.stanfy.app.beans.BeansManager;
import com.stanfy.app.service.ApplicationService;
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

  public Intent constructIntent(final RequestDescription description) {
    Class<?> serviceClass = BeansManager.get(context).getRemoteServerApiConfiguration().getApplicationServiceClass();
    return new Intent(context, serviceClass)
        .setAction(ApplicationService.ACTION_SEND_REQUEST)
        .putExtra(ApplicationService.EXTRA_REQUEST_DESCRIPTION, description);
  }

}
