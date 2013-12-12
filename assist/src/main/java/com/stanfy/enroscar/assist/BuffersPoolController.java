package com.stanfy.enroscar.assist;

import com.stanfy.enroscar.beans.BeansContainer;
import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.beans.FlushableBean;
import com.stanfy.enroscar.beans.InitializingBean;
import com.stanfy.enroscar.io.BuffersPool;

/**
 * Bean controller for BuffersPool.
 */
@EnroscarBean("enroscar.assist.BuffersPoolController")
public class BuffersPoolController implements FlushableBean, InitializingBean {

  /** Pool instance. */
  private BuffersPool buffersPool;


  @Override
  public void flushResources(BeansContainer beansContainer) {
    buffersPool.flush();
  }

  @Override
  public void onInitializationFinished(BeansContainer beansContainer) {
    buffersPool = beansContainer.getBean(BuffersPool.class);
  }

}
