package com.stanfy.enroscar.async.internal;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.AsyncObserver;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.stanfy.enroscar.async.internal.GenUtils.LOADER_ID_START;
import static org.truth0.Truth.ASSERT;

/**
 * Tests for AsyncProcessor.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE, emulateSdk = 18)
public class AsyncProcessorTest {

  static {
    GenUtils.debug = true;
  }

  /** Processor. */
  private final AsyncProcessor processor = new AsyncProcessor();

  @Before
  public void reset() {
    GenUtils.reset();
  }

  @Test
  public void methodErrors() {
    JavaFileObject file = JavaFileObjects.forSourceString("Error", Joiner.on("\n").join(
        "import com.stanfy.enroscar.async.Load;",
        "import com.stanfy.enroscar.async.Send;",
        "import com.stanfy.enroscar.async.Async;",
        "class Error {",
        "  @Load Async<String> one() { }",
        "  @Load String four() { }",
        "  String five() { }",
        "  @Send int six() { }",
        "}"));

    ASSERT.about(javaSource())
        .that(file).processedWith(processor)
        .failsToCompile()

        .withErrorContaining("@Load").in(file).onLine(6).and()
        .withErrorContaining("Async").in(file).onLine(6).and()

        .withErrorContaining("@Send").in(file).onLine(8).and()
        .withErrorContaining("Async").in(file).onLine(8);
  }

  @Test
  public void loadMethods() throws Exception {
    JavaFileObject file = JavaFileObjects.forSourceString("Operations", Joiner.on("\n").join(
        "import com.stanfy.enroscar.async.Load;",
        "import com.stanfy.enroscar.async.Async;",
        "import " + AsyncStub.class.getCanonicalName() + ";",
        "class Operations {",
        "  @Load Async<String> operation(int a1, String a2) { return new AsyncStub(); }",
        "}"));

    ASSERT.about(javaSource())
        .that(file).processedWith(processor)
        .compilesWithoutError().and()
        .generatesSources(loadExpectedSource("Operations")).and()
        .generatesSources(loaderDescriptionExpected("Operations"));
  }

  @Test
  public void testInnerClassSupport() throws Exception {
    JavaFileObject file = JavaFileObjects.forSourceString("Outer", Joiner.on("\n").join(
        "import com.stanfy.enroscar.async.Load;",
        "import com.stanfy.enroscar.async.Async;",
        "import " + AsyncStub.class.getCanonicalName() + ";",
        "class Outer {",
        "  static class Inner {",
        "    @Load Async<String> operation(int a1, String a2) { return new AsyncStub(); }",
        "  }",
        "}"));

    ASSERT.about(javaSource())
        .that(file).processedWith(processor)
        .compilesWithoutError().and()
        .generatesSources(loadExpectedSource("Outer.Inner")); //.and() TODO
        //.generatesSources(loaderDescriptionExpected("Outer.Inner"));

  }

  private JavaFileObject loadExpectedSource(final String className) {
    String base = className.replace(".", "");
    return JavaFileObjects.forSourceString(base + "Operator",
        Joiner.on('\n').join(
            "import com.stanfy.enroscar.async.Async;",
            "import com.stanfy.enroscar.async.OperatorBuilder;",
            "import com.stanfy.enroscar.async.internal.AsyncProvider;",
            "import com.stanfy.enroscar.async.internal.OperatorBase;",
            "import com.stanfy.enroscar.async.internal.OperatorBase.OperatorBuilderBase;",
            "import com.stanfy.enroscar.async.internal.OperatorBase.OperatorContext;",

            "class " + base + "Operator extends OperatorBase<" + className + ", "
                + base + "$$LoaderDescription> {",

            "  // construction",
            "  private " + base + "Operator(final OperatorContext<" + className + "> context) {",
            "    super(new " + base + "$$LoaderDescription(context), context);",
            "  }",
            "  public static OperatorBuilder<" + base + "Operator, " + className + "> build() {",
            "    return new OperatorBuilderBase<" + base + "Operator, " + className + ">() {",
            "      @Override",
            "      protected " + base + "Operator create(final OperatorContext<" + className + "> context) {",
            "        return new " + base + "Operator(context);",
            "      }",
            "    };",
            "  }",

            "  // invocation",
            "  public void operation(final int a1, final String a2) {",
            "     AsyncProvider<String> provider = new AsyncProvider<String>() {",
            "       @Override",
            "       public Async<String> provideAsync() {",
            "         return getOperations().operation(a1, a2);",
            "       }",
            "     };",
            "     initLoader(" + LOADER_ID_START + ", provider, false);",
            "  }",

            "  public void forceOperation(final int a1, final String a2) {",
            "    AsyncProvider<String> provider = new AsyncProvider<String>() {",
            "      @Override",
            "      public Async<String> provideAsync() {",
            "        return getOperations().operation(a1, a2);",
            "      }",
            "    };",
            "    restartLoader(" + LOADER_ID_START + ", provider);",
            "  }",

            "}"
        )
    );
  }

  private JavaFileObject loaderDescriptionExpected(final String className) {
    String base = className.replace(".", "");

    return JavaFileObjects.forSourceString(base + "$$LoaderDescription",
        Joiner.on('\n').join(
            "import com.stanfy.enroscar.async.internal.LoaderDescription;",
            "import com.stanfy.enroscar.async.internal.ObserverBuilder;",
            "import com.stanfy.enroscar.async.internal.OperatorBase.OperatorContext;",

            "class " + base + "$$LoaderDescription extends LoaderDescription {",

            "  " + base + "$$LoaderDescription(final OperatorContext<Operations> context) {",
            "    super(context);",
            "  }",

            "  public ObserverBuilder<String, " + base + "$$LoaderDescription> operationIsFinished() {",
            "    return new ObserverBuilder<String," + base + "$$LoaderDescription>(" + LOADER_ID_START + ", this);",
            "  }",
            "}"
        )
    );
  }

  @Test
  public void sendMethods() {
    JavaFileObject file = JavaFileObjects.forSourceString("WithSendMethods", Joiner.on("\n").join(
        "import com.stanfy.enroscar.async.Load;",
        "import com.stanfy.enroscar.async.Send;",
        "import com.stanfy.enroscar.async.Async;",
        "import " + AsyncStub.class.getCanonicalName() + ";",
        "class WithSendMethods {",
        "  @Send Async<String> operation(float a1) { return new AsyncStub(); }",
        "}"));

//    JavaFileObject expected = JavaFileObjects.forSourceString("WithSendMethods$$Loader",
//        Joiner.on('\n').join(
//            "import android.content.Context;",
//            "import android.support.v4.app.LoaderManager;",
//            "import com.stanfy.enroscar.async.Async;",
//            "import com.stanfy.enroscar.async.internal.AsyncContext;",
//            "import com.stanfy.enroscar.async.internal.AsyncProvider;",
//            "import com.stanfy.enroscar.async.internal.LoadAsync;",
//            "import com.stanfy.enroscar.async.internal.SendAsync;",
//
//            "class WithSendMethods$$Loader extends WithSendMethods {",
//
//            "  private final Context context;",
//            "  private final LoaderManager loaderManager;",
//
//            "  private final AsyncContext.DelegatedContext<String> twoContext;",
//            "  private final Async<String> twoAsync;",
//
//            "  WithSendMethods$$Loader(Context context, LoaderManager loaderManager) {",
//            "    this.context = context;",
//            "    this.loaderManager = loaderManager;",
//
//            "    this.twoContext = new AsyncContext.DelegatedContext<String>(context);",
//            "    this.twoAsync = new SendAsync<String>(loaderManager, this.twoContext, " + LOADER_ID_START + ");",
//            "  }",
//
//            "  @Override",
//            "  Async<String> one(final int a1, final String a2) {",
//            "    return new LoadAsync<String>(loaderManager, new AsyncContext<String>(super.one(a1, a2), context), " + (LOADER_ID_START + 1) + ");",
//            "  }",
//
//            "  @Override",
//            "  Async<String> two(final float a1) {",
//            "    this.twoContext.setDelegate(new AsyncProvider<String>() {",
//            "      @Override public Async<String> provideAsync() {",
//            "        return WithSendMethods$$Loader.super.two(a1);",
//            "      }",
//            "    });",
//            "    return this.twoAsync;",
//            "  }",
//
//            "}"
//        )
//    );

    ASSERT.about(javaSource())
        .that(file).processedWith(processor)
        .compilesWithoutError(); // TODO .and()
        //.generatesSources(expected);
  }

  // TODO: loader IDs, multiple methods, release methods

  /** Stub. */
  public static class AsyncStub implements Async<String> {
    @Override
    public void subscribe(final AsyncObserver<String> observer) {
      throw new UnsupportedOperationException();
    }

    @Override
    public void cancel() {
      throw new UnsupportedOperationException();
    }

    @Override
    public Async<String> replicate() {
      return new AsyncStub();
    }
  }

}
