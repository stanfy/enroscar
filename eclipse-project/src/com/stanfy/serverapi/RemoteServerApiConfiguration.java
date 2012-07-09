package com.stanfy.serverapi;

import com.stanfy.app.beans.BeansManager;
import com.stanfy.app.beans.EnroscarBean;
import com.stanfy.app.beans.InitializingBean;
import com.stanfy.app.beans.ManagerAwareBean;
import com.stanfy.app.service.ApplicationService;
import com.stanfy.serverapi.request.RequestDescription;
import com.stanfy.serverapi.response.DefaultResponseModelConverter;
import com.stanfy.serverapi.response.ResponseModelConverter;

/**
 * Configures remote server API access classes.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(RemoteServerApiConfiguration.BEAN_NAME)
public class RemoteServerApiConfiguration implements InitializingBean, ManagerAwareBean {

  /** Bean name. */
  public static final String BEAN_NAME = "RemoteServerApiConfiguration";

  /** Bean manager instance. */
  private BeansManager beansManager;

  /** Application service class. */
  private Class<?> applicationServiceClass;

  /** Request method. */
  private RequestMethod requestMethod;
  /** Response model converter. */
  private ResponseModelConverter responseModelConverter;

  /** Default content handler name. */
  private String defaultContentHandlerName;

  public RemoteServerApiConfiguration() {
    this(ApplicationService.class);
  }

  protected RemoteServerApiConfiguration(final Class<?> applicationServiceClass) {
    this.applicationServiceClass = applicationServiceClass;
  }

  public void setRequestMethod(final RequestMethod requestMethod) {
    this.requestMethod = requestMethod;
  }
  public void setResponseModelConverter(final ResponseModelConverter responseModelConverter) {
    this.responseModelConverter = responseModelConverter;
  }


  public void setApplicationServiceClass(final Class<?> applicationServiceClass) { this.applicationServiceClass = applicationServiceClass; }
  public Class<?> getApplicationServiceClass() { return applicationServiceClass; }

  public RequestMethod getRequestMethod(final RequestDescription requestDescription) { return requestMethod; }
  public ResponseModelConverter getResponseModelConverter(final RequestDescription requestDescription) { return responseModelConverter; }

  public String getDefaultContentHandlerName() { return defaultContentHandlerName; }
  public void setDefaultContentHandlerName(final String defaultContentHandlerName) {
    checkBeanExists(defaultContentHandlerName);
    this.defaultContentHandlerName = defaultContentHandlerName;
  }

  protected void checkBeanExists(final String name) {
    if (name == null) { throw new NullPointerException("Bean name cannot be null"); }
    if (!beansManager.getContainer().containsBean(name)) {
      throw new IllegalArgumentException("Bean " + name + " does not exist");
    }
  }

  public RequestDescription createRequestDescription() { return new RequestDescription(); }

  @Override
  public void onInititializationFinished() {
    if (this.requestMethod == null) {
      this.requestMethod = new RequestMethod();
    }
    if (this.responseModelConverter == null) {
      this.responseModelConverter = new DefaultResponseModelConverter();
    }
  }

  @Override
  public void setBeansManager(final BeansManager beansManager) {
    this.beansManager = beansManager;
  }

}
