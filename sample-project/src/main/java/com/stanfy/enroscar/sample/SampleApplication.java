package com.stanfy.enroscar.sample;

import android.app.Application;
import android.content.Context;

import com.stanfy.enroscar.assist.DefaultBeansManager;
import com.stanfy.enroscar.rest.RemoteServerApiConfiguration;
import com.stanfy.enroscar.rest.response.handler.XmlGsonContentHandler;
import com.stanfy.enroscar.sample.model.Rss;
import com.stanfy.gsonxml.GsonXml;
import com.stanfy.gsonxml.GsonXmlBuilder;

/**
 * Sample application.
 */
public class SampleApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    DefaultBeansManager.get(this)
        .edit().defaults()
        .remoteServerApi("json")
        .put(XmlHandler.class)
        .put(Rss.RssItemsListAnalyzer.class)
        .commit();

    RemoteServerApiConfiguration apiConfig = DefaultBeansManager.get(this).getRemoteServerApiConfiguration();
    apiConfig.setDebugRest(true);
    apiConfig.setDebugRestResponse(true);
  }

  /** Configure XML parsing. */
  static class XmlHandler extends XmlGsonContentHandler {

    public XmlHandler(final Context context) {
      super(context);
    }

    @Override
    protected GsonXml createGsonXml() {
      return new GsonXmlBuilder().setXmlParserCreator(PARSER_FACTORY).setSameNameLists(true).create();
    }

  }

}
