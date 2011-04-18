package com.stanfy.forms;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.stanfy.Destroyable;
import com.stanfy.views.R;

/**
 * Base step logic.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class BaseFormLogic implements OnClickListener, Destroyable, DialogInterface.OnClickListener {

  /** Dialog IDs. */
  private static final int DIALOG_EDITTEXT = 1, DIALOG_EDITTEXT_LARGE = 2;

  /** Owner. */
  private Activity owner;

  /** Containers. */
  private final View[] containers;

  /** Last edit text. */
  private EditText lastEditText;

  /** Container index. */
  private int cIndex = 0;

  /** State. */
  private BaseFormLogic.StateHolder state;

  protected BaseFormLogic(final int numberOfContainers, final Activity owner) {
    this.owner = owner;
    this.state = new StateHolder();
    containers = new View[numberOfContainers];
  }

  protected View addContainer(final int containerId, final int viewId, final boolean addColon, final boolean addStar) {
    final View c = owner.findViewById(containerId);
    containers[cIndex++] = c;
    c.setOnClickListener(this);
    final View result = c.findViewById(viewId);
    if (result instanceof TextView) { ((TextView)result).setFreezesText(true); }
    if (addColon || addStar && c instanceof ViewGroup) {
      final ViewGroup g = (ViewGroup)c;
      if (g.getChildCount() > 0) {
        final View first = g.getChildAt(0);
        if (first instanceof TextView) {
          final TextView tv = (TextView)first;
          if (addColon) { tv.setText(tv.getText() + ":"); }
          if (addStar) { tv.setText(tv.getText() + " *"); }
        }
      }
    }
    return result;
  }

  protected View addContainer(final int containerId, final int viewId) {
    return addContainer(containerId, viewId, true, false);
  }

  protected final Builder initAlertBuilder() {
    return new Builder(owner)
      .setTitle(state.currentDialogTitle);
  }

  public Dialog onCreateDialog(final int id) {
    final Builder builder = initAlertBuilder();

    switch (id) {

    case DIALOG_EDITTEXT:
      builder
        .setNegativeButton(R.string.cancel, this)
        .setPositiveButton(R.string.ok, this)
        .setView(LayoutInflater.from(owner).inflate(R.layout.dialog_edit_text, null));
      break;

    case DIALOG_EDITTEXT_LARGE:
      builder
      .setNegativeButton(R.string.cancel, this)
      .setPositiveButton(R.string.ok, this)
      .setView(LayoutInflater.from(owner).inflate(R.layout.dialog_edit_text_large, null));
      break;

    default:
      return null;
    }
    return builder.create();
  }

  public void onPrepareDialog(final int id, final Dialog d) {
    d.setTitle(state.currentDialogTitle);
    switch (id) {

    case DIALOG_EDITTEXT_LARGE:
    case DIALOG_EDITTEXT:
      final EditText edit = (EditText)d.findViewById(R.id.dialog_edit_text);
      edit.setText(state.currentDialogText);
      lastEditText = edit;
      break;

    default:
    }
  }

  protected void showEditTextDialog(final CharSequence title, final CharSequence value, final int sender) {
    showDialog(DIALOG_EDITTEXT, title, value, sender);
  }
  protected void showEditTextDialogLarge(final CharSequence title, final CharSequence value, final int sender) {
    showDialog(DIALOG_EDITTEXT_LARGE, title, value, sender);
  }

  protected void showDialog(final int id, final CharSequence title, final CharSequence text, final int sender) {
    state.currentDialogId = id;
    state.senderId = sender;
    state.currentDialogTitle = title;
    state.currentDialogText = text;
    owner.showDialog(id);
  }

  @Override
  public final void onClick(final DialogInterface dialog, final int which) {
    if (which == DialogInterface.BUTTON_POSITIVE) {
      onDialogOk(state.currentDialogId, state.senderId, dialog);
    }
  }

  protected void onDialogOk(final int dialogId, final int senderId, final DialogInterface dialog) {
    switch (dialogId) {
    case DIALOG_EDITTEXT_LARGE:
    case DIALOG_EDITTEXT:
      if (lastEditText != null) { onDialogResult(senderId, lastEditText.getText().toString()); }
      break;
    default:
    }
  }

  @Override
  public void destroy() {
    lastEditText = null;
    for (int i = containers.length - 1; i >= 0; i--) {
      final View v = containers[i];
      if (v != null) {
        v.setOnClickListener(null);
        containers[i] = null;
      }
    }
  }

  public Object getState() { return state; }
  public void setState(final Object state) {
    if (state instanceof BaseFormLogic.StateHolder) {
      this.state = (BaseFormLogic.StateHolder)state;
    }
  }

  protected abstract void onDialogResult(final int sender, final Object value);

  /**
   * State holder.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  protected static class StateHolder {
    /** Current dialog ID. */
    int currentDialogId, senderId;
    /** Strings. */
    CharSequence currentDialogTitle, currentDialogText;
  }
}
