package com.stanfy.app.service

import org.junit.Before;

import com.stanfy.test.AbstractGroovyEnroscarTest


abstract class AbstractAppServiceTest extends AbstractGroovyEnroscarTest {

  /** Applciation service. */
  ApplicationService appService

  @Before
  void setup() {
    appService = new ApplicationService()
    appService.onCreate()
  }

}
