package com.stanfy.enroscar.net.operation.executor;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.stanfy.enroscar.goro.GoroService;
import com.stanfy.enroscar.net.operation.RequestBuilder;
import com.stanfy.enroscar.net.operation.RequestDescription;

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
    this(context, GoroService.class);
  }

  public ServiceRequestPerformer(final Context context, final Class<?> serviceClass) {
    this.context = context;
    this.serviceClass = serviceClass;
  }

  @Override
  public void performRequest(final RequestDescription description) {
    //context.startService(constructIntent(description));
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

    return null; //constructIntent(requestDescription[0]);
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
