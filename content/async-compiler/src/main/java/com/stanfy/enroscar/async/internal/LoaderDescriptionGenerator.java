package com.stanfy.enroscar.async.internal;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.enroscar.async.OperatorBuilder;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

import static com.stanfy.enroscar.async.internal.GenUtils.getDataType;
import static com.stanfy.enroscar.async.internal.GenUtils.loaderDescription;
import static com.stanfy.enroscar.async.internal.GenUtils.operatorContext;
import static javax.lang.model.element.Modifier.PUBLIC;

/**
 * LoaderDescription class generator.
 */
final class LoaderDescriptionGenerator extends BaseGenerator {


  public LoaderDescriptionGenerator(final ProcessingEnvironment env, final TypeElement type,
                                    final List<ExecutableElement> methods) {
    super(env, type, methods, GenUtils.SUFFIX_LOADER_DESC);
    setExtendsClass(LoaderDescription.class.getName());
    setImports(
        LoaderDescription.class.getName(),
        OperatorBase.OperatorContext.class.getName().replace('$', '.'),
        ObserverBuilder.class.getName()
    );
  }

  @Override
  protected void writeClassBody(final JavaWriter w) throws IOException {
    w.beginConstructor(constructorModifiers(), "final " + operatorContext(operationsClass), "context");
    w.emitStatement("super(context)");
    w.endConstructor();

    w.emitEmptyLine();

    for (ExecutableElement m : methods) {
      String dataType = getDataType(m).toString();
      String builderType = w.compressType(ObserverBuilder.class.getName()
          + "<" + dataType + "," + loaderDescription(packageName, operationsClass) + ">");
      w.beginMethod(
          builderType,
          m.getSimpleName().toString().concat("IsFinished"),
          EnumSet.of(PUBLIC)
      );
      w.emitStatement("return new %s(%d, this)", builderType, getLoaderId(m));
      w.endMethod();
    }
  }

}
