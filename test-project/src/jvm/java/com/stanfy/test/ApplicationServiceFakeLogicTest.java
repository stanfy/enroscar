package com.stanfy.test;

import static org.hamcrest.Matchers.equalTo;

import org.junit.Test;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.stanfy.test.Application.ApplicationService;

/**
 * Test logic in {@link AbstractApplicationServiceTest}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class ApplicationServiceFakeLogicTest extends AbstractApplicationServiceTest {

  /** States. */
  private boolean creating = false, running = false, binding = false, bound = false, stopped = false, unbinding = false;

  @Test
  public void testLogic() {
    assertThat(creating, equalTo(false));

    // start
    getApplication().startService(new Intent());
    assertThat(creating, equalTo(true));
    assertThat(running, equalTo(true));

    final ServiceConnection connection = new ServiceConnection() {
      @Override
      public void onServiceDisconnected(final ComponentName name) {
        bound = false;
      }
      @Override
      public void onServiceConnected(final ComponentName name, final IBinder service) {
        bound = true;
      }
    };

    // bind
    assertThat(bound, equalTo(false));
    assertThat(binding, equalTo(false));
    getApplication().bindService(new Intent(), connection, 0);
    assertThat(binding, equalTo(true));
    assertThat(bound, equalTo(true));

    // unbind
    assertThat(unbinding, equalTo(false));
    getApplication().unbindService(connection);
    assertThat(unbinding, equalTo(true));
    assertThat(bound, equalTo(false));

    // stop
    assertThat(stopped, equalTo(false));
    getApplication().stopService(new Intent());
    assertThat(stopped, equalTo(true));

  }

  @Override
  protected ApplicationService createApplicationService(final Application app) {
    return app.new ApplicationService() {
      @Override
      public void onCreate() {
        super.onCreate();
        creating = true;
      }
      @Override
      public int onStartCommand(final Intent intent, final int flags, final int startId) {
        running = true;
        return super.onStartCommand(intent, flags, startId);
      }
      @Override
      public IBinder onBind(final Intent intent) {
        binding = true;
        return super.onBind(intent);
      }
      @Override
      public void onDestroy() {
        super.onDestroy();
        stopped = true;
      }
      @Override
      public boolean onUnbind(final Intent intent) {
        unbinding = true;
        return super.onUnbind(intent);
      }
    };
  }

}
