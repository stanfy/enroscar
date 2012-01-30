package com.stanfy.serverapi.response;

import java.io.Serializable;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.Destroyable;
import com.stanfy.serverapi.ErrorCodes;
import com.stanfy.serverapi.RequestMethod.RequestMethodException;


/**
 * Parser context.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ParserContext implements Destroyable {

  /** Logging tag. */
  public static final String TAG = "ParserContext";

  /** Debug flag. */
  protected static final boolean DEBUG = DebugFlags.DEBUG_API;

  /** System context. */
  private Context systemContext;

  /** Response. */
  private Response response;

  /** Simple result handler instance. */
  private SimpleResultHandler simpleResultHandler;

  /** @param systemContext the systemContext to set */
  public void setSystemContext(final Context systemContext) { this.systemContext = systemContext; }

  public void defineResponse(final Response response) {
    this.response = response;
  }
  public void defineResponse(final RequestMethodException exception) {
    this.response = createErrorResponse(exception);
    response.setErrorCode(exception.isConnectionError() ? ErrorCodes.ERROR_CODE_CONNECTION : ErrorCodes.ERROR_CODE_SERVER_COMUNICATION);
    response.setMessage(exception.getMessage());
  }

  protected Response createErrorResponse(final RequestMethodException exception) {
    return new Response();
  }

  protected void defineSimpleResultHandler(final SimpleResultHandler simpleResultHandler) {
    this.simpleResultHandler = simpleResultHandler;
  }

  public void postData(final Object data) { /* nothing */ }

  /** @return response instance */
  public Response getResponse() { return response; }

  /** @return the simpleResultHandler */
  public SimpleResultHandler getSimpleResultHandler() { return simpleResultHandler; }

  protected ResponseData createResponseData(final Uri data) { return new ResponseData(data, getModel()); }

  public final boolean isSuccessful() { return response != null && response.isSuccessful(); }

  /** @return URI to access the processed results */
  public final ResponseData processResults() {
    Response response = getResponse();

    if (response == null) { // server communication error
      if (DEBUG) { Log.d(TAG, "Response is null!"); }
      defineResponse(new RequestMethodException("Response is null"));
      response = getResponse();
    }

    final ResponseData responseData;
    if (response.isSuccessful()) {
      responseData = createResponseData(callAnalyzer());
    } else {
      responseData = createResponseData(null);
    }
    responseData.setResponse(response, systemContext);

    return responseData;
  }

  protected Uri callAnalyzer() {
    final ContextAnalyzer<ParserContext> analyzer = getAnalyzer();
    return analyzer != null ? analyzer.analyze(this, systemContext) : null;
  }

  /** @return context model instance */
  public Serializable getModel() { return null; }

  /** @return context analyzer instance */
  protected <T extends ParserContext> ContextAnalyzer<T> getAnalyzer() { return null; }

  @Override
  public void destroy() {
    systemContext = null;
    response = null;
    simpleResultHandler = null;
  }

  /**
   * @author Roman Mazur - Stanfy (http://www.stanfy.com)
   */
  public static class SimpleResultHandler {
    public void handleValue(final String name, final String value) { /* empty */ }
  }

}
