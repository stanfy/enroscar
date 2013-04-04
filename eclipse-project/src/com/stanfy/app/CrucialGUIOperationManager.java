package com.stanfy.app;

import java.util.HashSet;

import android.os.Looper;
import android.util.Log;

import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.beans.InitializingBean;
import com.stanfy.images.ImagesManager;

/**
 * Crucial GUI operation manager.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(value = CrucialGUIOperationManager.BEAN_NAME)
public class CrucialGUIOperationManager implements InitializingBean {

  /** Bean name. */
  public static final String BEAN_NAME = "CrucialGUIOperationManager";

  /** Logging tag. */
  private static final String TAG = BEAN_NAME;

  /** State of running crucial GUI operation. */
  private boolean crucialGuiOperationRunning = false;

  /** Crucial GUI operation listeners. */
  private HashSet<CrucialGUIOperationListener> crucialGuiOperationListeners;

  /** Images manager. */
  private ImagesManager imagesManager;

  private void checkThread() {
    if (Looper.myLooper() != Looper.getMainLooper()) {
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

    if (!newObject) { Log.e(TAG, "You are trying to add the same listener again"); }
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

  @Override
  public void onInitializationFinished(final BeansContainer container) {
    this.imagesManager = container.getBean(ImagesManager.BEAN_NAME, ImagesManager.class);
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
