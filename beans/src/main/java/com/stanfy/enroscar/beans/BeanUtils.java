package com.stanfy.enroscar.beans;

import java.lang.annotation.Annotation;


/**
 * Bean utilities.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public final class BeanUtils {

  private BeanUtils() { /* hidden */ }
  
  public static EnroscarBean getBeanInfo(final Class<?> clazz) {
    return getAnnotationFromHierarchy(clazz, EnroscarBean.class);
  }

  public static <A extends Annotation> A getAnnotationFromHierarchy(final Class<?> clazz, final Class<A> annotation) {
    Class<?> currentClass = clazz;
    A annotationInstance;
    do {
      annotationInstance = currentClass.getAnnotation(annotation);
      currentClass = currentClass.getSuperclass();
    } while (annotationInstance == null && currentClass != Object.class);
    return annotationInstance;
  }


}
