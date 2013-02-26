package com.stanfy.enroscar.beans;

/**
 * Interface of an object that can flush its resources on demand.
 * @author Roman Mazur (Stanfy - http://stanfy.com)
 *
 */
public interface FlushableBean extends Bean {

  /**
   * Flush resources.
   * @param beansContainer beans container instance
   */
  void flushResources(final BeansContainer beansContainer);

}
