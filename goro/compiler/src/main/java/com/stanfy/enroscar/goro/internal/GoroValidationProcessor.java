package com.stanfy.enroscar.goro.internal;

import com.stanfy.enroscar.goro.annotations.BindGoro;
import com.stanfy.enroscar.goro.annotations.Bound;
import com.stanfy.enroscar.goro.annotations.UnbindGoro;

import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

import static javax.lang.model.element.ElementKind.FIELD;
import static javax.lang.model.element.ElementKind.METHOD;
import static javax.tools.Diagnostic.Kind.ERROR;

/**
 * Validates that class where {@link Bound} field is defined has methods
 * for binding and unbinding.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
@SupportedAnnotationTypes({ "*" })
public class GoroValidationProcessor extends AbstractProcessor {

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public boolean process(final Set<? extends TypeElement> annotations,
                         final RoundEnvironment roundEnv) {
    Set<Element> processedClasses = new HashSet<>();
    HashSet<Element> processedGoroMethods = new HashSet<>();

    // TODO support class hierarchies

    // validate fields
    Set<? extends Element> goroFields = roundEnv.getElementsAnnotatedWith(Bound.class);
    for (Element field : goroFields) {
      Element clazz = field.getEnclosingElement();
      if (processedClasses.contains(clazz)) {
        error("Bound Goro field is already declared in " + clazz.getSimpleName(), field);
        continue;
      }
      processedGoroMethods.addAll(validateMethods(clazz, field));
      processedClasses.add(clazz);
    }

    // find methods without fields
    for (Element method : roundEnv.getElementsAnnotatedWith(BindGoro.class)) {
      if (!processedGoroMethods.contains(method)) {
        ensureGoroField(method);
      }
    }
    for (Element method : roundEnv.getElementsAnnotatedWith(UnbindGoro.class)) {
      if (!processedGoroMethods.contains(method)) {
        ensureGoroField(method);
      }
    }

    return false;
  }

  private void ensureGoroField(final Element method) {
    Element clazz = method.getEnclosingElement();
    boolean found = false;
    for (Element e : clazz.getEnclosedElements()) {
      if (e.getKind() == FIELD && e.getAnnotation(Bound.class) != null) {
        found = true;
        break;
      }
    }
    if (!found) {
      error("@Bound Goro field not found in class " + clazz.getSimpleName()
          + ", though there are binding methods", clazz);
    }
  }

  private Set<Element> validateMethods(final Element clazz, final Element field) {
    HashSet<Element> processedMethods = new HashSet<>();
    Element bindMethod = null, unbindMethod = null;
    for (Element element : clazz.getEnclosedElements()) {
      if (element.getKind() == METHOD) {

        if (element.getAnnotation(BindGoro.class) != null) {
          processedMethods.add(element);
          if (bindMethod != null) {
            error("Class cannot have multiple @BindGoro methods", element);
          } else {
            bindMethod = element;
          }
        } else if (element.getAnnotation(UnbindGoro.class) != null) {
          processedMethods.add(element);
          if (unbindMethod != null) {
            error("Class cannot have multiple @UnbindGoro methods", element);
          } else {
            unbindMethod = element;
          }
        }

      }
    }

    if (bindMethod == null) {
      error("@BindGoro method not found for field " + field.getSimpleName(), field);
    }
    if (unbindMethod == null) {
      error("@UnbindGoro method not found for field " + field.getSimpleName(), field);
    }

    return processedMethods;
  }

  private void error(final String message, final Element element) {
    processingEnv.getMessager().printMessage(ERROR, message, element);
  }

}
