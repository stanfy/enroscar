package com.stanfy.test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnroscarTestConfiguration {

  /**
   * @return whether connections engine must be initialized
   */
  boolean connectionEngineRequired() default false;

}
