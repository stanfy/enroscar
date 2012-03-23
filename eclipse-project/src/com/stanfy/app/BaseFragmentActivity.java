package com.stanfy.app;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.KeyEvent;

import com.stanfy.serverapi.request.RequestExecutor;
import com.stanfy.utils.ApiMethodsSupport;
import com.stanfy.utils.ApiMethodsSupport.ApiSupportRequestCallback;
import com.stanfy.utils.ChainedRequestCallback;
import com.stanfy.utils.LocationMethodsSupport;

/**
 * Fragment activity with common behavior.
 * @param <AT> application type
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class BaseFragmentActivity<AT extends Application> extends FragmentActivity implements ActionBarActivity, RequestExecutorProvider, LocationSupportProvider {

  /** Behavior. */
  private BaseActivityBehavior behavior;

  /** Request callback for API methods support. */
  private ChainedRequestCallback requestCallback;

  /** @return the behavior */
  protected BaseActivityBehavior getBehavior() { return behavior; }

  /** @return application instance */
  @SuppressWarnings("unchecked")
  public AT getApp() { return (AT)getApplication(); }


  public ChainedRequestCallback getRequestCallback() { return requestCallback; }

  /**
   * This method is called from {@link #onCreate(Bundle)}.
   * @return server API methods support
   */
  protected ApiMethodsSupport createApiMethodsSupport() {
    return requestCallback != null ? new ApiMethodsSupport(this, requestCallback, false) : null;
  }

  /**
   * Ensure that this activity is a root task when started from launcher.
   * Usage:
   * <pre>
   *   public void onCreate() {
   *     super.onCreate();
   *     if (!ensureRoot()) { return; }
   *     ...
   *   }
   * </pre>
   * @return false id this activity was incorrectly started from launcher
   */
  protected boolean ensureRoot() { return behavior.ensureRoot(); }

  @Override
  public void setupLocationSupport() { behavior.setupLocationSupport(); }
  @Override
  public LocationMethodsSupport getLocationSupport() { return behavior.getLocationSupport(); }
  @Override
  public RequestExecutor getRequestExecutor() { return behavior.getRequestExecutor(); }

  /**
   * Perform here all the operations required before creating API methods support.
   * @param savedInstanceState saved state
   */
  protected void onInitialize(final Bundle savedInstanceState) {
    // nothing
  }

  /** @param callback the requestCallback to set */
  public void addRequestCallback(final ApiSupportRequestCallback<?> callback) {
    if (callback == null) { return; }
    if (this.requestCallback == null) {
      this.requestCallback = new ChainedRequestCallback();
      if (behavior.getServerApiSupport() == null) {
        behavior.forceAPIBinding(createApiMethodsSupport());
      }
    }
    this.requestCallback.addCallback(callback);
  }

  /** @param callback request callback to remove */
  public void removeRequestCallback(final ApiSupportRequestCallback<?> callback) {
    if (this.requestCallback != null) {
      this.requestCallback.removeCallback(callback);
    }
  }

  /**
   * Return true here if your activity needs to ensure that it's binded to API service after {@link #onCreate(Bundle)} is finished.
   * @return whether to force binding to API service in {@link #onCreate(Bundle)}
   */
  protected boolean forceApiSupportOnCreate() { return false; }

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    behavior = getApp().createActivityBehavior(this);
    super.onCreate(savedInstanceState);
    onInitialize(savedInstanceState);
    if (requestCallback == null && forceApiSupportOnCreate()) {
      requestCallback = new ChainedRequestCallback();
    }
    behavior.onCreate(savedInstanceState, behavior.getServerApiSupport() == null ? createApiMethodsSupport() : null);
  }
  @Override
  protected void onStart() {
    super.onStart();
    behavior.onStart();
  }
  @Override
  protected void onRestart() {
    super.onRestart();
    behavior.onRestart();
  }
  @Override
  protected void onResume() {
    super.onResume();
    behavior.onResume();
  }
  @Override
  protected void onSaveInstanceState(final Bundle outState) {
    behavior.onSaveInstanceState(outState);
    super.onSaveInstanceState(outState);
  }
  @Override
  protected void onRestoreInstanceState(final Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    behavior.onRestoreInstanceState(savedInstanceState);
  }
  @Override
  protected void onPause() {
    behavior.onPause();
    super.onPause();
  }
  @Override
  protected void onStop() {
    behavior.onStop();
    super.onStop();
  }
  @Override
  protected void onDestroy() {
    behavior.onDestroy();
    if (requestCallback != null) { requestCallback.destroy(); }
    super.onDestroy();
  }
  @Override
  public void onContentChanged() {
    super.onContentChanged();
    behavior.onContentChanged();
  }
  @Override
  public ActionBarSupport getActionBarSupport() { return behavior.getActionBarSupport(); }
  @Override
  public boolean onKeyDown(final int keyCode, final KeyEvent event) {
    final boolean r = behavior.onKeyDown(keyCode, event);
    if (r) { return true; }
    return super.onKeyDown(keyCode, event);
  }

}
