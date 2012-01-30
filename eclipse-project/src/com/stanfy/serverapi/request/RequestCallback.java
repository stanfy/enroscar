package com.stanfy.serverapi.request;

import java.io.Serializable;

import com.stanfy.serverapi.response.ResponseData;

/**
 * @param <T> model tyoe
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class RequestCallback<T extends Serializable> {

  /**
   * @param token token identifier
   * @param operation operation
   * @param responseData response data
   */
  public void reportError(final int token, final int operation, final ResponseData responseData) { /* empty */ }

  @SuppressWarnings("unchecked")
  public final void castAndReportSuccess(final int token, final int operation, final ResponseData responseData, final Serializable model) {
    reportSuccess(token, operation, responseData, (T)model);
  }

  /**
   * @param token token identifier
   * @param operation operation
   * @param responseData response data
   * @param model model instance
   */
  public void reportSuccess(final int token, final int operation, final ResponseData responseData, final T model) { /* empty */ }

  /**
   * @param token token identifier
   * @param operation operation
   * @param responseData response data
   * @param model model instance
   */
  public void reportSuccessUnknownModelType(final int token, final int operation, final ResponseData responseData, final Serializable model) { /* empty */ }

  /**
   * @param token token identifier
   * @param operation pending operation
   */
  public void reportPending(final int token, final int operation) { /* empty */ }

  /**
   * @param token token identifier
   * @param operation last operation
   * @param data data URI
   * @param message result message
   */
  public void reportLastOperation(final int token, final int operation, final ResponseData responseData) { /* empty */ }

  /**
   * @return model class
   */
  public Class<?> getModelClass(final int token, final int operation) { return null; }

  /**
   * If this method returns false {@link ResponseData} arguments will never contain model instance.
   * @return true if callback requires model delivering
   */
  public boolean isModelInterest() { return true; }

}
