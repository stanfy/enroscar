package com.stanfy.enroscar.net;

import android.content.Context;

import com.stanfy.enroscar.net.cache.ResponseCacheSwitcher;

import java.net.ResponseCache;
import java.net.URL;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;

import static android.content.ContentResolver.*;

/**
 * <p>
 *   Engine consists of two ingredients:
 *   <ol>
 *     <li>
 *       Stream handler factory which uses Android {@link android.content.ContentResolver} to treat
 *       such schemes as {@code content}, {@code android.resource}, and optionally {@code file}.
 *       Also adds support of {@code data} scheme.
 *     </li>
 *     <li>
 *       Cache switcher that delegates cache control to different {@link java.net.ResponseCache}
 *       implementations depending on extra information associated with
 *       {@link java.net.URLConnection}.
 *       See {@link com.stanfy.enroscar.net.cache.CacheControlUrlConnection}.
 *     </li>
 *   </ol>
 * </p>
 *
 * <p>
 *   All three components may be configured like:
 *   <pre>
 *     EnroscarConnectionsEngine.config()
 *         .withStreamHandlers(true) // add support for content:// scheme
 *         .treatFileScheme(false) // do not use ContentResolver for file:// URLs
 *         .withCacheSwitcher(false) // do not setup cache switcher
 *         .setup(context); // apply configuration
 *   </pre>
 *   By default all the flags are set to {@code true}. After {@code setup} is called,
 *   engine sets default stream handler factory with
 *   {@link java.net.URL#setURLStreamHandlerFactory(java.net.URLStreamHandlerFactory)}.
 * </p>
 *
 * <p>
 *   After engine is set up, you may create {@code URLConnection}s for supported schemes:
 *   <pre>
 *     new URL("content://com.some.app/data/2").openConnection().getInputStream();
 *     new URL("file:///android_assets/file.txt").openConnection().getInputStream();
 *   </pre>
 * </p>
 *
 * <p>
 *   If you need your custom factory, you may create Enroscar factory with
 *   {@link #createStreamHandlerFactory(android.content.Context)} and use the directly,
 *   wrapping e.g.
 * </p>
 */
public final class EnroscarConnectionsEngine {

  /** Config factory instance. */
  private static ConfigFactory configFactory = new ConfigFactory() {
    @Override
    public Config create() {
      return new Config();
    }
  };

  /** Engine instance. */
  static EnroscarConnectionsEngine engineInstance;

  /** Stream handler factory. */
  final EnroscarStreamHandlerFactory streamHandlerFactory;

  EnroscarConnectionsEngine(final Context context) {
    streamHandlerFactory = new EnroscarStreamHandlerFactory(context);
  }

  public static URLStreamHandlerFactory createStreamHandlerFactory(final Context context) {
    return new EnroscarStreamHandlerFactory(context);
  }

  public static void setConfigFactory(final ConfigFactory configFactory) {
    if (configFactory == null) {
      throw new IllegalArgumentException("config factory cannot be null");
    }
    EnroscarConnectionsEngine.configFactory = configFactory;
  }

  /** @return configurator instance */
  public static Config config() {
    return configFactory.create();
  }

  static EnroscarConnectionsEngine get() {
    return engineInstance;
  }

  /** Creates a config instance. */
  public interface ConfigFactory {
    Config create();
  }

  /**
   * Configuration.
   */
  public static class Config {

    /** Configuration lock. */
    private static final Object LOCK = new Object();

    /** Factory installation flag. */
    private static boolean streamHandlerInstalled, contentHandlerInstalled;

    /** Configuration flag. */
    private boolean useStreamHandlers = true,
                    useCacheSwitcher = true,
                    treatFileScheme = true;

    /** Engine instance. */
    private EnroscarConnectionsEngine engine;

    protected Config() { /* nothing */ }

    /** Whether to install custom {@link java.net.URLStreamHandlerFactory}. */
    public Config withStreamHandlers(final boolean flag) {
      this.useStreamHandlers = flag;
      return this;
    }

    /**
     * Whether to install cache switcher
     * (via {@link java.net.ResponseCache#setDefault(java.net.ResponseCache)}).
     */
    public Config withCacheSwitcher(final boolean flag) {
      this.useCacheSwitcher = flag;
      return this;
    }

    /** Whether to resolve {@code file} scheme using {@link android.content.ContentResolver}. */
    public Config treatFileScheme(final boolean flag) {
      this.treatFileScheme = flag;
      return this;
    }

    /** Remove all components. */
    public Config withNothing() {
      useStreamHandlers = false;
      useCacheSwitcher = false;
      return this;
    }

    private void configureStreamHandler() {
      if (useStreamHandlers && !streamHandlerInstalled) {
        streamHandlerInstalled = true;
        URL.setURLStreamHandlerFactory(engine.streamHandlerFactory);
      }
      engine.streamHandlerFactory.treatFileScheme = treatFileScheme;
    }

    /**
     * Install engine.
     * @param ctx context instance
     */
    public void setup(final Context ctx) {
      Context context = ctx.getApplicationContext();

      // force handler classes to be load before setting the factory
      ContentUriStreamHandler.class.getSimpleName();
      ContentUriConnection.class.getSimpleName();

      synchronized (LOCK) {
        if (engineInstance == null) {
          engineInstance = new EnroscarConnectionsEngine(context);
        }
        this.engine = engineInstance;

        // stream handler
        configureStreamHandler();

        // cache switcher
        if (useCacheSwitcher) {
          if (!(ResponseCache.getDefault() instanceof ResponseCacheSwitcher)) {
            ResponseCache.setDefault(new ResponseCacheSwitcher());
          }
        } else {
          ResponseCache.setDefault(null);
        }
      }
    }

  }

  /**
   * Stream handler factory implementation.
   * Uses Android {@link android.content.ContentResolver} to treat such schemes as
   * {@code content}, {@code android.resource}, and {@code file} if such an option is set.
   * Add support of {@code data} scheme.
   */
  static class EnroscarStreamHandlerFactory implements URLStreamHandlerFactory {

    /** Application context. */
    private final Context context;

    /** Whether to treat file scheme using content resolver. */
    boolean treatFileScheme;

    public EnroscarStreamHandlerFactory(final Context context) {
      this.context = context;
    }

    private boolean isContentResolverScheme(final String protocol) {
      return SCHEME_ANDROID_RESOURCE.equals(protocol)
          || SCHEME_CONTENT.equals(protocol)
          || (treatFileScheme && SCHEME_FILE.equals(protocol));
    }

    @Override
    public URLStreamHandler createURLStreamHandler(final String protocol) {
      if (isContentResolverScheme(protocol)) {
        return new ContentUriStreamHandler(context.getContentResolver());
      }
      if (DataStreamHandler.PROTOCOL.equals(protocol)) {
        return new DataStreamHandler();
      }
      return null;
    }

  }

}
