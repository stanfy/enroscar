package com.stanfy.enroscar.content.async.internal;

import com.stanfy.enroscar.content.async.Async;
import com.stanfy.enroscar.content.async.Load;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
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
    return Collections.singleton(Load.class.getCanonicalName());
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
    for (Element m : roundEnv.getElementsAnnotatedWith(Load.class)) {
      if (!(m instanceof ExecutableElement)) {
        throw new IllegalStateException(m + " annotated with @Load");
      }
      ExecutableElement method = (ExecutableElement) m;

      Element encl = method.getEnclosingElement();
      if (!(encl instanceof TypeElement)) {
        throw new IllegalStateException(method + " annotated with @Loa in " + encl);
      }
      TypeElement type = (TypeElement) encl;

      if (type.getModifiers().contains(FINAL)) {
        error(type, "Class with @Load annotations must not be final");
        continue;
      }
      if (method.getModifiers().contains(FINAL)) {
        error(method, "Method annotated with @Load must not be final");
        continue;
      }
      if (method.getModifiers().contains(ABSTRACT)) {
        error(method, "Method annotated with @Load must not be abstract");
        continue;
      }

      String expectedReturn = Async.class.getCanonicalName();
      if (!GenUtils.getReturnType(method).startsWith(expectedReturn)) {
        error(method, "Method annotated with @Load must return " + expectedReturn);
        continue;
      }

      List<ExecutableElement> methods = classMethods.get(type);
      if (methods == null) {
        methods = new ArrayList<>();
        classMethods.put(type, methods);
      }
      methods.add(method);
    }

    for (Map.Entry<TypeElement, List<ExecutableElement>> e : classMethods.entrySet()) {
      generateLoader(e.getKey(), e.getValue());
    }

    return false;
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
