package com.stanfy.app.service.serverapi;

import android.content.Context;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.app.Application;
import com.stanfy.serverapi.RequestMethod;
import com.stanfy.serverapi.RequestMethod.RequestMethodException;
import com.stanfy.serverapi.RequestMethodHelper;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ParserContext;
import com.stanfy.serverapi.response.ResponseData;

/**
 * A strategy that creates {@link com.stanfy.serverapi.response.ParserContext} and calls
 * {@link com.stanfy.serverapi.RequestMethod}.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
public class RequestDescriptionProcessor {

  /** Logging tag. */
  protected static final String TAG = "RDProcessor";
  /** Debug API. */
  protected static final boolean DEBUG = DebugFlags.DEBUG_API;

  /** Application instance. */
  private final Application application;

  public RequestDescriptionProcessor(final Application application) {
    this.application = application;
  }

  /**
   * Process an incoming request description. This method is running in the worker thread.
   * @param context system context (service or application context as a rule)
   * @param description request description to be processed
   * @param hooks request processor hooks
   */
  public void process(final Context context, final RequestDescription description, final RequestProcessorHooks hooks) {
    final Application app = this.application;

    final RequestMethodHelper requestMethodHelper = app.getRequestMethodHelper();

    final ParserContext pContext = requestMethodHelper.createParserContext(description);
    pContext.setSystemContext(context);
    final RequestMethod requestMethod = requestMethodHelper.createRequestMethod(description);

    final int opCode = description.getOperationCode();
    final int token = description.getToken();
    if (DEBUG) { Log.d(TAG, "Current context: " + pContext + ", op " + opCode + ", token " + token); }

    hooks.beforeRequestProcessingStarted(description, pContext, requestMethod);

    try {
      // execute request method
      requestMethod.setup(app);
      requestMethod.start(context, description, pContext);
      requestMethod.stop(app);

      // process results
      final ResponseData response = pContext.processResults();

      // report results
      if (pContext.isSuccessful()) {
        hooks.onRequestSuccess(description, response);
      } else {
        Log.e(TAG, "Server error: " + response.getErrorCode() + ", " + response.getMessage());
        hooks.onRequestError(description, response);
      }

    } catch (final RequestMethodException e) {
      Log.e(TAG, "Request method error", e);
      pContext.defineResponse(e);
      hooks.onRequestError(description, pContext.processResults());
    } finally {
      hooks.afterRequestProcessingFinished(description, pContext, requestMethod);
      pContext.destroy();
    }
  }

  /**
   * Request description processing hooks.
   * @author Roman Mazur (Stanfy - http://stanfy.com)
   */
  public interface RequestProcessorHooks {
    /**
     * Called before the {@link RequestMethod} methods are invoked.
     * @param requestDescription request description to be processed
     * @param pContext parser context instance
     * @param requestMethod request method created for processing this request description
     */
    void beforeRequestProcessingStarted(final RequestDescription requestDescription, final ParserContext pContext, final RequestMethod requestMethod);
    /**
     * Called after the {@link RequestMethod} methods are invoked.
     * @param requestDescription request description to be processed
     * @param pContext parser context instance
     * @param requestMethod request method created for processing this request description
     */
    void afterRequestProcessingFinished(final RequestDescription requestDescription, final ParserContext pContext, final RequestMethod requestMethod);

    /**
     * Called when success response is retrieved.
     * @param requestDescription request description instance
     * @param responseData response data instance
     */
    void onRequestSuccess(final RequestDescription requestDescription, final ResponseData responseData);
    /**
     * Called when error response is retrieved.
     * @param requestDescription request description instance
     * @param responseData response data instance
     */
    void onRequestError(final RequestDescription requestDescription, final ResponseData responseData);
  }

}
