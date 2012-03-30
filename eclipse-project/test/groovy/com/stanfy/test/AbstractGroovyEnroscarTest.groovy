package com.stanfy.test

import java.lang.reflect.Method;

import org.hamcrest.Matchers;

/**
 * Base class for Groovy tests.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
abstract class AbstractGroovyEnroscarTest extends AbstractEnroscarTest {

  static {
    // borrow methods from Matchers
    Matchers.class.methods.each { Method method ->
      String name = method.name
      AbstractGroovyEnroscarTest.metaClass[name] = Matchers.&"$name";
    }
  }

}
