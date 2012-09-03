package android.support.v4.app;

import java.lang.reflect.Field;

import android.support.v4.app.LoaderManagerImpl.LoaderInfo;
import android.support.v4.util.SparseArrayCompat;

/**
 * Provides access to {@link LoaderManagerImpl}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public final class LoaderManagerImplAccess {

  private LoaderManagerImplAccess() { /* hidden */ }

  public static LoaderManager initLoaderManager(final LoaderManager loaderManager, final FragmentActivity activity) {
    try {

      final Field activityField = LoaderManagerImpl.class.getDeclaredField("mActivity");
      activityField.setAccessible(true);
      activityField.set(loaderManager, activity);

      final Field loadersField = LoaderManagerImpl.class.getDeclaredField("mLoaders");
      loadersField.setAccessible(true);
      loadersField.set(loaderManager, new SparseArrayCompat<LoaderInfo>());

      final Field inactiveLoadersField = LoaderManagerImpl.class.getDeclaredField("mInactiveLoaders");
      inactiveLoadersField.setAccessible(true);
      inactiveLoadersField.set(loaderManager, new SparseArrayCompat<LoaderInfo>());

      final Field startedField = LoaderManagerImpl.class.getDeclaredField("mStarted");
      startedField.setAccessible(true);
      startedField.setBoolean(loaderManager, true);

      return loaderManager;
    } catch (final Exception e) {
      e.printStackTrace();
      throw new RuntimeException(e);
    }
  }

}
