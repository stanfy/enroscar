package com.stanfy.enroscar.async.internal;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.AsyncObserver;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static com.stanfy.enroscar.async.internal.LoaderGenerator.LOADER_ID_START;
import static org.truth0.Truth.ASSERT;

/**
 * Tests for AsyncProcessor.
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest=Config.NONE)
public class AsyncProcessorTest {

  static {
    GenUtils.debug = true;
  }

  /** Processor. */
  private final AsyncProcessor processor = new AsyncProcessor();

  @Test
  public void methodErrors() {
    JavaFileObject file = JavaFileObjects.forSourceString("Error", Joiner.on("\n").join(
        "import com.stanfy.enroscar.async.Load;",
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

        .withErrorContaining("@Load").in(file).onLine(5).and()
        .withErrorContaining("Async").in(file).onLine(5).and()

        .withErrorContaining("@Send").in(file).onLine(7).and()
        .withErrorContaining("Async").in(file).onLine(7);
  }

  @Test
  public void generatedCode() throws Exception {
    JavaFileObject file = JavaFileObjects.forSourceString("Generated", Joiner.on("\n").join(
        "import com.stanfy.enroscar.async.Load;",
        "import com.stanfy.enroscar.async.Async;",
        "import " + AsyncStub.class.getCanonicalName() + ";",
        "class Generated {",
        "  @Load Async<String> one(int a1, String a2) { return new AsyncStub(); }",
        "}"));

    JavaFileObject expectedSource = loadExpectedSource("Generated", "Generated");

    ASSERT.about(javaSource())
        .that(file).processedWith(processor)
        .compilesWithoutError().and()
        .generatesSources(expectedSource);
  }

  @Test
  public void testInnerClassSupport() throws Exception {
    JavaFileObject file = JavaFileObjects.forSourceString("Outer", Joiner.on("\n").join(
        "import com.stanfy.enroscar.async.Load;",
        "import com.stanfy.enroscar.async.Async;",
        "import " + AsyncStub.class.getCanonicalName() + ";",
        "class Outer {",
        "  static class Inner {",
        "    @Load Async<String> one(int a1, String a2) { return new AsyncStub(); }",
        "  }",
        "}"));

    JavaFileObject expectedSource = loadExpectedSource("Outer.Inner", "Outer$Inner");

    ASSERT.about(javaSource())
        .that(file).processedWith(processor)
        .compilesWithoutError().and()
        .generatesSources(expectedSource);

  }

  private JavaFileObject loadExpectedSource(final String baseClassName, final String className) {
    return JavaFileObjects.forSourceString(className + "$$Loader",
        Joiner.on('\n').join(
            "import android.content.Context;",
            "import android.support.v4.app.LoaderManager;",
            "import com.stanfy.enroscar.async.Async;",
            "import com.stanfy.enroscar.async.internal.AsyncContext;",
            "import com.stanfy.enroscar.async.internal.AsyncProvider;",
            "import com.stanfy.enroscar.async.internal.LoadAsync;",
            "import com.stanfy.enroscar.async.internal.SendAsync;",

            "class " + className + "$$Loader extends " +
                baseClassName + " {",

            "  private final Context context;",
            "  private final LoaderManager loaderManager;",

            "  " + className + "$$Loader(Context context, LoaderManager loaderManager) {",
            "    this.context = context;",
            "    this.loaderManager = loaderManager;",
            "  }",

            "  @Override",
            "  Async<String> one(final int a1, final String a2) {",
            "    return new LoadAsync<String>(loaderManager, new AsyncContext.DirectContext<String>(super.one(a1, a2), context), " + LOADER_ID_START + ");",
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
        "  @Load Async<String> one(int a1, String a2) { return new AsyncStub(); }",
        "  @Send Async<String> two(float a1) { return new AsyncStub(); }",
        "}"));

    JavaFileObject expected = JavaFileObjects.forSourceString("WithSendMethods$$Loader",
        Joiner.on('\n').join(
            "import android.content.Context;",
            "import android.support.v4.app.LoaderManager;",
            "import com.stanfy.enroscar.async.Async;",
            "import com.stanfy.enroscar.async.internal.AsyncContext;",
            "import com.stanfy.enroscar.async.internal.AsyncProvider;",
            "import com.stanfy.enroscar.async.internal.LoadAsync;",
            "import com.stanfy.enroscar.async.internal.SendAsync;",

            "class WithSendMethods$$Loader extends WithSendMethods {",

            "  private final Context context;",
            "  private final LoaderManager loaderManager;",

            "  private final AsyncContext.DelegatedContext<String> twoContext;",
            "  private final Async<String> twoAsync;",

            "  WithSendMethods$$Loader(Context context, LoaderManager loaderManager) {",
            "    this.context = context;",
            "    this.loaderManager = loaderManager;",

            "    this.twoContext = new AsyncContext.DelegatedContext<String>(context);",
            "    this.twoAsync = new SendAsync<String>(loaderManager, this.twoContext, " + LOADER_ID_START + ");",
            "  }",

            "  @Override",
            "  Async<String> one(final int a1, final String a2) {",
            "    return new LoadAsync<String>(loaderManager, new AsyncContext<String>(super.one(a1, a2), context), " + (LOADER_ID_START + 1) + ");",
            "  }",

            "  @Override",
            "  Async<String> two(final float a1) {",
            "    this.twoContext.setDelegate(new AsyncProvider<String>() {",
            "      @Override public Async<String> provideAsync() {",
            "        return WithSendMethods$$Loader.super.two(a1);",
            "      }",
            "    });",
            "    return this.twoAsync;",
            "  }",

            "}"
        )
    );

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
  }

}
