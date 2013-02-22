package com.stanfy.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.Looper;

import com.stanfy.app.service.ApiMethodCallback;
import com.stanfy.app.service.ApiMethods;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ResponseData;
import com.xtremelabs.robolectric.Robolectric;

/**
 * Test application.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class Application extends com.stanfy.app.Application {

  /** Base service for testing. */
  public class ApplicationService extends com.stanfy.app.service.ApplicationService {
    @Override
    protected void doStop() {
      Application.this.destroyService();
    }
    @Override
    public com.stanfy.app.service.ApiMethods getApiMethods() {
      return super.getApiMethods();
    }

    @Override
    protected ApiMethods createApiMethods() {
      return new MyApiMethods(this);
    }

  }

  /** Api methods. */
  private static class MyApiMethods extends ApiMethods {
    public MyApiMethods(final ApplicationService service) {
      super(service);
    }
    @Override
    protected void checkClientThread() {
      // nothing
    }
  }

  /** Callback for testing. */
  public class WaitApiCallback implements ApiMethodCallback {

    /** Latch. */
    private final CountDownLatch latch = new CountDownLatch(1);

    /** Data. */
    private ResponseData<?> data;

    /** Canceled. */
    private boolean canceled = false;

    @Override
    public void reportSuccess(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      data = responseData;
      latch.countDown();
    }
    @Override
    public void reportPending(final int requestId) {
    }
    @Override
    public void reportLastOperation(final int requestId, final ResponseData<?> responseData) {
    }
    @Override
    public void reportError(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      data = responseData;
      latch.countDown();
    }
    @Override
    public void reportCancel(final RequestDescription requestDescription, final ResponseData<?> responseData) {
      canceled = true;
      data = responseData;
      latch.countDown();
    }

    public boolean isCanceled() { return canceled; }

    public ResponseData<?> waitData() {
      try {
        latch.await();
      } catch (final InterruptedException e) {
        throw new RuntimeException("Wait is interrupted!");
      }
      return data;
    }
  }

  /** Service state. */
  private static final int ZYGOTE = 0, CREATED = 1;

  /** App service instance. */
  private ApplicationService appServiceInstance;

  /** Component name. */
  private final ComponentName serviceComponentName = new ComponentName(this, ApplicationService.class);

  public void setAppServiceInstance(final ApplicationService appServiceInstance) {
    this.appServiceInstance = appServiceInstance;
  }

  /** Last connection. */
  private ServiceConnection lastConnection;
  /** Last intent. */
  private Intent lastBindIntent;

  /** Service state. */
  private int serviceState = ZYGOTE;

  /** Start ID. */
  private AtomicInteger startId = new AtomicInteger(0);

  public boolean isServiceRunning() {
    return serviceState >= CREATED;
  }

  private void createService() {
    if (serviceState < CREATED) {
      appServiceInstance.onCreate();
      serviceState = CREATED;
    }
  }

  private void destroyService() {
    serviceState = ZYGOTE;
    if (lastConnection != null) {
      lastConnection.onServiceDisconnected(serviceComponentName);
    }
    appServiceInstance.onDestroy();
  }

  @Override
  public ComponentName startService(final Intent service) {
    createService();
    appServiceInstance.onStartCommand(service, 0, startId.incrementAndGet());
    return serviceComponentName;
  }

  @Override
  public boolean bindService(final Intent service, final ServiceConnection conn, final int flags) {
    if ((flags & Context.BIND_AUTO_CREATE) != 0) {
      createService();
    }
    lastConnection = conn;
    lastBindIntent = service;
    Robolectric.shadowOf(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        final IBinder binder = appServiceInstance.onBind(service);
        conn.onServiceConnected(serviceComponentName, binder);
      }
    }, 0);
    return true;
  }

  @Override
  public void unbindService(final ServiceConnection conn) {
    if (lastConnection != conn) {
      throw new IllegalArgumentException("You were bound with another connection");
    }
    Robolectric.shadowOf(Looper.getMainLooper()).post(new Runnable() {
      @Override
      public void run() {
        appServiceInstance.onUnbind(lastBindIntent);
        conn.onServiceDisconnected(serviceComponentName);
      }
    }, 0);
    lastConnection = null;
  }

  @Override
  public boolean stopService(final Intent name) {
    final boolean result = serviceState > ZYGOTE;
    if (result) {
      destroyService();
    }
    return result;
  }

}
