package com.stanfy.app.activities;

import java.util.HashMap;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.TabHost;

import com.stanfy.app.Application;
import com.stanfy.app.BaseFragmentActivity;

/**
 * Implements switching between the tabs of a
 * TabHost through fragments. It uses a trick to allow the
 * tabs to switch between fragments instead of simple views.
 * @param <AT> application type
 */
public abstract class FragmentTabsActivity<AT extends Application> extends BaseFragmentActivity<AT> {

  /** Saved tab key. */
  protected static final String SAVE_TAB = "tab";

  /** Tab host. */
  private TabHost mTabHost;
  /** Tab manager. */
  private TabManager mTabManager;

  @Override
  protected void onInitialize(final Bundle savedInstanceState) {
    super.onInitialize(savedInstanceState);
    setContentView(getLayoutId());

    mTabHost = (TabHost)findViewById(android.R.id.tabhost);
    mTabHost.setup();

    mTabManager = createTabManager(mTabHost, getFragmentsContainer());
  }

  protected TabManager createTabManager(final TabHost host, final int containerId) {
    return new TabManager(this, host, containerId);
  }

  @Override
  protected void onSaveInstanceState(final Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(SAVE_TAB, mTabHost.getCurrentTabTag());
  }

  @Override
  protected void onRestoreInstanceState(final Bundle savedInstanceState) {
    super.onRestoreInstanceState(savedInstanceState);
    mTabManager.setCurrentTabByTag(savedInstanceState.getString(SAVE_TAB));
  }

  public TabManager getTabManager() { return mTabManager; }
  public TabHost getTabHost() { return mTabHost; }

  public int getFragmentsContainer() { return android.R.id.tabcontent; }
  public abstract int getLayoutId();

  /**
   * This is a helper class that implements a generic mechanism for associating
   * fragments with the tabs in a tab host. It relies on a trick. Normally a tab
   * host has a simple API for supplying a View or Intent that each tab will
   * show. This is not sufficient for switching between fragments. So instead we
   * make the content part of the tab host 0dp high (it is not shown) and the
   * TabManager supplies its own dummy view to show as the tab content. It
   * listens to changes in tabs, and takes care of switch to the correct
   * fragment shown in a separate content area whenever the selected tab
   * changes.
   */
  public static class TabManager implements TabHost.OnTabChangeListener {
    /** Activity. */
    private final FragmentActivity mActivity;
    /** Tab host. */
    private final TabHost mTabHost;
    /** Container. */
    private final int mContainerId;
    /** Tabs. */
    private final HashMap<String, TabInfo> mTabs = new HashMap<String, TabInfo>();
    /** Last tab. */
    TabInfo mLastTab;

    /** Tab info. */
    protected static final class TabInfo {
      /** Tag. */
      private final String tag;
      /** Class. */
      private final Class<?> clss;
      /** Args. */
      private final Bundle args;
      /** Fragment. */
      private Fragment fragment;

      TabInfo(final String tag, final Class<?> claz, final Bundle args) {
        this.tag = tag;
        this.clss = claz;
        this.args = args;
      }
    }

    /** Tab factory. */
    static class DummyTabFactory implements TabHost.TabContentFactory {
      /** Context. */
      private final View view;

      public DummyTabFactory(final Context context) {
        final View v = new View(context);
        v.setMinimumWidth(0);
        v.setMinimumHeight(0);
        this.view = v;
      }

      @Override
      public View createTabContent(final String tag) {
        return view;
      }
    }

    public TabManager(final FragmentActivity activity, final TabHost tabHost, final int containerId) {
      mActivity = activity;
      mTabHost = tabHost;
      mContainerId = containerId;
      mTabHost.setOnTabChangedListener(this);
    }

    protected final void prepareSpec(final TabHost.TabSpec tabSpec) {
      tabSpec.setContent(new DummyTabFactory(mActivity));
    }

    protected void addContent(final String tag, final Class<?> fragmentClass, final Bundle args) {
      final TabInfo info = new TabInfo(tag, fragmentClass, args);

      // Check to see if we already have a fragment for this tab, probably
      // from a previously saved state. If so, deactivate it, because our
      // initial state is that a tab isn't shown.
      info.fragment = mActivity.getSupportFragmentManager().findFragmentByTag(
          tag);
      if (info.fragment != null && !info.fragment.isDetached()) {
        final FragmentTransaction ft = mActivity.getSupportFragmentManager()
            .beginTransaction();
        ft.detach(info.fragment);
        ft.commit();
      }

      mTabs.put(tag, info);
    }

    public void addTab(final TabHost.TabSpec tabSpec, final Class<?> clss,
        final Bundle args) {
      prepareSpec(tabSpec);
      final String tag = tabSpec.getTag();
      addContent(tag, clss, args);
      mTabHost.addTab(tabSpec);
    }

    public void clearAllTabs() {
      if (!mTabs.isEmpty()) { mTabHost.setCurrentTab(0); }
      mTabHost.clearAllTabs();
      final FragmentTransaction t = mActivity.getSupportFragmentManager().beginTransaction();
      boolean affect = false;
      for (final TabInfo tab : mTabs.values()) {
        if (tab.fragment != null && !tab.fragment.isDetached()) {
          t.detach(tab.fragment);
          affect = true;
        }
      }
      if (affect) { t.commit(); }
      mTabs.clear();
      mLastTab = null;
    }

    public void setCurrentTabByTag(final String tag) {
      mTabHost.setCurrentTabByTag(tag);
    }

    protected TabInfo getTabInfo(final String tabId) { return mTabs.get(tabId); }
    protected TabInfo getLastTabInfo() { return mLastTab; }

    @Override
    public void onTabChanged(final String tabId) {
      final TabInfo newTab = mTabs.get(tabId);
      if (mLastTab != newTab) {
        final FragmentTransaction ft = mActivity.getSupportFragmentManager()
            .beginTransaction();
        if (mLastTab != null && mLastTab.fragment != null) {
          ft.detach(mLastTab.fragment);
        }
        if (newTab != null) {
          if (newTab.fragment == null) {
            newTab.fragment = Fragment.instantiate(mActivity,
                newTab.clss.getName(), newTab.args);
            ft.add(mContainerId, newTab.fragment, newTab.tag);
          } else {
            ft.attach(newTab.fragment);
          }
        }

        mLastTab = newTab;
        ft.commit();
        mActivity.getSupportFragmentManager().executePendingTransactions();
      }
    }
  }
}
