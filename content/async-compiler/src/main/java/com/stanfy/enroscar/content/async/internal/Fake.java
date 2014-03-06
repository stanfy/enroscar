package com.stanfy.enroscar.content.async.internal;

import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public class Fake extends AbstractProcessor {
  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    return false;
  }
}
