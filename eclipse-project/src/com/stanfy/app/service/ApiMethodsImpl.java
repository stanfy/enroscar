package com.stanfy.app.service;

import static com.stanfy.app.service.ApplicationService.DEBUG;

import java.util.concurrent.atomic.AtomicBoolean;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.util.Log;

import com.stanfy.Destroyable;
import com.stanfy.app.Application;
import com.stanfy.app.service.ApiMethods.Stub;
import com.stanfy.serverapi.RequestMethod;
import com.stanfy.serverapi.RequestMethod.RequestMethodException;
import com.stanfy.serverapi.RequestMethodHelper;
import com.stanfy.serverapi.request.Operation;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.ParserContext;
import com.stanfy.serverapi.response.RequestConfigurableContext;
import com.stanfy.serverapi.response.ResponseData;
import com.stanfy.views.R;

/**
 * Implementation for {@link ApiMethods}.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
public class ApiMethodsImpl extends Stub implements Destroyable {

  /** Logging tag. */
  static final String TAG = "ApiMethodsImpl";

  /** Null operation data. */
  private static final APICallInfoData NULL_OPERATION_DATA = new APICallInfoData();

  /** Calls {@link ApiMethodCallback#reportSuccess(int, int, String, Uri, ResponseData)}. */
  private static final CallbackReporter SUCCESS_REPORTER = new CallbackReporter("success") {
    @Override
    void report(final ApiMethodCallback callback, final int token, final int operation, final ResponseData responseData) throws RemoteException {
      callback.reportSuccess(token, operation, responseData);
    }
  };
  /** Calls {@link ApiMethodCallback#reportSuccess(int, int, String, Uri, ResponseData)}. */
  private static final CallbackReporter ERROR_REPORTER = new CallbackReporter("error") {
    @Override
    void report(final ApiMethodCallback callback, final int token, final int operation, final ResponseData responseData) throws RemoteException {
      callback.reportError(token, operation, responseData);
    }
  };

  /** Message code. */
  private static final int MSG_REQUEST = 0, MSG_FINISH = 1;

  /** API callbacks. */
  private final RemoteCallbackList<ApiMethodCallback> apiCallbacks = new RemoteCallbackList<ApiMethodCallback>();

  /** Application service. */
  private ApplicationService appService;

  /** Working flag. */
  private AtomicBoolean workingFlag = new AtomicBoolean(false);

  /** Main worker. */
  private HandlerThread mainWorker;
  /** Handler instance for main worker. */
  private ApiMethodsHandler mainHandler;

  /** Last operation dump. */
  private SharedPreferences lastOperationDump;

  /** Operations info. */
  final APICallInfoData pending = new APICallInfoData(), lastOperation = new APICallInfoData();

  /** Default error message. */
  final String defaultErrorMessage;

  /** Special handler. */
  class ApiMethodsHandler extends Handler {
    public ApiMethodsHandler(final Looper looper) {
      super(looper);
    }
    @Override
    public void handleMessage(final Message msg) {
      final ApplicationService appService = ApiMethodsImpl.this.appService;
      if (appService == null) { return; } // we are destroyed

      if (msg.what == MSG_FINISH) {
        workingFlag.set(false);
        appService.checkForStop();
        return;
      }
      workingFlag.set(true);

      final Application app = appService.getApp();
      final RequestMethodHelper h = app.getRequestMethodHelper();

      final RequestDescription description = (RequestDescription)msg.obj;

      final ParserContext pContext = h.createParserContext(description);
      pContext.setSystemContext(appService);
      final int opCode = description.getOperationCode();
      if (DEBUG) { Log.d(TAG, "Current context: " + pContext + ", op " + opCode); }
      final int token = description.getToken();

      before(description, pContext, appService);

      pending.set(NULL_OPERATION_DATA);
      pending.set(opCode, token);
      try {
        // execute request method
        final RequestMethod rm = h.createRequestMethod(description);
        rm.setup(app);
        rm.start(appService, description, pContext);
        rm.stop(app);

        // process results
        final ResponseData response = pContext.processResults();

        // report results
        if (pContext.isSuccessful()) {
          reportApiSuccess(token, opCode, response);
        } else {
          Log.e(TAG, "Server error: " + response.getErrorCode() + ", " + response.getMessage());
          reportError(token, opCode, response);
        }

      } catch (final RequestMethodException e) {
        Log.e(TAG, "Request method error", e);
        pContext.defineResponse(e);
        reportError(token, opCode, pContext.processResults());
      } finally {
        after(description, pContext, appService);
        dumpLastOperation(lastOperation);
        pending.set(NULL_OPERATION_DATA);
        pContext.destroy();
      }
    }
  }

  public ApiMethodsImpl(final ApplicationService appService) {
    this.appService = appService;

    this.lastOperationDump = appService.getSharedPreferences("last-operation", Context.MODE_PRIVATE);
    loadLastOperation();

    defaultErrorMessage = appService.getString(R.string.error_server_default);
    mainWorker = new HandlerThread("api-thread");
    mainWorker.start();
    mainHandler = new ApiMethodsHandler(mainWorker.getLooper());
    if (DEBUG) { Log.d(TAG, "Worker thread is now alive " + this); }
  }

  protected void before(final RequestDescription rd, final ParserContext context, final ApplicationService service) {
    if (context instanceof RequestConfigurableContext) {
      ((RequestConfigurableContext) context).configureContext(rd, service);
    }
  }
  protected void after(final RequestDescription rd, final ParserContext context, final ApplicationService service) {
    /* nothing */
  }

  private void dumpLastOperation(final APICallInfoData data) {
    if (DEBUG) { Log.d(TAG, "Dump " + data.operation); }
    final Editor lastOperationEditor = lastOperationDump.edit();
    lastOperationEditor
        .putInt("op", data.operation)
        .putInt("token", data.token);
    final ResponseData rd = data.responseData;
    if (rd != null) {
      final Uri dataUri = rd.getData();
      lastOperationEditor
        .putString("msg", rd.getMessage())
        .putInt("errorCode", rd.getErrorCode())
        .putString("data", dataUri != null ? dataUri.toString() : null);
    }
    lastOperationEditor.commit();
  }

  private void loadLastOperation() {
    final SharedPreferences src = lastOperationDump;
    final APICallInfoData dst = lastOperation;
    dst.set(src.getInt("op", Operation.NOP), src.getInt("token", -1));
    final ResponseData responseData = new ResponseData();
    responseData.setMessage(src.getString("msg", null));
    responseData.setErrorCode(src.getInt("errorCode", ResponseData.RESPONSE_CODE_ILLEGAL));
    final String url = src.getString("data", null);
    responseData.setData(url != null ? Uri.parse(url) : null);
    dst.set(responseData);
    if (DEBUG) { Log.d(TAG, "Loaded last operation: " + dst.operation + " / " + dst.responseData.getErrorCode() + " -> " + dst.hasData()); }
  }

  private void updateLastOperation(final ResponseData rd) {
    final APICallInfoData lastOperation = this.lastOperation;
    lastOperation.set(pending);
    lastOperation.set(rd);
  }

  private void reportToCallbacks(final int token, final int opCode, final ResponseData responseData, final CallbackReporter reporter) {
    updateLastOperation(responseData);
    if (DEBUG) { Log.v(TAG, "Start broadcast"); }
    int c = apiCallbacks.beginBroadcast();
    ResponseData noModelData = null;
    while (c > 0) {
      --c;
      try {
        final ApiMethodCallback callback = apiCallbacks.getBroadcastItem(c);
        if (DEBUG) { Log.d(TAG, "Report API " + reporter.name + "/op=" + opCode + "/token=" + token + " " + c + ": " + callback); }
        final boolean requiresModel = (Boolean)apiCallbacks.getBroadcastCookie(c);
        ResponseData sendingData = responseData;
        if (!requiresModel) {
          if (noModelData == null) { noModelData = ResponseData.withoutModel(responseData); }
          sendingData = noModelData;
        }
        reporter.report(callback, token, opCode, sendingData);
      } catch (final RemoteException e) {
        Log.e(TAG, "Cannot run callback report method", e);
      }
    }
    apiCallbacks.finishBroadcast();
    if (DEBUG) { Log.v(TAG, "Finish broadcast"); }
  }

  void reportApiSuccess(final int token, final int opCode, final ResponseData responseData) {
    reportToCallbacks(token, opCode, responseData, SUCCESS_REPORTER);
  }

  void reportError(final int token, final int opCode, final ResponseData responseData) {
    reportToCallbacks(token, opCode, responseData, ERROR_REPORTER);
  }

  @Override
  public void performRequest(final RequestDescription description) throws RemoteException {
    final Handler handler = this.mainHandler;
    if (handler == null) { return; }
    if (DEBUG) { Log.d(TAG, "Perform " + description + " " + this); }
    handler.removeMessages(MSG_FINISH);
    handler.sendMessage(handler.obtainMessage(MSG_REQUEST, description));
    handler.sendEmptyMessage(MSG_FINISH);
  }

  @Override
  public void registerCallback(final ApiMethodCallback callback, final boolean requiresModel) throws RemoteException {
    if (DEBUG) { Log.d(TAG, "Register API callback " + callback); }
    final APICallInfoData b = new APICallInfoData();
    b.set(lastOperation);
    if (b.hasData()) {
      if (DEBUG) { Log.d(TAG, "Report last operation " + b.operation); }
      callback.reportLastOperation(b.token, b.operation, b.responseData);
    }
    b.set(pending);
    if (b.hasData()) { callback.reportPending(b.token, b.operation); }
    apiCallbacks.register(callback, requiresModel);
  }

  @Override
  public void removeCallback(final ApiMethodCallback callback) throws RemoteException {
    if (DEBUG) { Log.d(TAG, "Remove API callback " + callback); }
    apiCallbacks.unregister(callback);
  }

  @Override
  public void destroy() {
    final Looper looper = mainWorker.getLooper();
    if (looper != null) { looper.quit(); }
    apiCallbacks.kill();
    mainHandler = null;
    mainWorker = null;
    appService = null;
    if (DEBUG) { Log.d(TAG, "API methods destroyed"); }
    System.gc();
  }

  public boolean isWorking() { return workingFlag.get(); }

  /**
   * Information about last operation.
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  private static class APICallInfoData {
    /** Operation code. */
    int operation = Operation.NOP;
    /** Operation token. */
    int token = -1;
    /** Response data. */
    ResponseData responseData = new ResponseData();

    public void set(final APICallInfoData data) {
      this.operation = data.operation;
      this.token = data.token;
      responseData = data.responseData;
    }
    public void set(final ResponseData rd) {
      final ResponseData responseData = this.responseData;
      responseData.setErrorCode(rd.getErrorCode());
      responseData.setData(rd.getData());
      responseData.setMessage(rd.getMessage());
    }
    public void set(final int opCode, final int token) {
      this.operation = opCode;
      this.token = token;
    }
    public boolean hasData() { return operation != Operation.NOP; }
  }

  /**
   * @author Roman Mazur (Stanfy - http://www.stanfy.com)
   */
  private abstract static class CallbackReporter {
    /** Reporter name. */
    final String name;
    protected CallbackReporter(final String name) {
      this.name = name;
    }
    abstract void report(final ApiMethodCallback callback, final int token, final int operation, final ResponseData responseData) throws RemoteException;
  }

}
