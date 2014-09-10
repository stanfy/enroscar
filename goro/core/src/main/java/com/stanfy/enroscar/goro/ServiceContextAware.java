package com.stanfy.enroscar.goro;

import android.content.Context;

/**
 * Indicates a task that can be supplied with a service context instance the task in executed in.
 * This interface addresses a problem of injecting Android {@link android.content.Context} instance
 * into tasks that are packed into {@link android.os.Parcel}.
 *
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public interface ServiceContextAware {

  /**
   * Invoked by {@link GoroService} on tasks passed via {@link android.content.Intent} to
   * provide a context instance.
   * You are not supposed to invoke this method manually except in tests code.
   */
  void injectServiceContext(Context context);

}
