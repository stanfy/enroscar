package com.stanfy.test;

import com.xtremelabs.robolectric.bytecode.ClassHandler;
import com.xtremelabs.robolectric.bytecode.RobolectricClassLoader;
import com.xtremelabs.robolectric.bytecode.ShadowWrangler;

/**
 * Class loader that handles Groovy dependencies.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public final class EnroscarClassLoader extends RobolectricClassLoader {

  /** Instance. */
  private static EnroscarClassLoader instance;

  private EnroscarClassLoader(final ClassHandler handler) {
    super(handler);
  }

  public static EnroscarClassLoader getInstance() {
    if (instance == null) {
      instance = new EnroscarClassLoader(ShadowWrangler.getInstance());
    }
    return instance;
  }

  @Override
  public Class<?> loadClass(final String name) throws ClassNotFoundException {
    final boolean shouldComeFromThisClassLoader = !(
        name.startsWith("groovy")
        || name.startsWith("org.codehaus.groovy")
        || name.startsWith("junit")
    );
    return shouldComeFromThisClassLoader
        ? super.loadClass(name)
        : getParent().loadClass(name);
  }

}
