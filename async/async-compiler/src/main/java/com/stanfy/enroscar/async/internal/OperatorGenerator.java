package com.stanfy.enroscar.async.internal;

import com.squareup.javawriter.JavaWriter;
import com.stanfy.enroscar.async.Load;
import com.stanfy.enroscar.async.OperatorBuilder;
import com.stanfy.enroscar.async.rx.RxLoad;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;

import static com.stanfy.enroscar.async.internal.GenUtils.*;
import static com.stanfy.enroscar.async.internal.OperatorBase.OperatorBuilderBase;
import static com.stanfy.enroscar.async.internal.OperatorBase.OperatorContext;
import static javax.lang.model.element.Modifier.*;

/**
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
final class OperatorGenerator extends BaseGenerator {

  // TODO: release method support

  public OperatorGenerator(final ProcessingEnvironment env, final TypeElement type,
                           final List<MethodData> methods) {
    super(env, type, methods, GenUtils.SUFFIX_OPERATOR);

    addImports(
        AsyncProvider.class.getName(),
        OperatorBase.class.getName(), OperatorContext.class.getName().replace('$', '.'),
        OperatorBuilder.class.getName(), OperatorBuilderBase.class.getName().replace('$', '.')
    );

    for (MethodData d : methods) {
      addImports(d.operatorTypeSupport.operatorImports());
    }

    String operationsName = operationsClass.getQualifiedName().toString();
    setExtendsClass(OperatorBase.class.getSimpleName() + "<" + operationsName + ","
        + " " + loaderDescription(packageName, operationsClass) + ">");
  }

  @Override
  protected void writeClassBody(JavaWriter w) throws IOException {
    w.emitSingleLineComment("construction");

    w.beginConstructor(EnumSet.of(PRIVATE), "final " + operatorContext(operationsClass), "context");
    w.emitStatement("super(new %s(context), context)", loaderDescription(packageName, operationsClass));
    w.endConstructor();

    String buildClass = OperatorBuilder.class.getName()
        + "<" + getFqcn() + ", " + operationsClass.getQualifiedName() + ">";
    w.beginMethod(buildClass, "build", EnumSet.of(PUBLIC, STATIC));
    w.emitStatement(
        "return new " + OperatorBuilderBase.class.getSimpleName()
        + "<" + getFqcn() + ", " + operationsClass.getQualifiedName() + ">() {\n"
        + "  @Override\n"
        + "  protected " + getFqcn() + " create(final " + operatorContext(operationsClass) + " context) {\n"
        + "    return new " + getFqcn() + "(context);\n"
        + "  }\n"
        + "}"
    );
    w.endMethod();

    w.emitEmptyLine();
    w.emitSingleLineComment("invocation");

    for (MethodData data : methods) {
      ExecutableElement m = data.method;

      boolean load = m.getAnnotation(Load.class) != null || m.getAnnotation(RxLoad.class) != null;
      int loaderId = getLoaderId(m);

      w.beginMethod("void", m.getSimpleName().toString(), EnumSet.of(PUBLIC), parameters(w, m), null);
      w.emitStatement(data.operatorTypeSupport.asyncProvider(w, m));
      w.emitStatement("initLoader(%d, provider, %b)", loaderId, !load);
      w.endMethod();
      w.emitEmptyLine();

      if (load) {
        // force method
        w.beginMethod("void", "force" + capitalize(m.getSimpleName().toString()),
            EnumSet.of(PUBLIC), parameters(w, m), null);
        w.emitStatement(data.operatorTypeSupport.asyncProvider(w, m));
        w.emitStatement("restartLoader(%d, provider)", loaderId);
        w.endMethod();
        w.emitEmptyLine();
      }
    }

  }

}
