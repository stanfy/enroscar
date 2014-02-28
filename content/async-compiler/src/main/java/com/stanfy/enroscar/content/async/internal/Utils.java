package com.stanfy.enroscar.content.async.internal;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.type.ExecutableType;

/**
 * Tools.
 */
final class Utils {

  static boolean debug = false;

  static String getReturnType(final ExecutableElement method) {
    return ((ExecutableType) method.asType()).getReturnType().toString();
  }

  static void generate(final LoaderGenerator gen, final Writer out) throws IOException {
    if (debug) {
      StringWriter sw = new StringWriter();
      gen.generateTo(sw);
      System.out.println(sw);
      out.write(sw.toString());
    } else {
      gen.generateTo(out);
    }
  }

}
