package com.stanfy.enroscar.content.async;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.RetentionPolicy.SOURCE;
import static java.lang.annotation.ElementType.METHOD;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
@Retention(SOURCE)
@Target(METHOD)
public @interface Load {
}
