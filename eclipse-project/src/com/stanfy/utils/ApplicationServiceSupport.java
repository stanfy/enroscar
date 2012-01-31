package com.stanfy.utils;

import java.lang.ref.WeakReference;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IInterface;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.app.Application;

/**
 * Base class for helpers that can bind to application service.
 * @param <T> service object type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
abstract class ApplicationServiceSupport<T extends IInterface> implements ServiceConnection {

  /** Logging tag. */
  private static final String TAG = "ServiceSupport";
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_SERVICES, DEBUG_CALLS = false;

  /** Context instance. */
  final WeakReference<Context> contextRef;

  /** Service object. */
  T serviceObject;

  public ApplicationServiceSupport(final Context a) {
    this.contextRef = new WeakReference<Context>(a);
  }

  /** @return application service class used to resolve a component name */
  private Class<?> getServiceClass(final Context context) {
    final Application app = (Application)context.getApplicationContext();
    return app.getApplicationServiceClass();
  }

  protected abstract Class<T> getInterfaceClass();

  /**
   * Create the service connection.
   */
  public void bind() {
    if (serviceObject != null) { return; }
    final Context context = contextRef.get();
    if (context == null) { return; }
    final Intent intent = new Intent(context, getServiceClass(context));
    intent.setAction(getInterfaceClass().getName());
    if (DEBUG_CALLS) {
      Log.v(TAG, "Attempt to bind to service " + this + "/" + context, new RuntimeException());
    }
    context.startService(intent);
    final boolean bindResult = context.bindService(intent, this, 0);
    if (DEBUG) { Log.v(TAG, "Binded to service: " + bindResult + ", " + context); }
  }

  /**
   * Destroy the service connection.
   */
  public void unbind() {
    serviceObject = null;
    final Context context = contextRef.get();
    if (DEBUG) { Log.v(TAG, "Unbind " + context); }
    if (context == null) { return; }
    try {
      context.unbindService(this);
    } catch (final Exception e) {
      if (DEBUG) { Log.e(TAG, "Cannot unbind from application service", e); }
    }
  }

}
