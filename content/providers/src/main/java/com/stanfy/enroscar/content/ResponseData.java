package com.stanfy.enroscar.content;


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
  private final T entity;

  public ResponseData(final int errorCode) {
    this(errorCode, null);
  }

  public ResponseData(final int errorCode, final String message) {
    this(errorCode, message, null);
  }

  public ResponseData(final T entity) {
    this(0, null, entity);
  }

  public ResponseData(final ResponseData<?> response, final T entity) {
    this(response.getErrorCode(), response.getMessage(), entity);
  }

  public ResponseData(final int errorCode, final String message, final T entity) {
    this.errorCode = errorCode;
    this.message = message;
    this.entity = entity;
  }

  /** @return response entity object */
  public T getEntity() {
    return entity;
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
    return "ResponseData{errorCode=" + errorCode + ", message='" + message + "', entity="
        + (entity == null ? "<null>" : entity.getClass()) + "}";
  }
}
