package com.stanfy.enroscar.async.internal;

import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.Load;
import com.stanfy.enroscar.async.Send;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public final class AsyncProcessor extends AbstractProcessor {

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return new HashSet<>(Arrays.asList(
        Load.class.getCanonicalName(),
        Send.class.getCanonicalName(),
        Rx.LOAD,
        Rx.SEND
    ));
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations,
                         final RoundEnvironment roundEnv) {
    Map<TypeElement, List<MethodData>> classMethods =
        new LinkedHashMap<>();
    collectAndValidate(classMethods, Load.class, roundEnv);
    collectAndValidate(classMethods, Send.class, roundEnv);
    if (Rx.hasRx()) {
      collectAndValidate(classMethods, Rx.rxLoad(), roundEnv);
      collectAndValidate(classMethods, Rx.rxSend(), roundEnv);
    }

    for (Map.Entry<TypeElement, List<MethodData>> e : classMethods.entrySet()) {
      generateCode(e.getKey(), e.getValue());
    }

    return false;
  }

  private void collectAndValidate(final Map<TypeElement, List<MethodData>> classMethods,
                                  final Class<? extends Annotation> annotation,
                                  final RoundEnvironment roundEnv) {
    for (Element m : roundEnv.getElementsAnnotatedWith(annotation)) {
      if (!(m instanceof ExecutableElement)) {
        throw new IllegalStateException(m + " annotated with @" + annotation.getSimpleName());
      }
      ExecutableElement method = (ExecutableElement) m;

      Element encl = method.getEnclosingElement();
      if (!(encl instanceof TypeElement)) {
        throw new IllegalStateException(method + " annotated with @" + annotation.getSimpleName()
            + " in " + encl);
      }
      TypeElement type = (TypeElement) encl;

      String returnType = GenUtils.getReturnType(method);

      TypeSupport operatorTypeSupport = null;
      if (returnType.startsWith(Async.class.getCanonicalName().concat("<"))) {
        operatorTypeSupport = TypeSupport.ASYNC;
      } else if (returnType.startsWith(TypeSupport.RX_OBSERVABLE.concat("<"))) {
        operatorTypeSupport = TypeSupport.RX;
      }

      if (operatorTypeSupport == null) {
        error(method, "Method annotated with @" + annotation.getSimpleName()
            + " must return either Async<T> or rx.Observable<T>");
        continue;
      }

      TypeSupport loaderDescriptionTypeSupport = operatorTypeSupport;
      if (Rx.hasRx() && (annotation == Rx.rxLoad() || annotation == Rx.rxSend())) {
        loaderDescriptionTypeSupport = TypeSupport.RX;
      }

      List<MethodData> methods = classMethods.get(type);
      if (methods == null) {
        methods = new ArrayList<>();
        classMethods.put(type, methods);
      }
      methods.add(new MethodData(method, operatorTypeSupport, loaderDescriptionTypeSupport));
    }
  }

  private void generateCode(final TypeElement baseType, final List<MethodData> methods) {
    new LoaderDescriptionGenerator(processingEnv, baseType, methods).generateCode();
    new OperatorGenerator(processingEnv, baseType, methods).generateCode();
  }

  private void error(final Element element, final String message) {
    processingEnv.getMessager().printMessage(ERROR, message, element);
  }

}
