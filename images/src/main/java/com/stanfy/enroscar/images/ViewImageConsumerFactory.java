package com.stanfy.enroscar.images;

import android.view.View;

/**
 * Factory constructs {@link ImageConsumer} object for provided widget.
 */
public interface ViewImageConsumerFactory {

  ImageConsumer createConsumer(View view);
  
}
