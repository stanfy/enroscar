package com.stanfy.serverapi.response;

import android.content.Context;

import com.stanfy.serverapi.request.RequestDescription;

/**
 * Interface for contexts that can be configured by request description.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface RequestConfigurableContext {

  /**
   * @param description request description
   * @param systemContext system context
   */
  void configureContext(final RequestDescription description, final Context systemContext);

}
