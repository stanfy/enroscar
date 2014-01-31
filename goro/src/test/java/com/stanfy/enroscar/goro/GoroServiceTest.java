package com.stanfy.enroscar.goro;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.concurrent.Callable;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests for GoroService.
 */
@RunWith(RobolectricTestRunner.class)
@Config(emulateSdk = Build.VERSION_CODES.JELLY_BEAN_MR2)
public class GoroServiceTest {

  /** Queues. */
  private TestingQueues queues;

  /** Instance under tests. */
  private GoroService service;

  /** Execution flag. */
  private boolean executed;

  /** Test task. */
  private Task task;

  @Before
  public void init() {
    queues = new TestingQueues();
    service = new GoroService() {
      @Override
      protected Goro createGoro() {
        return new Goro(queues);
      }
    };
    service.onCreate();

    task = new Task(new Callable<String>() {
      @Override
      public String call() throws Exception {
        executed = true;
        return "ok";
      }
    });
    executed = false;
  }

  @Test
  public void shouldScheduleTasksPassedInCommandIntent() {
    Intent command = new Intent();
    command.putExtra(GoroService.EXTRA_TASK, task);
    service.onStartCommand(command, 0, 1);
    queues.executeAll();
    assertThat(executed).isTrue();
  }

  @Test
  public void shouldScheduleTasksCreatedWithFactoryMethod() {
    service.onStartCommand(GoroService.taskIntent(Robolectric.application, task), 0, 1);
    queues.executeAll();
    assertThat(executed).isTrue();
  }

  @Test
  public void shouldScheduleTasksPassedInCommandIntentWrappedInBundle() {
    Intent command = new Intent();
    Bundle bundle = new Bundle();
    bundle.putParcelable(GoroService.EXTRA_TASK, task);
    command.putExtra(GoroService.EXTRA_TASK_BUNDLE, bundle);
    service.onStartCommand(command, 0, 1);
    queues.executeAll();
    assertThat(executed).isTrue();
  }

  /** A test task. */
  public static class Task implements Parcelable, Callable<String> {

    /** Creator instance. */
    public static Creator<Task> CREATOR = new Creator<Task>() {
      @Override
      public Task createFromParcel(final Parcel source) {
        throw new UnsupportedOperationException();
      }

      @Override
      public Task[] newArray(final int size) {
        return new Task[size];
      }
    };

    private final Callable<String> delegate;

    public Task(final Callable<String> delegate) {
      this.delegate = delegate;
    }

    @Override
    public String call() throws Exception {
      return delegate.call();
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

    }
  }

}
