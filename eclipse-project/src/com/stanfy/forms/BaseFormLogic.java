package com.stanfy.forms;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import com.stanfy.Destroyable;
import com.stanfy.views.R;
import com.stanfy.views.utils.AppUtils;

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

  /** Container index. */
  private int cIndex = 0;

  /** Lines count. */
  private final int linesCountForLargeText;

  /** State. */
  private BaseFormLogic.StateHolder state;

  protected BaseFormLogic(final int numberOfContainers, final Activity owner) {
    this.owner = owner;
    this.state = new StateHolder();
    containers = new View[numberOfContainers];

    linesCountForLargeText = owner.getResources().getInteger(R.integer.form_dialog_large_text_lines);
  }

  protected String addStar(final CharSequence in) { return "*" + in; }

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
          if (addStar) { tv.setText(addStar(tv.getText())); }
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
      final View v = LayoutInflater.from(owner).inflate(R.layout.dialog_edit_text_large, null);
      builder
          .setNegativeButton(R.string.cancel, this)
          .setPositiveButton(R.string.ok, this)
          .setView(v);
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
      final EditText edit = retrieveDialogEdit(d, id);
      edit.setText(state.currentDialogText);
      edit.setInputType(state.inputType);
      if (id == DIALOG_EDITTEXT_LARGE) {
        edit.setLines(linesCountForLargeText);
      } else {
        edit.setImeOptions(edit.getImeOptions() | EditorInfo.IME_ACTION_DONE);
        edit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
          
          @Override
          public boolean onEditorAction(final TextView v, final int actionId, final KeyEvent event) {
            if (d instanceof AlertDialog
                && (actionId == EditorInfo.IME_ACTION_DONE
                    || (actionId == EditorInfo.IME_ACTION_UNSPECIFIED
                        && event != null
                        && event.getAction() == KeyEvent.ACTION_DOWN
                        && event.getKeyCode() == KeyEvent.KEYCODE_ENTER))) {
              ((AlertDialog)d).getButton(AlertDialog.BUTTON_POSITIVE).performClick();
              return true;
            }
            return false;
          }
        });
      }
      break;

    default:
    }
  }

  private static EditText retrieveDialogEdit(final Dialog d, final int dialogType) {
    return (EditText)d.findViewById(dialogType == DIALOG_EDITTEXT ? R.id.dialog_edit_text : R.id.dialog_edit_text_large);
  }

  protected void showEditTextDialog(final CharSequence title, final CharSequence value, final int sender) {
    showEditTextDialog(title, value, sender, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
  }
  protected void showEditTextDialogLarge(final CharSequence title, final CharSequence value, final int sender) {
    showEditTextDialogLarge(title, value, sender, InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_SENTENCES);
  }
  protected void showEditTextDialog(final CharSequence title, final CharSequence value, final int sender, final int inputType) {
    showDialog(DIALOG_EDITTEXT, title, value, sender, inputType);
  }
  protected void showEditTextDialogLarge(final CharSequence title, final CharSequence value, final int sender, final int inputType) {
    showDialog(DIALOG_EDITTEXT_LARGE, title, value, sender, inputType | InputType.TYPE_TEXT_FLAG_MULTI_LINE);
  }

  protected void showDialog(final int id, final CharSequence title, final CharSequence text, final int sender) {
    showDialog(id, title, text, sender, InputType.TYPE_CLASS_TEXT);
  }
  protected void showDialog(final int id, final CharSequence title, final CharSequence text, final int sender, final int inputType) {
    state.currentDialogId = id;
    state.senderId = sender;
    state.currentDialogTitle = title;
    state.currentDialogText = text;
    state.inputType = inputType;
    owner.showDialog(id);
  }
  
  @Override
  public final void onClick(final DialogInterface dialog, final int which) {
    final EditText edit;
    final int dialogType = state.currentDialogId;
    switch (dialogType) {
      case DIALOG_EDITTEXT_LARGE:
      case DIALOG_EDITTEXT:
        edit = retrieveDialogEdit((Dialog)dialog, dialogType);
        AppUtils.hideSoftInput(edit);
        break;
      default:
        edit = null;
    }
    if (which == DialogInterface.BUTTON_POSITIVE) {
      onDialogOk(state.currentDialogId, state.senderId, dialog, edit);
    }
  }

  private void onDialogOk(final int dialogId, final int senderId, final DialogInterface dialog, final EditText edit) {
    switch (dialogId) {
    case DIALOG_EDITTEXT_LARGE:
    case DIALOG_EDITTEXT:
      if (edit != null) {
        final String value = edit.getText().toString();
        edit.setText(null);
        onDialogResult(senderId, value);
      }
      break;
    default:
      onDialogOk(dialogId, senderId, dialog);
    }
  }

  protected void onDialogOk(final int dialogId, final int senderId, final DialogInterface dialog) { /* nothing */ }

  @Override
  public void destroy() {
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
    /** Input type. */
    int inputType;
    /** Strings. */
    CharSequence currentDialogTitle, currentDialogText;
  }
}
