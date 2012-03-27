package com.stanfy;

import org.junit.runners.model.InitializationError;

import android.app.Application;

import com.xtremelabs.robolectric.RobolectricTestRunner;

/**
 * Custom test runner.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 *
 */
public class EnroscarTestRunner extends RobolectricTestRunner {

  public EnroscarTestRunner(final Class<?> testClass) throws InitializationError {
    super(testClass);
  }

  @Override
  protected Application createApplication() {
    return new com.stanfy.app.Application();
  }

}
