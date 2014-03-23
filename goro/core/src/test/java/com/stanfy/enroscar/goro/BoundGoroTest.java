package com.stanfy.enroscar.goro;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;

import static org.mockito.Mockito.*;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.ANDROID.assertThat;

/**
 * Tests for BoundGoro.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BoundGoroTest {

  /** Implementation. */
  private BoundGoro goro;

  /** Mock service instance of Goro. */
  private Goro serviceInstance;

  private Activity context;
  private ShadowActivity shadowContext;

  private IBinder binder;
  private ComponentName serviceCompName;

  @Before
  public void create() {
    context = Robolectric.setupActivity(Activity.class);
    shadowContext = Robolectric.shadowOf(context);
    goro = Goro.bindWith(context);
    goro = spy(goro);

    serviceInstance = mock(Goro.class);

    serviceCompName = new ComponentName(context, GoroService.class);
    binder = new GoroService.GoroBinder(serviceInstance, null);
    shadowContext.getShadowApplication()
        .setComponentNameAndServiceForBindService(
            serviceCompName,
            binder
        );
    reset(serviceInstance);
  }

  private void assertBinding() {
    Intent startedService = shadowContext.getNextStartedService();
    assertThat(startedService).isNotNull();
    assertThat(startedService).hasComponent(context, GoroService.class);
    Intent boundService = shadowContext.getNextStartedService();
    assertThat(boundService).isNotNull();
    assertThat(boundService).hasComponent(context, GoroService.class);

    verify(goro).onServiceConnected(serviceCompName, binder);
  }

  @Test
  public void addListenerShouldRecord() {
    GoroListener listener = mock(GoroListener.class);
    goro.addTaskListener(listener);
    goro.bind();
    assertBinding();
    verify(serviceInstance).addTaskListener(listener);
  }

  @Test
  public void removeListenerShouldRemoveFromRecords() {
    GoroListener listener = mock(GoroListener.class);
    goro.addTaskListener(listener);
    goro.removeTaskListener(listener);
    goro.bind();
    assertBinding();
    verify(serviceInstance, never()).addTaskListener(listener);
  }

  @Test
  public void scheduleShouldRecordDefaultQueue() {
    Callable<?> task = mock(Callable.class);
    goro.schedule(task);
    goro.bind();
    assertBinding();
    verify(serviceInstance).schedule(Goro.DEFAULT_QUEUE, task);
  }

  @Test
  public void scheduleShouldReturnFuture() {
    Future<?> future = goro.schedule(mock(Callable.class));
    assertThat(future).isNotNull();
  }

  @Test
  public void cancelFutureBeforeBindingShouldRemoveRecordedTask() {
    Future<?> future = goro.schedule(mock(Callable.class));
    future.cancel(true);
    goro.bind();
    assertBinding();
    verify(serviceInstance, never()).schedule(anyString(), any(Callable.class));
  }

  @Test
  public void scheduleShouldRequestQueueName() {
    Callable<?> task = mock(Callable.class);
    goro.schedule("1", task);
    goro.bind();
    assertBinding();
    verify(serviceInstance).schedule("1", task);
  }

  @Test
  public void afterBindingAddRemoveListenerShouldBeDelegated() {
    goro.bind();
    GoroListener listener = mock(GoroListener.class);
    goro.addTaskListener(listener);
    goro.removeTaskListener(listener);
    InOrder order = inOrder(serviceInstance);
    order.verify(serviceInstance).addTaskListener(listener);
    order.verify(serviceInstance).removeTaskListener(listener);
  }

  @Test
  public void afterBindingScheduleShouldBeDelegated() {
    goro.bind();
    Callable<?> task = mock(Callable.class);
    ObservableFuture<?> future = mock(ObservableFuture.class);
    doReturn(future).when(serviceInstance).schedule("2", task);
    assertThat((Object) goro.schedule("2", task)).isSameAs(future);
  }

  @Test
  public void getExecutorShouldReturnExecutorWrapper() {
    Executor executor = goro.getExecutor("q");
    assertThat(executor).isNotNull();
    Runnable task = mock(Runnable.class);
    executor.execute(task);
    verify(task, never()).run();

    //noinspection NullableProblems
    Executor direct = new Executor() {
      @Override
      public void execute(Runnable command) {
        command.run();
      }
    };
    doReturn(direct).when(serviceInstance).getExecutor(anyString());

    goro.bind();

    verify(task).run();
  }

}
