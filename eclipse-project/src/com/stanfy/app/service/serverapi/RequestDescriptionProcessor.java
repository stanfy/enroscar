package com.stanfy.app.service.serverapi;

import android.content.Context;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.app.Application;
import com.stanfy.app.beans.BeansManager;
import com.stanfy.serverapi.RemoteServerApiConfiguration;
import com.stanfy.serverapi.RequestMethod;
import com.stanfy.serverapi.RequestMethod.RequestMethodException;
import com.stanfy.serverapi.RequestMethod.RequestResult;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.serverapi.response.ResponseModelConverter;

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

  /** Configuration. */
  private final RemoteServerApiConfiguration config;

  public RequestDescriptionProcessor(final Application application) {
    this.config = BeansManager.get(application).getRemoteServerApiConfiguration();
  }

  /**
   * Process an incoming request description. This method is running in the worker thread.
   * @param context system context (service or application context as a rule)
   * @param description request description to be processed
   * @param hooks request processor hooks
   */
  public void process(final Context context, final RequestDescription description, final RequestProcessorHooks hooks) {
    final RequestMethod requestMethod = config.getRequestMethod(description);
    final ResponseModelConverter converter = config.getResponseModelConverter(description);

    if (DEBUG) { Log.d(TAG, "Process request id " + description.getId()); }

    hooks.beforeRequestProcessingStarted(description, requestMethod);

    try {
      // execute request method
      final RequestResult res = requestMethod.perform(context, description);

      // check for cancel
      if (description.isCanceled()) {
        hooks.onRequestCancel(description, null);
        return;
      }

      // process results
      final ResponseData<?> response
          = converter.toResponseData(description, res.getConnection(), res.getModel());

      // check for cancel
      if (description.isCanceled()) {
        hooks.onRequestCancel(description, response);
        return;
      }

      // report results
      if (response.isSuccessful()) {
        hooks.onRequestSuccess(description, response);
      } else {
        Log.e(TAG, "Server error: " + response.getErrorCode() + ", " + response.getMessage());
        hooks.onRequestError(description, response);
      }

    } catch (final RequestMethodException e) {
      Log.e(TAG, "Request method error", e);
      hooks.onRequestError(description, converter.toResponseData(description, e));
    } finally {
      hooks.afterRequestProcessingFinished(description, requestMethod);
    }
  }

}
