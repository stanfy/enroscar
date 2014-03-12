package com.stanfy.enroscar.content.loader;


/**
 * Response received as a result of fetching some remote data. User can check
 * whether fetching operations has been successful with {@link #isSuccessful()} method.
 * Response contains error (or result) code, text message, and result object.
 * Zero error code indicates successful result.
 *
 * @param <T> result object type
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ResponseData<T>  {

  /** Error code. */
  private final int errorCode;
  /** Message. */
  private final String message;
  /** Result object. */
  private final T model;

  public ResponseData(final int errorCode) {
    this(errorCode, null);
  }

  public ResponseData(final int errorCode, final String message) {
    this(errorCode, message, null);
  }

  public ResponseData(final T model) {
    this(0, null, model);
  }

  public ResponseData(final ResponseData<?> response, final T model) {
    this(response.getErrorCode(), response.getMessage(), model);
  }

  public ResponseData(final int errorCode, final String message, final T model) {
    this.errorCode = errorCode;
    this.message = message;
    this.model = model;
  }

  /** @return response model object */
  public T getModel() {
    return model;
  }

  /** @return the errorCode */
  public int getErrorCode() {
    return errorCode;
  }

  /** @return the message */
  public String getMessage() {
    return message;
  }

  /** @return success result flag */
  public boolean isSuccessful() {
    return errorCode == 0;
  }

  @Override
  public String toString() {
    return "ResponseData{errorCode=" + errorCode + ", message='" + message + "', model="
        + (model == null ? "<null>" : model.getClass()) + "}";
  }
}
