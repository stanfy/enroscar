package com.stanfy.app.service.serverapi;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.stanfy.DebugFlags;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.serverapi.RemoteServerApiConfiguration;
import com.stanfy.serverapi.RequestMethod;
import com.stanfy.serverapi.RequestMethod.RequestMethodException;
import com.stanfy.serverapi.RequestMethod.RequestResult;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ContentAnalyzer;
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
    this.config = BeansManager.get(application)
        .getContainer().getBean(RemoteServerApiConfiguration.BEAN_NAME, RemoteServerApiConfiguration.class);
  }

  /** @return configuration object */
  public RemoteServerApiConfiguration getConfig() { return config; }

  @SuppressWarnings({ "unchecked", "rawtypes" })
  private static ResponseData<?> analyze(final Context context, final ContentAnalyzer analyzer, final ResponseData<?> responseData,
      final RequestDescription description) throws RequestMethodException {
    final ResponseData data = responseData;
    return analyzer.analyze(context, description, data);
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

    ContentAnalyzer<?, ?> analyzer = null;

    final String analyzerBeanName = description.getContentAnalyzer();
    if (analyzerBeanName != null) {
      analyzer = BeansManager.get(context).getContainer().getBean(analyzerBeanName, ContentAnalyzer.class);
      if (analyzer == null) {
        throw new RuntimeException("ContentAnalyzer bean with name " + analyzerBeanName + " is not declared.");
      }
    }

    if (DEBUG) { Log.d(TAG, "Process request id " + description.getId()); }

    hooks.beforeRequestProcessingStarted(description, requestMethod);

    boolean passedToAnalyzer = false;

    try {
      // execute request method
      final RequestResult res = requestMethod.perform(context, description);

      // check for cancel
      if (description.isCanceled()) {
        hooks.onRequestCancel(description, null);
        return;
      }

      // process results
      ResponseData<?> response
          = converter.toResponseData(description, res.getConnection(), res.getModel());

      // check for cancel
      if (description.isCanceled()) {
        hooks.onRequestCancel(description, response);
        return;
      }

      // analyze
      passedToAnalyzer = true;
      if (analyzer != null) {
        response = analyze(context, analyzer, response, description);
      }

      // report results
      if (response.isSuccessful()) {
        hooks.onRequestSuccess(description, response);
      } else {
        Log.e(TAG, "Server error: " + response.getErrorCode() + ", " + response.getMessage());
        hooks.onRequestError(description, response);
      }

    } catch (final RequestMethodException e) {

      Log.e(TAG, "Request method error while processing " + description, e);
      ResponseData<?> data = converter.toResponseData(description, e);

      if (analyzer != null && !passedToAnalyzer) {
        try {
          data = analyze(context, analyzer, data, description);
        } catch (RequestMethodException analyzerException) {
          Log.e(TAG, "Analyzer exception analyzerName=" + analyzerBeanName + " for " + description, analyzerException);
          // repack data to use the current exception
          data = converter.toResponseData(description, analyzerException);
        }
      }

      hooks.onRequestError(description, data);

    } finally {
      hooks.afterRequestProcessingFinished(description, requestMethod);
    }
  }

}
