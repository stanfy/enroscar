package com.stanfy.enroscar.async.internal;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.enroscar.async.Load;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

import static com.stanfy.enroscar.async.internal.OperatorBase.OperatorContext;

/**
 * Tools.
 */
final class GenUtils {

  static final String DOCS_OPERATOR_CLASS = "Provides methods to control asynchronous operations "
      + "described in class {@link %s}.%n<p>Usage examples:</p>%n"
      + "<pre>%n"
      + "  // construct operator instance%n"
      + "  %s operator = %s.build()%n"
      + "      .operations(this)%n"
      + "      .context(getActivity())%n"
      + "      .loaderManager(getSupportLoaderManager())%n"
      + "      .create();%n"
      + "%n"
      + "  // setup actions invoked when operation finishes%n"
      + "  operator.when().%sIsFinished()%n"
      + "      .%s%n"
      + "%n"
      + "  // trigger operation start%n"
      + "  operator.%s%s;%n"
      + "  // forcing operation start (discarding cached result by Loader)%n"
      + "  operator.force%s%s;%n"
      + "  // cancel operation%n"
      + "  operator.cancel%s();%n"
      + "</pre>%n";

  static boolean debug = false;

  /** Start value for loader ID. */
  static final int LOADER_ID_START = 3000;

  /** Loader id counter. */
  private static final AtomicInteger loaderId = new AtomicInteger(LOADER_ID_START);

  /** Generated class name suffix. */
  public static final String SUFFIX_OPERATOR = "Operator";
  /** Generated class name suffix. */
  public static final String SUFFIX_LOADER_DESC = "$$LoaderDescription";

  public static String getGeneratedClassName(final String packageName, final String name, final String suffix) {
    String base = name;
    if (packageName.length() > 0) {
      base = base.substring(packageName.length() + 1);
    }
    return base.replace(".", "").concat(suffix);
  }

  public static String loaderDescription(final String packageName, final TypeElement operationsClass) {
    return getGeneratedClassName(packageName, operationsClass.getQualifiedName().toString(), SUFFIX_LOADER_DESC);
  }

  public static String operatorContext(final TypeElement operationsClass) {
    return OperatorContext.class.getSimpleName() + "<" + operationsClass.getQualifiedName() + ">";
  }

  static String getReturnType(final ExecutableElement method) {
    return ((ExecutableType) method.asType()).getReturnType().toString();
  }

  static TypeMirror getDataType(ExecutableElement method) {
    ExecutableType execType = (ExecutableType) method.asType();
    DeclaredType returnType = (DeclaredType) execType.getReturnType();
    return returnType.getTypeArguments().get(0);
  }

  static void generate(final BaseGenerator gen, final Writer out) throws IOException {
    if (debug) {
      StringWriter sw = new StringWriter();
      gen.generateTo(sw);
      System.out.println(sw);
      out.write(sw.toString());
    } else {
      gen.generateTo(out);
    }
  }

  static boolean isLoadMethod(ExecutableElement method) {
    return method.getAnnotation(Load.class) != null
        || (Rx.hasRx() && method.getAnnotation(Rx.rxLoad()) != null);
  }

  public static String capitalize(final String s) {
    char first = Character.toUpperCase(s.charAt(0));
    return s.length() > 1 ? first + s.substring(1) : String.valueOf(first);
  }

  public static int nextLoaderId() {
    return loaderId.getAndIncrement();
  }

  static void reset() {
    loaderId.set(LOADER_ID_START);
  }

  public static String invocation(final ExecutableElement method) {
    return method.getSimpleName().toString().concat(invocationParams(method));
  }

  public static String invocationParams(final ExecutableElement method) {
    StringBuilder stmt = new StringBuilder().append("(");
    if (!method.getParameters().isEmpty()) {
      for (VariableElement arg : method.getParameters()) {
        stmt.append(arg.getSimpleName().toString()).append(", ");
      }
      stmt.delete(stmt.length() - 2, stmt.length());
    }
    stmt.append(")");
    return stmt.toString();
  }

  public static List<String> parameters(final JavaWriter w, final ExecutableElement method) {
    ArrayList<String> res = new ArrayList<String>(method.getParameters().size());
    for (VariableElement arg : method.getParameters()) {
      TypeMirror type = arg.asType();
      res.add("final " + w.compressType(type.toString()));
      res.add(arg.getSimpleName().toString());
    }
    return res;
  }
}
