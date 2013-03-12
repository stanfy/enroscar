package com.stanfy.app;

import java.util.HashSet;

import android.content.Context;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.enroscar.beans.Bean;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.images.ImagesManager;

/**
 * Crucial GUI operation manager.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(value = CrucialGUIOperationManager.BEAN_NAME, contextDependent = true)
public class CrucialGUIOperationManager implements Bean {

  /** Bean name. */
  public static final String BEAN_NAME = "CrucialGUIOperationManager";

  /** Logging tag. */
  private static final String TAG = BEAN_NAME;
  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_GUI;

  /** Main thread instance. */
  private final Thread mainThread;

  /** State of running crucial GUI operation. */
  private boolean crucialGuiOperationRunning = false;

  /** Crucial GUI operation listeners. */
  private HashSet<CrucialGUIOperationListener> crucialGuiOperationListeners;

  /** Images manager. */
  private final ImagesManager imagesManager;

  public CrucialGUIOperationManager(final Context context) {
    this.mainThread = Thread.currentThread();
    this.imagesManager = BeansManager.get(context).getContainer().getBean(ImagesManager.class);
    if (imagesManager == null) {
      Log.i(TAG, "CrucialGUIOperationManager created BUT images manager is null. "
          + "If it's not an expected behavior, please make sure you declare images manager before this entity.");
    }
  }

  private void checkThread() {
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
    if (imagesManager != null) {
      imagesManager.pauseLoading();
    }
    final HashSet<CrucialGUIOperationListener> listeners = crucialGuiOperationListeners;
    if (listeners != null) {
      for (final CrucialGUIOperationListener crucialGUIOperationListener : listeners) {
        crucialGUIOperationListener.onStartCrucialGUIOperation();
      }
    }
  }

  /**
   * This method is called when some GUI animations finish.
   * It should resume previously suspended heavy background operation.
   * Note also that this method can be called from {@link android.app.Activity#onPause()}
   * in order to be safe and prevent permanent locks.
   * This method must be called from the main thread.
   * @see Application#dispatchCrucialGUIOperationStart()
   */
  public void dispatchCrucialGUIOperationFinish() {
    checkThread();
    if (!crucialGuiOperationRunning) { return; }
    crucialGuiOperationRunning = false;
    if (imagesManager != null) {
      imagesManager.resumeLoading();
    }
    final HashSet<CrucialGUIOperationListener> listeners = crucialGuiOperationListeners;
    if (listeners != null) {
      for (final CrucialGUIOperationListener crucialGUIOperationListener : listeners) {
        crucialGUIOperationListener.onFinishCrucialGUIOperation();
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
      crucialGuiOperationListeners = new HashSet<CrucialGUIOperationListener>();
    }
    final boolean newObject = crucialGuiOperationListeners.add(listener);

    if (DEBUG && !newObject) { Log.e(TAG, "You are trying to add the same listener again"); }
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
