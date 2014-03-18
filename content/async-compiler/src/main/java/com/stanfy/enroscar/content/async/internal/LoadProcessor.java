package com.stanfy.enroscar.content.async.internal;

import com.stanfy.enroscar.content.async.Async;
import com.stanfy.enroscar.content.async.Load;
import com.stanfy.enroscar.content.async.Send;

import java.io.IOException;
import java.io.Writer;
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
import javax.tools.JavaFileObject;

import static javax.lang.model.element.Modifier.ABSTRACT;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public final class LoadProcessor extends AbstractProcessor {

  @Override
  public Set<String> getSupportedAnnotationTypes() {
    return new HashSet<>(Arrays.asList(
        Load.class.getCanonicalName(),
        Send.class.getCanonicalName()
    ));
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations,
                         final RoundEnvironment roundEnv) {
    Map<TypeElement, List<ExecutableElement>> classMethods =
        new LinkedHashMap<>();
    collectAndValidate(classMethods, Load.class, roundEnv);
    collectAndValidate(classMethods, Send.class, roundEnv);

    for (Map.Entry<TypeElement, List<ExecutableElement>> e : classMethods.entrySet()) {
      generateLoader(e.getKey(), e.getValue());
    }

    return false;
  }

  private void collectAndValidate(final Map<TypeElement, List<ExecutableElement>> classMethods,
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

      if (type.getModifiers().contains(FINAL)) {
        error(type, "Class with @" + annotation.getSimpleName() + " annotations must not be final");
        continue;
      }
      if (method.getModifiers().contains(FINAL)) {
        error(method, "Method annotated with @" + annotation.getSimpleName() + " must not be final");
        continue;
      }
      if (method.getModifiers().contains(ABSTRACT)) {
        error(method, "Method annotated with @" + annotation.getSimpleName() + " must not be abstract");
        continue;
      }

      String expectedReturn = Async.class.getCanonicalName();
      if (!GenUtils.getReturnType(method).startsWith(expectedReturn)) {
        error(method, "Method annotated with @" + annotation.getSimpleName() + " must return " + expectedReturn);
        continue;
      }

      List<ExecutableElement> methods = classMethods.get(type);
      if (methods == null) {
        methods = new ArrayList<>();
        classMethods.put(type, methods);
      }
      methods.add(method);
    }
  }

  private void generateLoader(final TypeElement baseType, final List<ExecutableElement> methods) {
    LoaderGenerator gen = new LoaderGenerator(processingEnv, baseType, methods);
    Writer out = null;
    try {
      JavaFileObject jfo = processingEnv.getFiler().createSourceFile(gen.getFqcn(), baseType);
      out = jfo.openWriter();
      GenUtils.generate(gen, out);
      out.flush();
    } catch (IOException e) {
      error(baseType, "Cannot generate loader for base class " + baseType + ": " + e.getMessage());
    } finally {
      if (out != null) {
        try {
          out.close();
        } catch (IOException e) {
          // nothing
        }
      }
    }
  }

  private void error(final Element element, final String message) {
    processingEnv.getMessager().printMessage(ERROR, message, element);
  }

}
