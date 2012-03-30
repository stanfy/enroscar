package com.stanfy.test;

import java.io.File;

import org.junit.runners.model.InitializationError;

import com.xtremelabs.robolectric.RobolectricConfig;
import com.xtremelabs.robolectric.RobolectricTestRunner;
import com.xtremelabs.robolectric.bytecode.ShadowWrangler;

/**
 * Custom test runner.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class EnroscarTestRunner extends RobolectricTestRunner {

  /** Cache path property name. */
  private static final String ROBOLECTRIC_CACHE_PATH = "cached.roboelectric.classes.path";

  /** Instrument detector. */
  private static final InstrumentDetector IDETECTOR = new InstrumentDetector() {
    @Override
    public boolean isInstrumented() {
      return EnroscarTestRunner.class.getClassLoader().getClass().getName().contains(EnroscarClassLoader.class.getName());
    }
  };

  static {
    setInstrumentDetector(IDETECTOR);
    String tmpPath = System.getProperty(ROBOLECTRIC_CACHE_PATH);
    if (tmpPath == null || "".equals(tmpPath.trim())) {
      tmpPath = "./build/output/temp/robolectric";
      System.setProperty(ROBOLECTRIC_CACHE_PATH, tmpPath);
    }
    new File(tmpPath).mkdirs();
  }

  public EnroscarTestRunner(final Class<?> testClass) throws InitializationError {
    super(
        testClass,
        isInstrumented() ? null : ShadowWrangler.getInstance(),
        isInstrumented() ? null : EnroscarClassLoader.getInstance(),
        new RobolectricConfig(new File("."))
        );
  }

  @Override
  protected Application createApplication() { return new Application(); }

}
