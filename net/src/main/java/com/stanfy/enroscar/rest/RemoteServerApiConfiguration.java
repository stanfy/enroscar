package com.stanfy.enroscar.rest;

import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.beans.InitializingBean;
import com.stanfy.enroscar.beans.ManagerAwareBean;
import com.stanfy.enroscar.rest.request.RequestDescription;
import com.stanfy.enroscar.rest.response.DefaultResponseModelConverter;
import com.stanfy.enroscar.rest.response.ResponseModelConverter;

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

  /** Request method. */
  private RequestMethod requestMethod;
  /** Response model converter. */
  private ResponseModelConverter responseModelConverter;

  /** Default content handler name. */
  private String defaultContentHandlerName;

  /** Default cache bean name. */
  private String defaultCacheBeanName;

  public void setRequestMethod(final RequestMethod requestMethod) {
    this.requestMethod = requestMethod;
  }
  public void setResponseModelConverter(final ResponseModelConverter responseModelConverter) {
    this.responseModelConverter = responseModelConverter;
  }


  public RequestMethod getRequestMethod(final RequestDescription requestDescription) { return requestMethod; }
  public ResponseModelConverter getResponseModelConverter(final RequestDescription requestDescription) { return responseModelConverter; }

  public String getDefaultContentHandlerName() { return defaultContentHandlerName; }
  public void setDefaultContentHandlerName(final String defaultContentHandlerName) {
    checkBeanExists(defaultContentHandlerName);
    this.defaultContentHandlerName = defaultContentHandlerName;
  }

  public String getDefaultCacheBeanName() { return defaultCacheBeanName; }
  public void setDefaultCacheBeanName(final String defaultCacheBeanName) {
    checkBeanExists(defaultCacheBeanName);
    this.defaultCacheBeanName = defaultCacheBeanName;
  }

  /**
   * Throw {@link IllegalArgumentException} if the specified bean does not exist in the beans container.
   * @param name bean name
   */
  protected void checkBeanExists(final String name) {
    if (name == null) { throw new NullPointerException("Bean name cannot be null"); }
    if (!beansManager.getContainer().containsBean(name)) {
      throw new IllegalArgumentException("Bean " + name + " does not exist");
    }
  }

  public RequestDescription createRequestDescription() { return new RequestDescription(); }

  @Override
  public void onInitializationFinished(final BeansContainer beansContainer) {
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
