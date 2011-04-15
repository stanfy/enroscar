package com.stanfy.forms;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
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
  private static final int DIALOG_EDITTEXT = 1;

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

  protected View addContainer(final int containerId, final int viewId) {
    final View c = owner.findViewById(containerId);
    containers[cIndex++] = c;
    c.setOnClickListener(this);
    final View result = c.findViewById(viewId);
    if (result instanceof TextView) { ((TextView)result).setFreezesText(true); }
    return result;
  }

  public Dialog onCreateDialog(final int id) {
    final Builder builder = new Builder(owner)
        .setNegativeButton(R.string.cancel, this)
        .setPositiveButton(R.string.ok, this);

    switch (id) {

    case DIALOG_EDITTEXT:
      builder.setView(LayoutInflater.from(owner).inflate(R.layout.dialog_edit_text, null));
      break;

    default:
      return null;
    }
    return builder.create();
  }

  public void onPrepareDialog(final int id, final Dialog d) {
    d.setTitle(state.currentDialogTitle);
    switch (id) {

    case DIALOG_EDITTEXT:
      final EditText edit = (EditText)d.findViewById(R.id.dialog_edit_text);
      edit.setText(state.currentDialogText);
      lastEditText = edit;
      break;

    default:
    }
  }

  protected void showEditTextDialog(final CharSequence title, final CharSequence value, final int sender) {
    state.currentDialogId = DIALOG_EDITTEXT;
    state.senderId = sender;
    state.currentDialogTitle = title;
    state.currentDialogText = value;
    owner.showDialog(DIALOG_EDITTEXT);
  }

  @Override
  public final void onClick(final DialogInterface dialog, final int which) {
    if (which == DialogInterface.BUTTON_POSITIVE) {
      switch (state.currentDialogId) {
      case DIALOG_EDITTEXT:
        if (lastEditText != null) { onDialogResult(state.senderId, lastEditText.getText().toString()); }
        break;
      default:
      }
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
      Log.d("123123", "new state " + state);
      this.state = (BaseFormLogic.StateHolder)state;
    }
  }

  protected abstract void onDialogResult(final int sender, final Object value);

  /**
   * State holder.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  private static class StateHolder {
    /** Current dialog ID. */
    int currentDialogId, senderId;
    /** Strings. */
    CharSequence currentDialogTitle, currentDialogText;
  }
}
