package com.stanfy.enroscar.sample;

import com.stanfy.serverapi.RequestMethodHelper;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class SampleRequestMethodHelper extends RequestMethodHelper {

  public SampleRequestMethodHelper() {
    super(TYPE_JSON, SampleApplication.APP_AUTHORITY);
  }

}
