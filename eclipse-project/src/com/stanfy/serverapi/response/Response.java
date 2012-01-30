package com.stanfy.serverapi.response;

import android.content.Context;

import com.stanfy.views.R;

/**
 * Response.
 * @author Roman Mazur (mailto: mazur.roman@gmail.com)
 */
public class Response {

  /** Success code. */
  public static final int SUCCESS_CODE = 0;

  /** Error code. */
  private int errorCode;
  /** Message. */
  private String message;

  /** @return whether result is successful */
  public boolean isSuccessful() { return errorCode == SUCCESS_CODE; }

  /**
   * @return whether error message can be shown to user
   */
  public boolean isHumanError() { return false; }

  public String getMessage() { return message; }
  public void setMessage(final String message) { this.message = message; }

  public int getErrorCode() { return errorCode; }
  public void setErrorCode(final int errorCode) { this.errorCode = errorCode; }

  public String resolveUserErrorMessage(final Context context) {
    return isHumanError() ? message : getDefaultErrorMessage(context);
  }
  public String getDefaultErrorMessage(final Context context) {
    return context.getString(R.string.error_server_default);
  }

}
