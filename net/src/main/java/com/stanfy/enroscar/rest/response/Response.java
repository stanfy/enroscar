package com.stanfy.enroscar.rest.response;

import android.content.Context;

import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.rest.RemoteServerApiConfiguration;

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

  public final String getMessage() { return message; }
  public final void setMessage(final String message) { this.message = message; }

  public final int getErrorCode() { return errorCode; }
  public final void setErrorCode(final int errorCode) { this.errorCode = errorCode; }

  public String resolveUserErrorMessage(final Context context) {
    return isHumanError() ? message : getDefaultErrorMessage(context);
  }
  public String getDefaultErrorMessage(final Context context) {
    RemoteServerApiConfiguration conf = BeansManager.get(context).getContainer().getBean(RemoteServerApiConfiguration.BEAN_NAME, RemoteServerApiConfiguration.class);
    String defMessage = conf.getDefaultErrorMessage();
    return defMessage != null ? defMessage : message;
  }

}
