package com.stanfy.app;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.app.service.ApplicationService;
import com.stanfy.app.service.ToolsApplicationService;
import com.stanfy.images.DefaultDownloader;
import com.stanfy.images.DefaultImagesDAO;
import com.stanfy.images.Downloader;
import com.stanfy.images.ImagesManager;
import com.stanfy.images.ImagesManagerContext;
import com.stanfy.images.ImagesManagerContext.MemCacheMode;
import com.stanfy.images.ImagesManagerContextProvider;
import com.stanfy.images.model.CachedImage;
import com.stanfy.serverapi.RequestMethodHelper;
import com.stanfy.stats.EmptyStatsManager;
import com.stanfy.stats.StatsManager;
import com.stanfy.utils.AppUtils;
import com.stanfy.utils.Time;

/**
 * Base application class.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class Application extends android.app.Application implements ImagesManagerContextProvider {

  /** Images manager instance. */
  private ImagesManagerContext<?> imagesContext;

  /** HTTP clients pool. */
  private HttpClientsPool httpClientsPool;

  /** Request method helper. */
  private RequestMethodHelper requestMethodHelper;

  /** Stats manager. */
  private StatsManager statsManager;

  /** Authority to access images cache content. */
  private String imagesDAOAuthority;

  /** Crucial GUI operation listeners. */
  private ArrayList<CrucialGUIOperationListener> crucialGuiOperationListeners;

  /** State of running crucial GUI operation. */
  private boolean crucialGuiOperationRunning = false;

  /** Main thread instance. */
  private Thread mainThread;

  /**
   * Setup images cache cleanup.
   * By default this method schedules cache cleanup in 3 minutes after application instance creation.
   */
  protected void setupImagesCacheCleanup() {
    final Class<?> toolsClass = getApplicationToolsServiceClass();
    if (toolsClass == null) { return; }
    final long delay = 1 * Time.MINUTES;
    final AlarmManager alarmM = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
    final PendingIntent intent = PendingIntent.getService(this, 0, new Intent(this, toolsClass).setAction(ToolsApplicationService.ACTION_IMAGES_CACHE_CLEANUP),
        PendingIntent.FLAG_UPDATE_CURRENT);
    alarmM.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + delay, intent);
  }

  @Override
  public void onCreate() {
    this.mainThread = Thread.currentThread();
    super.onCreate();
    setupImagesCacheCleanup();
    if (DebugFlags.STRICT_MODE) {
      AppUtils.getSdkDependentUtils().enableStrictMode();
    }
  }

  @Override
  public void onLowMemory() {
    Log.w(getClass().getSimpleName(), "Low memory!");
    if (requestMethodHelper != null) {
      requestMethodHelper.flush();
    }
    if (imagesContext != null) {
      imagesContext.flush();
    }
    if (httpClientsPool != null) {
      httpClientsPool.flush();
    }
  }

  /** @param imagesDAOAuthority authority to access images cache content */
  public void setImagesDAOAuthority(final String imagesDAOAuthority) { this.imagesDAOAuthority = imagesDAOAuthority; }

  /** @return new instance of images context */
  protected ImagesManagerContext<?> createImagesContext() {
    final Downloader downloader = new DefaultDownloader(getHttpClientsPool());
    final ImagesManager<CachedImage> imagesManager = new ImagesManager<CachedImage>(getResources());
    final DefaultImagesDAO imagesDAO = new DefaultImagesDAO(this, imagesDAOAuthority);

    final ImagesManagerContext<CachedImage> result = new ImagesManagerContext<CachedImage>();
    result.setDownloader(downloader);
    result.setImagesManager(imagesManager);
    result.setImagesDAO(imagesDAO);
    result.setMemCache(MemCacheMode.LRU);
    return result;
  }

  /** @return new instance of HTTP clients pool */
  protected HttpClientsPool createHttpClientsPool() { return new HttpClientsPool(this); }

  /** @return images context */
  @Override
  public final synchronized ImagesManagerContext<?> getImagesContext() {
    if (imagesContext == null) {
      imagesContext = createImagesContext();
    }
    return imagesContext;
  }

  /** @return HTTP clients pool */
  public final synchronized HttpClientsPool getHttpClientsPool() {
    if (httpClientsPool == null) {
      httpClientsPool = createHttpClientsPool();
    }
    return httpClientsPool;
  }

  /**
   * Override this method if you want to achieve custom HTTP requests building and response processing.
   * @return new instance of {@link RequestMethodHelper} with JSON as main API format
   */
  protected RequestMethodHelper createRequestMethodHelper() { return new RequestMethodHelper(RequestMethodHelper.TYPE_JSON, null); }

  /** @return the requestMethodHelper */
  public final synchronized RequestMethodHelper getRequestMethodHelper() {
    if (requestMethodHelper == null) {
      requestMethodHelper = createRequestMethodHelper();
    }
    return requestMethodHelper;
  }

  /**
   * @return main application service class
   */
  public Class<?> getApplicationServiceClass() { return ApplicationService.class; }
  /**
   * @return tools application service class
   */
  public Class<?> getApplicationToolsServiceClass() { return ToolsApplicationService.class; }

  /**
   * @param a activity
   * @return behavior instance
   */
  protected BaseActivityBehavior createActivityBehavior(final Activity a) { return new BaseActivityBehavior(a); }

  /**
   * @return stats manager instance
   */
  protected StatsManager createStatsManager() { return new EmptyStatsManager(); }

  /** @return the statsManager */
  public synchronized StatsManager getStatsManager() {
    if (statsManager == null) {
      statsManager = createStatsManager();
    }
    return statsManager;
  }

  /**
   * @return new instance of action bar helper
   */
  public ActionBarSupport createActionBarSupport() { return new ActionBarSupport(); }

  /**
   * @return true if location methods implementation should be accessible from service
   */
  public boolean addLocationSupportToService() { return false; }

  void checkThread() {
    if (mainThread != Thread.currentThread()) {
      throw new IllegalStateException("This is operation is allowed in main thread only");
    }
  }

  /**
   * This method is called when some GUI animations start.
   * It should prevent some heavy background operation, suspend them.
   * This method must be called from the main thread.
   * @see Application#dispatchCrucialGUIOperationFinish()
   */
  public void dispatchCrucialGUIOperationStart() {
    checkThread();
    crucialGuiOperationRunning = true;
    if (imagesContext != null) {
      imagesContext.getImagesManager().pauseLoading();
    }
    final ArrayList<CrucialGUIOperationListener> listeners = crucialGuiOperationListeners;
    if (listeners != null) {
      final int listsnersCount = listeners.size();
      for (int i = listsnersCount - 1; i >= 0; i--) {
        listeners.get(i).onStartCrucialGUIOperation();
      }
    }
  }

  /**
   * This method is called when some GUI animations finish.
   * It should resume previously suspended heavy background operation.
   * Note also that this method is always called from {@link Activity#onPause()}
   * in order to be safe and prevent permanent locks.
   * This method must be called from the main thread.
   * @see Application#dispatchCrucialGUIOperationStart()
   */
  public void dispatchCrucialGUIOperationFinish() {
    checkThread();
    if (!crucialGuiOperationRunning) { return; }
    crucialGuiOperationRunning = false;
    if (imagesContext != null) {
      imagesContext.getImagesManager().resumeLoading();
    }
    final ArrayList<CrucialGUIOperationListener> listeners = crucialGuiOperationListeners;
    if (listeners != null) {
      final int listsnersCount = listeners.size();
      for (int i = listsnersCount - 1; i >= 0; i--) {
        listeners.get(i).onFinishCrucialGUIOperation();
      }
    }
  }

  /**
   * Add a crucial GUI operation listener.
   * @param listener listener instance
   */
  public void addCrucialGUIOperationListener(final CrucialGUIOperationListener listener) {
    if (listener == null) { throw new NullPointerException("Crucial GUI operation listener is null!"); }
    checkThread();
    if (crucialGuiOperationListeners == null) {
      crucialGuiOperationListeners = new ArrayList<CrucialGUIOperationListener>();
    }
    crucialGuiOperationListeners.add(listener);
  }

  /**
   * Remove a crucial GUI operation listener.
   * @param listener listener instance
   */
  public void removeCrucialGUIOperationListener(final CrucialGUIOperationListener listener) {
    if (listener == null) { throw new NullPointerException("Crucial GUI operation listener is null!"); }
    checkThread();
    if (crucialGuiOperationListeners == null) { return; }
    crucialGuiOperationListeners.remove(listener);
  }

  /**
   * Crucial GUI operation listener.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  public interface CrucialGUIOperationListener {
    /**
     * This method is called when some GUI animations start.
     */
    void onStartCrucialGUIOperation();
    /**
     * This method is called when some GUI animations finish.
     */
    void onFinishCrucialGUIOperation();
  }

}
