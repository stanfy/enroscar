package com.stanfy.enroscar.rest.loader.test;

import org.junit.Before;

import com.google.mockwebserver.MockResponse;
import com.stanfy.enroscar.beans.BeansManager.Editor;
import com.stanfy.enroscar.rest.response.handler.XmlGsonContentHandler;

/**
 * XML format support test.
 */
public class XmlRequestTest extends JsonRequestTest {

  @Override
  @Before
  public void setFormat() {
    format = "xml";
  }

  @Override
  protected void configureBeansManager(final Editor editor) {
    super.configureBeansManager(editor);
    editor.put(XmlGsonContentHandler.class);
  }

  @Override
  protected void whenBeansConfigured() {
    super.whenBeansConfigured();
    initContentHandler(XmlGsonContentHandler.BEAN_NAME);
  }
  
  @Override
  protected void enqueueResponse(final MyModel model) {
    getWebServer().enqueue(new MockResponse().setBody(
      "<model><id>" + model.id + "</id><text>" + model.text + "</text></model>"
    ));
  }

}
