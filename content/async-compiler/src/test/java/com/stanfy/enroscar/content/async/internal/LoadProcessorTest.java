package com.stanfy.enroscar.content.async.internal;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;
import com.stanfy.enroscar.content.async.Async;
import com.stanfy.enroscar.content.async.AsyncObserver;
import com.stanfy.enroscar.content.async.internal.LoadProcessor;
import com.stanfy.enroscar.content.async.internal.Utils;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

/**
 * Tests for LoadProcessor.
 */
@RunWith(JUnit4.class)
public class LoadProcessorTest {

  static {
    Utils.debug = true;
  }

  /** Processor. */
  private final LoadProcessor processor = new LoadProcessor();

  @Test
  public void finalClass() {
    JavaFileObject file = JavaFileObjects.forSourceString("Error", Joiner.on("\n").join(
        "import com.stanfy.enroscar.content.async.Load;",
        "import com.stanfy.enroscar.content.async.Async;",
        "final class Error {",
        "  @Load Async<String> one() { }",
        "}"));

    ASSERT.about(javaSource())
        .that(file).processedWith(processor)
        .failsToCompile()
        .withErrorContaining("final").in(file).onLine(3);
  }

  @Test
  public void methodErrors() {
    JavaFileObject file = JavaFileObjects.forSourceString("Error", Joiner.on("\n").join(
        "import com.stanfy.enroscar.content.async.Load;",
        "import com.stanfy.enroscar.content.async.Async;",
        "class Error {",
        "  @Load Async<String> one() { }",
        "  @Load abstract Async<String> two() { }",
        "  @Load final Async<String> three() { }",
        "  @Load String four() { }",
        "  String five() { }",
        "}"));

    ASSERT.about(javaSource())
        .that(file).processedWith(processor)
        .failsToCompile()
        .withErrorContaining("abstract").in(file).onLine(5).and()
        .withErrorContaining("final").in(file).onLine(6).and()
        .withErrorContaining("Async").in(file).onLine(7);
  }

  @Test
  public void generatedCode() throws Exception {
    JavaFileObject file = JavaFileObjects.forSourceString("Generated", Joiner.on("\n").join(
        "import com.stanfy.enroscar.content.async.Load;",
        "import com.stanfy.enroscar.content.async.Async;",
        "import " + AsyncStub.class.getCanonicalName() + ";",
        "class Generated {",
        "  @Load Async<String> one(int a1, String a2) { return new AsyncStub(); }",
        "}"));

    JavaFileObject expectedSource = JavaFileObjects.forSourceString("Generated$Loader",
        Joiner.on('\n').join(
            "import com.stanfy.enroscar.content.async.Async;",
            "class Generated$Loader extends Generated {",
            "  @Override",
            "  Async<String> one(int a1, String a2) {",
            "    return super.one(a1, a2);",
            "  }",
            "}"
        ));

    Object b = ASSERT.about(javaSource())
        .that(file).processedWith(processor)
        .compilesWithoutError().and()
        .generatesSources(expectedSource);
  }

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
