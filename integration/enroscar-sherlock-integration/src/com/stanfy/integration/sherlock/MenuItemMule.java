package com.stanfy.integration.sherlock;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.ActionProvider;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;

class MenuItemMule implements MenuItem {
  private static final String ERROR = "Cannot interact with object designed for temporary "
      + "instance passing. Make sure you using both SherlockFragmentActivity and "
      + "SherlockFragment.";


  private final com.actionbarsherlock.view.MenuItem mItem;

  public MenuItemMule(final com.actionbarsherlock.view.MenuItem item) {
    mItem = item;
  }

  public com.actionbarsherlock.view.MenuItem unwrap() {
    return mItem;
  }


  @Override
  public boolean collapseActionView() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public boolean expandActionView() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public ActionProvider getActionProvider() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public View getActionView() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public char getAlphabeticShortcut() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public int getGroupId() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public Drawable getIcon() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public Intent getIntent() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public int getItemId() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public ContextMenuInfo getMenuInfo() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public char getNumericShortcut() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public int getOrder() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public SubMenu getSubMenu() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public CharSequence getTitle() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public CharSequence getTitleCondensed() {
    return mItem.getTitleCondensed();
    //throw new IllegalStateException(ERROR);
  }

  @Override
  public boolean hasSubMenu() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public boolean isActionViewExpanded() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public boolean isCheckable() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public boolean isChecked() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public boolean isEnabled() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public boolean isVisible() {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setActionProvider(final ActionProvider arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setActionView(final View arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setActionView(final int arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setAlphabeticShortcut(final char arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setCheckable(final boolean arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setChecked(final boolean arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setEnabled(final boolean arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setIcon(final Drawable arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setIcon(final int arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setIntent(final Intent arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setNumericShortcut(final char arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setOnActionExpandListener(final OnActionExpandListener arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setOnMenuItemClickListener(final OnMenuItemClickListener arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setShortcut(final char arg0, final char arg1) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public void setShowAsAction(final int arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setShowAsActionFlags(final int arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setTitle(final CharSequence arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setTitle(final int arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setTitleCondensed(final CharSequence arg0) {
    throw new IllegalStateException(ERROR);
  }

  @Override
  public MenuItem setVisible(final boolean arg0) {
    throw new IllegalStateException(ERROR);
  }
}
