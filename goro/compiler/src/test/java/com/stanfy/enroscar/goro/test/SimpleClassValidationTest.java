package com.stanfy.enroscar.goro.test;

import com.google.common.base.Joiner;
import com.google.testing.compile.JavaFileObjects;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.JavaSourceSubjectFactory.javaSource;
import static org.truth0.Truth.ASSERT;

import static com.stanfy.enroscar.goro.test.TestUtil.goroProcessors;

/**
 * Tests validation.
 */
@RunWith(JUnit4.class)
public class SimpleClassValidationTest {

  @Test
  public void multipleFields() {
    JavaFileObject file = JavaFileObjects.forSourceString("MultipleFields", Joiner.on("\n").join(
        "import com.stanfy.enroscar.goro.annotations.Bound;",
        "import com.stanfy.enroscar.goro.Goro;",
        "class MultipleFields {",
        "  @Bound Goro goro;",
        "  @Bound Goro goro2;",
        "  @Bound Goro goro3;",
        "}"));

    String message = "Bound Goro field is already declared";
    ASSERT.about(javaSource())
        .that(file).processedWith(goroProcessors())
        .failsToCompile()
        .withErrorContaining(message).in(file).onLine(5).and()
        .withErrorContaining(message).in(file).onLine(6);
  }

  @Test
  public void multipleBindMethods() {
    JavaFileObject file = JavaFileObjects.forSourceString("BindError", Joiner.on("\n").join(
        "import com.stanfy.enroscar.goro.annotations.BindGoro;",
        "import com.stanfy.enroscar.goro.annotations.Bound;",
        "import com.stanfy.enroscar.goro.annotations.UnbindGoro;",
        "import com.stanfy.enroscar.goro.Goro;",
        "class BindError {",
        "  @Bound Goro goro;",
        "  @BindGoro",
        "  void bind() {}",
        "  @BindGoro",
        "  void bind2() {}",
        "  @UnbindGoro",
        "  void unbind() {}",
        "}"));

    ASSERT.about(javaSource())
        .that(file).processedWith(goroProcessors())
        .failsToCompile()
        .withErrorContaining("Class cannot have multiple @BindGoro methods").in(file).onLine(10);
  }

  @Test
  public void multipleUnbindMethods() {
    JavaFileObject file = JavaFileObjects.forSourceString("BindError", Joiner.on("\n").join(
        "import com.stanfy.enroscar.goro.annotations.BindGoro;",
        "import com.stanfy.enroscar.goro.annotations.Bound;",
        "import com.stanfy.enroscar.goro.annotations.UnbindGoro;",
        "import com.stanfy.enroscar.goro.Goro;",
        "class BindError {",
        "  @Bound Goro goro;",
        "  @BindGoro",
        "  void bind() {}",
        "  @UnbindGoro",
        "  void unbind() {}",
        "  @UnbindGoro",
        "  void unbind2() {}",
        "}"));

    ASSERT.about(javaSource())
        .that(file).processedWith(goroProcessors())
        .failsToCompile()
        .withErrorContaining("Class cannot have multiple @UnbindGoro methods").in(file).onLine(12);
  }

  @Test
  public void missingBindMethod() {
    JavaFileObject file = JavaFileObjects.forSourceString("MissingMethods", Joiner.on("\n").join(
        "import com.stanfy.enroscar.goro.annotations.Bound;",
        "import com.stanfy.enroscar.goro.Goro;",
        "class MissingMethods {",
        "  @Bound Goro goro;",
        "}"));

    ASSERT.about(javaSource())
        .that(file).processedWith(goroProcessors())
        .failsToCompile()
        .withErrorContaining("@BindGoro method not found").in(file).onLine(4);
  }

  @Test
  public void missingUnbindMethod() {
    JavaFileObject file = JavaFileObjects.forSourceString("MissingMethods", Joiner.on("\n").join(
        "import com.stanfy.enroscar.goro.annotations.BindGoro;",
        "import com.stanfy.enroscar.goro.annotations.Bound;",
        "import com.stanfy.enroscar.goro.Goro;",
        "class MissingMethods {",
        "  @Bound Goro goro;",
        "  @BindGoro",
        "  void bind() {}",
        "}"));

    ASSERT.about(javaSource())
        .that(file).processedWith(goroProcessors())
        .failsToCompile()
        .withErrorContaining("@UnbindGoro method not found").in(file).onLine(5);
  }

  @Test
  public void missingBoundField() {
    JavaFileObject file = JavaFileObjects.forSourceString("MissingField", Joiner.on("\n").join(
        "import com.stanfy.enroscar.goro.annotations.BindGoro;",
        "import com.stanfy.enroscar.goro.annotations.UnbindGoro;",
        "import com.stanfy.enroscar.goro.Goro;",
        "class MissingField {",
        "  @BindGoro",
        "  void bind() {}",
        "  @UnbindGoro",
        "  void unbind() {}",
        "}"));

    ASSERT.about(javaSource())
        .that(file).processedWith(goroProcessors())
        .failsToCompile()
        .withErrorContaining("@Bound Goro field not found").in(file).onLine(4);
  }

}
