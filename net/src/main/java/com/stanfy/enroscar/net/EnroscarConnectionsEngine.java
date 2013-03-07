package com.stanfy.enroscar.net;

import static android.content.ContentResolver.SCHEME_ANDROID_RESOURCE;
import static android.content.ContentResolver.SCHEME_CONTENT;
import static android.content.ContentResolver.SCHEME_FILE;

import java.net.ContentHandler;
import java.net.ContentHandlerFactory;
import java.net.ResponseCache;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import android.content.Context;
import android.os.Build;

import com.stanfy.enroscar.net.cache.ResponseCacheSwitcher;

/**
 * Creates custom stream handlers. Holds a reference to application context.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class EnroscarConnectionsEngine implements URLStreamHandlerFactory, ContentHandlerFactory {

  /** Install flag. */
  private static boolean installFlag = false;
  /** Working state flag. */
  private static boolean workingFlag = false;

  /** Content handler factory. */
  private static ContentHandlerFactory contentHandlerFactory;

  /** Treat 'file' scheme flag. */
  static boolean treatFileScheme = true;

  /** Context instance. */
  private final Context context;

  public EnroscarConnectionsEngine(final Context context) {
    this.context = context.getApplicationContext();
  }

  public static ContentHandlerFactory getContentHandlerFactory() { return contentHandlerFactory; }

  /** @return configurator instance */
  public static Config config() { return new Config(); }

  public static boolean isInstalled() { return installFlag; }

  /**
   * Installs the instance of this factory.
   * @param context context instance
   */
  static void install(final Context context, final Config config) {
    if (!installFlag) {
      // workarounds
      disableConnectionReuseIfNecessary();
      // install handlers
      installStreamHandlers(context, config);
      // enable strict mode
      if (DebugFlags.STRICT_MODE) {
        // TODO enable strict mode
        //AppUtils.getSdkDependentUtils().enableStrictMode();
      }
    }

    // install cache
    if (!workingFlag && config.installCache) {
      ResponseCache.setDefault(new ResponseCacheSwitcher());
    }

    installFlag = true;
    workingFlag = true;
  }

  public static void uninstall() {
    workingFlag = false;
    ResponseCache.setDefault(null);
  }

  private static void installStreamHandlers(final Context context, final Config config) {
    // force handler classes to be load before setting the factory
    ContentUriStreamHandler.class.getSimpleName();
    ContentUriConnection.class.getSimpleName();

    final EnroscarConnectionsEngine factory = new EnroscarConnectionsEngine(context);
    if (config.installStreamHandlers) {
      URL.setURLStreamHandlerFactory(factory);
    }
    if (config.installContentHandlers) {
      contentHandlerFactory = factory;
      URLConnection.setContentHandlerFactory(factory);
    }
  }

  /** Workaround for http://code.google.com/p/android/issues/detail?id=2939. */
  private static void disableConnectionReuseIfNecessary() {
    // HTTP connection reuse which was buggy pre-froyo
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.FROYO) {
      System.setProperty("http.keepAlive", "false");
    }
  }

  static boolean isContentResolverScheme(final String protocol) {
    return SCHEME_ANDROID_RESOURCE.equals(protocol) || SCHEME_CONTENT.equals(protocol) || (treatFileScheme && SCHEME_FILE.equals(protocol));
  }

  @Override
  public URLStreamHandler createURLStreamHandler(final String protocol) {
    // content resolver schemes
    if (workingFlag && isContentResolverScheme(protocol)) {
      return new ContentUriStreamHandler(context.getContentResolver());
    }
    // use default handlers
    return null;
  }

  @Override
  public ContentHandler createContentHandler(final String contentType) {
    return workingFlag ? new ContentHandlerSwitcher() : null;
  }

  /**
   * Configuration.
   */
  public static class Config {
    /** Configuration flag. */
    boolean installStreamHandlers = true, installContentHandlers = true, installCache = true;

    Config() { /* nothing */ }

    public Config installStreamHandlers(final boolean flag) {
      this.installStreamHandlers = flag;
      return this;
    }

    public Config installContentHandlers(final boolean flag) {
      this.installContentHandlers = flag;
      return this;
    }

    public Config installCache(final boolean flag) {
      this.installCache = flag;
      return this;
    }

    /**
     * Install engine.
     * @param context context instance
     */
    public void install(final Context context) {
      EnroscarConnectionsEngine.install(context, this);
    }

  }

}
