package com.stanfy.enroscar.async.internal;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Before;
import org.junit.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.stanfy.enroscar.async.internal.GenUtils.LOADER_ID_START;
import static org.truth0.Truth.ASSERT;

/**
 * RxJava related tests.
 */
public class RxTest {

  /** Processor. */
  private final AsyncProcessor processor = new AsyncProcessor();

  @Before
  public void reset() {
    GenUtils.reset();
  }

  @Test
  public void rxObservablesOperator() {
    JavaFileObject file = JavaFileObjects.forSourceString("A", Joiner.on("\n").join(
        "import com.stanfy.enroscar.async.Load;",
        "import com.stanfy.enroscar.async.Async;",
        "import rx.Observable;",
        "class A {",
        "  @Load Observable<String> operation(int a) { return Observable.never(); }",
        "}"));

    JavaFileObject operatorSource = JavaFileObjects.forSourceString("AOperator",
        Joiner.on('\n').join(
            "import com.stanfy.enroscar.async.OperatorBuilder;",
            "import com.stanfy.enroscar.async.internal.AsyncProvider;",
            "import com.stanfy.enroscar.async.internal.ObservableAsyncProvider;",
            "import com.stanfy.enroscar.async.internal.OperatorBase;",
            "import com.stanfy.enroscar.async.internal.OperatorBase.OperatorBuilderBase;",
            "import com.stanfy.enroscar.async.internal.OperatorBase.OperatorContext;",
            "import rx.Observable;",

            "class AOperator extends OperatorBase<A, A$$LoaderDescription> {",

            "  // construction",
            "  private AOperator(final OperatorContext<A> context) {",
            "    super(new A$$LoaderDescription(context), context);",
            "  }",
            "  public static OperatorBuilder<AOperator, A> build() {",
            "    return new OperatorBuilderBase<AOperator, A>() {",
            "      @Override",
            "      protected AOperator create(final OperatorContext<A> context) {",
            "        return new AOperator(context);",
            "      }",
            "    };",
            "  }",

            "  // invocation",
            "  public void operation(final int a) {",
            "    ObservableAsyncProvider<String> provider = new ObservableAsyncProvider<String>() {",
            "      @Override",
            "      protected Observable<String> provideObservable() {",
            "        return getOperations().operation(a);",
            "      }",
            "    };",
            "    initLoader(" + LOADER_ID_START + ", provider, false);",
            "  }",

            "  public void forceOperation(final int a) {",
            "    ObservableAsyncProvider<String> provider = new ObservableAsyncProvider<String>() {",
            "      @Override",
            "      protected Observable<String> provideObservable() {",
            "        return getOperations().operation(a);",
            "      }",
            "    };",
            "    restartLoader(" + LOADER_ID_START + ", provider, false);",
            "  }",

            "  public void cancelOperation() {",
            "    destroyLoader(" + LOADER_ID_START + ");",
            "  }",

            "}"
        )
    );

    JavaFileObject loaderDescriptionSource = JavaFileObjects.forSourceString("A$$LoaderDescription",
        Joiner.on('\n').join(
            "import com.stanfy.enroscar.async.internal.LoaderDescription;",
            "import com.stanfy.enroscar.async.internal.ObservableTools;",
            "import com.stanfy.enroscar.async.internal.OperatorBase.OperatorContext;",
            "import rx.Observable;",

            "class A$$LoaderDescription extends LoaderDescription {",

            "  A$$LoaderDescription(final OperatorContext<A> context) {",
            "    super(context);",
            "  }",

            "  public Observable<String> operationIsFinished() {",
            "    return ObservableTools.loaderObservable(" + LOADER_ID_START + ", this, false);",
            "  }",

            "  public A$$LoaderDescription operationIsStartedDo(final Runnable action) {",
            "    addStartAction(" + LOADER_ID_START + ", action);",
            "    return this;",
            "  }",
            "}"
        )
    );

    ASSERT.about(javaSource())
        .that(file).processedWith(processor)
        .compilesWithoutError().and()
        .generatesSources(operatorSource, loaderDescriptionSource);
  }

  @Test
  public void rxLoad() {
    JavaFileObject file = JavaFileObjects.forSourceString("A", Joiner.on("\n").join(
        "import com.stanfy.enroscar.async.rx.RxLoad;",
        "import com.stanfy.enroscar.async.Async;",
        "import " + AsyncStub.class.getCanonicalName() + ";",
        "class A {",
        "  @RxLoad Async<String> operation(int a) { return new AsyncStub(); }",
        "}"));

    JavaFileObject operatorSource = JavaFileObjects.forSourceLines("AOperator",
        Joiner.on('\n').join(
            "import com.stanfy.enroscar.async.Async;",
            "import com.stanfy.enroscar.async.OperatorBuilder;",
            "import com.stanfy.enroscar.async.internal.AsyncProvider;",
            "import com.stanfy.enroscar.async.internal.OperatorBase;",
            "import com.stanfy.enroscar.async.internal.OperatorBase.OperatorBuilderBase;",
            "import com.stanfy.enroscar.async.internal.OperatorBase.OperatorContext;",

            "class AOperator extends OperatorBase<A, A$$LoaderDescription> {",

            "  // construction",
            "  private AOperator(final OperatorContext<A> context) {",
            "    super(new A$$LoaderDescription(context), context);",
            "  }",
            "  public static OperatorBuilder<AOperator, A> build() {",
            "    return new OperatorBuilderBase<AOperator, A>() {",
            "      @Override",
            "      protected AOperator create(final OperatorContext<A> context) {",
            "        return new AOperator(context);",
            "      }",
            "    };",
            "  }",

            "  // invocation",
            "  public void operation(final int a) {",
            "    AsyncProvider<String> provider = new AsyncProvider<String>() {",
            "      @Override",
            "      public Async<String> provideAsync() {",
            "        return getOperations().operation(a);",
            "      }",
            "    };",
            "    initLoader(" + LOADER_ID_START + ", provider, false);",
            "  }",

            "  public void forceOperation(final int a) {",
            "    AsyncProvider<String> provider = new AsyncProvider<String>() {",
            "      @Override",
            "      public Async<String> provideAsync() {",
            "        return getOperations().operation(a);",
            "      }",
            "    };",
            "    restartLoader(" + LOADER_ID_START + ", provider, false);",
            "  }",

            "  public void cancelOperation() {",
            "    destroyLoader(" + LOADER_ID_START + ");",
            "  }",

            "}"
        )
    );

    JavaFileObject loaderDescriptionSource = JavaFileObjects.forSourceString("A$$LoaderDescription",
        Joiner.on('\n').join(
            "import com.stanfy.enroscar.async.internal.LoaderDescription;",
            "import com.stanfy.enroscar.async.internal.ObservableTools;",
            "import com.stanfy.enroscar.async.internal.OperatorBase.OperatorContext;",
            "import rx.Observable;",

            "class A$$LoaderDescription extends LoaderDescription {",

            "  A$$LoaderDescription(final OperatorContext<A> context) {",
            "    super(context);",
            "  }",

            "  public Observable<String> operationIsFinished() {",
            "    return ObservableTools.loaderObservable(" + LOADER_ID_START + ", this, false);",
            "  }",

            "  public A$$LoaderDescription operationIsStartedDo(final Runnable action) {",
            "    addStartAction(" + LOADER_ID_START + ", action);",
            "    return this;",
            "  }",
            "}"
        )
    );

    ASSERT.about(javaSource())
        .that(file).processedWith(processor)
        .compilesWithoutError().and()
        .generatesSources(operatorSource, loaderDescriptionSource);
  }
}
