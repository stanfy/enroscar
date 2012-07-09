package com.stanfy.app.loader;

import org.junit.Before;

import com.google.mockwebserver.MockResponse;
import com.stanfy.app.beans.BeansManager.Editor;

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
    editor.required().remoteServerApi("xml");
  }

  @Override
  protected void enqueueResponse(final MyModel model) {
    getWebServer().enqueue(new MockResponse().setBody(
      "<model><id>" + model.id + "</id><text>" + model.text + "</text></model>"
    ));
  }

}
