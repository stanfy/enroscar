package com.stanfy.enroscar.async.internal;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.enroscar.async.Async;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.ExecutableElement;

import static com.stanfy.enroscar.async.internal.GenUtils.*;

/**
 * Type support.
 */
interface TypeSupport {

  String asyncProvider(JavaWriter w, ExecutableElement method);

  Set<String> operatorImports();

  Set<String> loaderDescriptionImports();

  String loaderDescriptionReturnType(JavaWriter w, ExecutableElement method, BaseGenerator generator);

  String loaderDescriptionMethodBody(JavaWriter w, ExecutableElement method, BaseGenerator generator);

  String listenerExample(String dataType);


  String RX_OBSERVABLE = "rx.Observable";

  TypeSupport ASYNC = new TypeSupport() {

    @Override
    public Set<String> operatorImports() {
      return Collections.singleton(Async.class.getName());
    }

    @Override
    public Set<String> loaderDescriptionImports() {
      return Collections.singleton(ObserverBuilder.class.getCanonicalName());
    }

    @Override
    public String asyncProvider(final JavaWriter w, final ExecutableElement method) {
      String type = w.compressType(AsyncProvider.class.getName() + "<" + getDataType(method) + ">");
      return type + " provider = new " + type + "() {\n"
          + "  @Override\n"
          + "  public " + w.compressType(getReturnType(method)) + " provideAsync() {\n"
          + "    return getOperations()." + invocation(method) + ";\n"
          + "  }\n"
          + "}";
    }

    @Override
    public String loaderDescriptionReturnType(JavaWriter w, ExecutableElement method,
                                              BaseGenerator generator) {
      String dataType = getDataType(method).toString();
      return w.compressType(ObserverBuilder.class.getName()
          + "<" + dataType + "," +
          loaderDescription(generator.packageName, generator.operationsClass) + ">");
    }

    @Override
    public String loaderDescriptionMethodBody(JavaWriter w, ExecutableElement method,
                                              BaseGenerator generator) {
      String type = loaderDescriptionReturnType(w, method, generator);
      return String.format("return new %s(%d, this, %b)",
          type, generator.getLoaderId(method), !isLoadMethod(method));
    }

    @Override
    public String listenerExample(String dataType) {
      return String.format("doOnResult(new Action<%s>() { ... })", dataType);
    }

    @Override
    public String toString() {
      return "AsyncSupport";
    }
  };

  TypeSupport RX = new TypeSupport() {

    private static final String PROVIDER = "ObservableAsyncProvider";
    private static final String TOOLS = "ObservableTools";

    @Override
    public Set<String> operatorImports() {
      return new HashSet<>(Arrays.asList(
          RX_OBSERVABLE,
          getClass().getPackage().getName() + "." + PROVIDER
      ));
    }

    @Override
    public Set<String> loaderDescriptionImports() {
      return new HashSet<>(Arrays.asList(
          RX_OBSERVABLE,
          getClass().getPackage().getName() + "." + TOOLS
      ));
    }

    @Override
    public String asyncProvider(final JavaWriter w, final ExecutableElement method) {
      String type = w.compressType(PROVIDER + "<" + getDataType(method) + ">");
      return type + " provider = new " + type + "() {\n"
          + "  @Override\n"
          + "  protected " + w.compressType(getReturnType(method)) + " provideObservable() {\n"
          + "    return getOperations()." + invocation(method) + ";\n"
          + "  }\n"
          + "}";
    }

    @Override
    public String loaderDescriptionReturnType(JavaWriter w, ExecutableElement method,
                                              BaseGenerator generator) {
      return w.compressType(RX_OBSERVABLE + "<" + getDataType(method) + ">");
    }

    @Override
    public String loaderDescriptionMethodBody(JavaWriter w, ExecutableElement method,
                                              BaseGenerator generator) {
      return String.format("return %s.loaderObservable(%d, this, %b)",
          TOOLS, generator.getLoaderId(method), !isLoadMethod(method));
    }

    @Override
    public String listenerExample(String dataType) {
      return String.format("subscribe(new Action1<%s>() { ... })", dataType);
    }

    @Override
    public String toString() {
      return "RxSupport";
    }
  };

}
