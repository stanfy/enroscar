package com.stanfy.app.service;

import org.junit.Before;
import org.junit.runner.RunWith;

import com.stanfy.EnroscarTestRunner;

/**
 * Application service tests.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@RunWith(EnroscarTestRunner.class)
public abstract class AbstractAppServiceTest {

  /** Application service. */
  ApplicationService appService;

  @Before
  public void setup() {
    appService = new ApplicationService();
    appService.onCreate();
  }

}
