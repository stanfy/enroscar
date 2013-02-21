package com.stanfy.app.beans;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotates a named entity.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface EnroscarBean {

  /**
   * @return bean name
   */
  String value();

  /**
   * @return true if this bean requires context to be created
   */
  boolean contextDependent() default false;

}
