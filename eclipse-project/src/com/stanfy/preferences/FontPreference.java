package com.stanfy.preferences;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

import com.stanfy.views.R;

/**
 * A preference to configure the font size.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class FontPreference extends DialogPreference {

  /** Default text. */
  public static final String DEFAULT_TEXT = "Aa";

  /** Default text size range. */
  public static final int DEFAULT_FROM_SIZE = 10, DEFAULT_TO_SIZE = 24, DEFAULT_VALUE = 16;

  /** Test text. */
  private CharSequence testText;

  /** Range of sizes. */
  private int fromSize, toSize;

  /** Value. */
  private int value, selectedValue;

  public FontPreference(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init(context, attrs, 0);
  }
  public FontPreference(final Context context, final AttributeSet attrs, final int defStyle) {
    super(context, attrs, defStyle);
    init(context, attrs, defStyle);
  }

  private void init(final Context context, final AttributeSet attrs, final int defStyle) {
    final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.FontPreference, defStyle, 0);

    testText = a.getString(R.styleable.FontPreference_testText);
    if (testText == null) { testText = DEFAULT_TEXT; }
    fromSize = a.getInt(R.styleable.FontPreference_fromSize, DEFAULT_FROM_SIZE);
    toSize = a.getInt(R.styleable.FontPreference_toSize, DEFAULT_TO_SIZE);

    a.recycle();
  }

  public void setTestText(final CharSequence testText) { this.testText = testText; }
  public void setFromSize(final int fromSize) { this.fromSize = fromSize; }
  public void setToSize(final int toSize) { this.toSize = toSize; }

  /** @return font size value */
  public int getValue() {
    if (value == 0) {
      value = getPersistedInt(DEFAULT_VALUE);
    }
    return value;
  }

  /**
   * @param value font size to set
   */
  public void setValue(final int value) {
    this.value = value;

    persistInt(value);
  }

  @Override
  public CharSequence getSummary() {
    final int v = getValue();
    final CharSequence summary = super.getSummary();
    if (summary == null || v == 0) { return summary; }
    return String.format(summary.toString(), v);
  }

  private void setText(final TextView text, final int size) {
    text.setText(testText + " " + size);
    text.setTextSize(size);
  }

  @Override
  protected void onPrepareDialogBuilder(final Builder builder) {
    super.onPrepareDialogBuilder(builder);
    selectedValue = getValue();

    final LayoutInflater inflater = LayoutInflater.from(getContext());
    final View view = inflater.inflate(R.layout.font_preference, null);
    final TextView text = (TextView)view.findViewById(android.R.id.text1);
    if (text != null) {
      setText(text, selectedValue);
    }
    final SeekBar bar = (SeekBar)view.findViewById(android.R.id.content);
    if (bar != null) {
      bar.setProgress(selectedValue - fromSize);
      bar.setMax(toSize - fromSize);
      bar.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {
        @Override
        public void onStopTrackingTouch(final SeekBar seekBar) { /* nothing */ }
        @Override
        public void onStartTrackingTouch(final SeekBar seekBar) { /* nothing */ }
        @Override
        public void onProgressChanged(final SeekBar seekBar, final int progress, final boolean fromUser) {
          selectedValue = progress + fromSize;
          if (text != null) { setText(text, selectedValue); }
        }
      });
    }

    builder
      .setView(view)
      .setNeutralButton(R.string.reset, new OnClickListener() {
        @Override
        public void onClick(final DialogInterface dialog, final int which) {
          if (bar != null) { bar.setProgress(DEFAULT_VALUE - fromSize); }
          storeSelected();
        }
      });
  }

  private void storeSelected() {
    if (callChangeListener(selectedValue)) {
      setValue(selectedValue);
    }
  }

  @Override
  protected void onDialogClosed(final boolean positiveResult) {
    super.onDialogClosed(positiveResult);
    if (positiveResult) { storeSelected(); }
  }

  @Override
  protected Object onGetDefaultValue(final TypedArray a, final int index) {
    return a.getInt(index, DEFAULT_VALUE);
  }
  @Override
  protected void onSetInitialValue(final boolean restorePersistedValue, final Object defaultValue) {
    setValue(restorePersistedValue ? getPersistedInt(DEFAULT_VALUE) : (Integer)defaultValue);
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    final Parcelable superState = super.onSaveInstanceState();
    if (isPersistent()) { return superState; }
    final SavedState state = new SavedState(superState);
    state.value = getValue();
    return state;
  }

  @Override
  protected void onRestoreInstanceState(final Parcelable state) {
    if (state == null || !state.getClass().equals(SavedState.class)) {
      super.onRestoreInstanceState(state);
      return;
    }
    final SavedState myState = (SavedState)state;
    super.onRestoreInstanceState(myState.getSuperState());
    setValue(myState.value);
  }

  /**
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  static class SavedState extends BaseSavedState {

    /** Creator. */
    public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
      @Override
      public SavedState createFromParcel(final Parcel source) { return new SavedState(source); }
      @Override
      public SavedState[] newArray(final int size) { return new SavedState[size]; }
    };

    /** Stored value. */
    int value;

    public SavedState(final Parcel source) {
      super(source);
      value = source.readInt();
    }

    public SavedState(final Parcelable superState) {
      super(superState);
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
      super.writeToParcel(dest, flags);
      dest.writeInt(value);
    }

  }

}
