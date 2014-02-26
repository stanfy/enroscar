package com.stanfy.enroscar.rest;

import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.BeansManager;
import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.beans.InitializingBean;
import com.stanfy.enroscar.beans.ManagerAwareBean;
import com.stanfy.enroscar.net.operation.RequestDescription;

/**
 * Configures remote server API access classes.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 */
@EnroscarBean(RemoteServerApiConfiguration.BEAN_NAME)
public class RemoteServerApiConfiguration implements InitializingBean, ManagerAwareBean {

  /** Bean name. */
  public static final String BEAN_NAME = "enroscar.RemoteServerApiConfiguration";

  /** Bean manager instance. */
  private BeansManager beansManager;

  /** Request method. */
  private RequestMethod defaultRequestMethod;

  /** Default content handler name. */
  private String defaultContentHandlerName;

  /** Default cache bean name. */
  private String defaultCacheBeanName;

  /** Debug REST calls. */
  private boolean debugRest;
  
  /** Debug REST response. */
  private boolean debugRestResponse;

  public void setDefaultRequestMethod(final RequestMethod requestMethod) {
    this.defaultRequestMethod = requestMethod;
  }


  public RequestMethod getRequestMethod(final RequestDescription requestDescription) { return defaultRequestMethod; }

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

  public void setDebugRest(final boolean debugRest) { this.debugRest = debugRest; }
  public boolean isDebugRestResponse() { return debugRestResponse; }
  
  public void setDebugRestResponse(final boolean debugRestResponse) { this.debugRestResponse = debugRestResponse; }
  public boolean isDebugRest() { return debugRest; }
  
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
    if (this.defaultRequestMethod == null) {
      this.defaultRequestMethod = new RequestMethod();
    }
  }

  @Override
  public void setBeansManager(final BeansManager beansManager) {
    this.beansManager = beansManager;
  }

}
