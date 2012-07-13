package com.stanfy.serverapi.response;



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

  public ResponseData(final ResponseData<?> response) {
    this.errorCode = response.errorCode;
    this.message = response.message;
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
  }
  /** @param message the message to set */
  public void setMessage(final String message) { this.message = message; }
  /** @param model the model to set */
  public void setModel(final T model) { this.model = model; }

  /** @return success flag */
  public boolean isSuccessful() { return errorCode == 0; }

}
