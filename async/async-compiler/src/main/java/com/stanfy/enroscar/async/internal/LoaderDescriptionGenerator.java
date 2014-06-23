package com.stanfy.enroscar.async.internal;

import com.squareup.javawriter.JavaWriter;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import static com.stanfy.enroscar.async.internal.GenUtils.operatorContext;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * LoaderDescription class generator.
 */
final class LoaderDescriptionGenerator extends BaseGenerator {


  public LoaderDescriptionGenerator(final ProcessingEnvironment env, final TypeElement type,
                                    final List<MethodData> methods) {
    super(env, type, methods, GenUtils.SUFFIX_LOADER_DESC);
    setExtendsClass(LoaderDescription.class.getName());
    addImports(
        LoaderDescription.class.getName(),
        OperatorBase.OperatorContext.class.getName().replace('$', '.')
    );

    for (MethodData d : methods) {
      addImports(d.loaderDescriptionTypeSupport.loaderDescriptionImports());
    }
  }

  @Override
  protected void writeClassBody(final JavaWriter w) throws IOException {
    w.beginConstructor(constructorModifiers(), "final " + operatorContext(operationsClass), "context");
    w.emitStatement("super(context)");
    w.endConstructor();

    w.emitEmptyLine();

    for (MethodData data : methods) {
      ExecutableElement m = data.method;

      w.beginMethod(
          data.loaderDescriptionTypeSupport.loaderDescriptionReturnType(w, m, this),
          m.getSimpleName().toString().concat("IsFinished"),
          EnumSet.of(PUBLIC)
      );
      w.emitStatement(data.loaderDescriptionTypeSupport.loaderDescriptionMethodBody(w, m, this));
      w.endMethod();
    }
  }

}
