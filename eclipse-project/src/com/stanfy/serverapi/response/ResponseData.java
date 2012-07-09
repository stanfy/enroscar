package com.stanfy.serverapi.response;

import android.util.Log;

import com.stanfy.serverapi.ErrorCodes;


/**
 * Response data that is passed to service callbacks.
 * @param <T> model type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ResponseData<T>  {

  /** Error code. */
  private int errorCode;
  /** Message. */
  private String message;
  /** Model. */
  private T model;

  public ResponseData() {
    // nothing
  }
  public ResponseData(final T model) {
    this.model = model;
  }

  /** @return serializable model */
  public T getModel() { return model; }
  /** @return the errorCode */
  public int getErrorCode() { return errorCode; }
  /** @return the message */
  public String getMessage() { return message; }

  /** @param errorCode the errorCode to set */
  public void setErrorCode(final int errorCode) {
    this.errorCode = errorCode;
    if (errorCode == ErrorCodes.ERROR_CODE_SERVER_COMUNICATION) {
      Log.e("123123", "aaaaaaa", new Throwable());
    }
  }
  /** @param message the message to set */
  public void setMessage(final String message) { this.message = message; }
  /** @param model the model to set */
  public void setModel(final T model) { this.model = model; }

  /** @return success flag */
  public boolean isSuccessful() { return errorCode == 0; }

}
