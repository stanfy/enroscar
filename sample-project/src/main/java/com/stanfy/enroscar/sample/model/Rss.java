package com.stanfy.enroscar.sample.model;

import android.content.Context;
import android.net.Uri;

import com.google.gson.annotations.SerializedName;
import com.stanfy.enroscar.beans.EnroscarBean;
import com.stanfy.enroscar.content.ResponseData;
import com.stanfy.enroscar.content.UniqueObject;
import com.stanfy.enroscar.net.operation.RequestDescription;
import com.stanfy.enroscar.rest.response.ContentAnalyzer;
import com.stanfy.enroscar.rest.response.Model;

import java.util.ArrayList;

/**
 * RSS feed.
 */
public class Rss {

  /** Channel. */
  private Channel channel;

  public Channel getChannel() {
    return channel;
  }

  /** RSS channel. */
  public static class Channel {

    /** Channel title. */
    private String title;

    /** Items. */
    @SerializedName("item")
    private RssItemsList items;

    public String getTitle() {
      return title;
    }

    public RssItemsList getItems() {
      return items;
    }

  }

  /** Flickr RSS channel item. */
  public static class Item implements UniqueObject {

    /** Title. */
    private String title;

    /** Link. */
    private String link;

    /** Author. */
    private Author author;

    /** Thumbnail URL. */
    @SerializedName("media:thumbnail")
    private Thumbnail thumbnail;

    /** Image url. */
    private String url;

    @Override
    public long getId() {
      return Long.parseLong(Uri.parse(link).getLastPathSegment());
    }

    public String getTitle() {
      return title;
    }

    public Author getAuthor() {
      return author;
    }

    public Thumbnail getThumbnail() {
      return thumbnail;
    }

    public String getUrl() {
      return url;
    }

  }

  /** Author. */
  public static class Author {
    /** Profile URL. */
    @SerializedName("@flickr:profile")
    private String profileUrl;

    /** Name. */
    @SerializedName("$")
    private String name;


    public String getProfileUrl() {
      return profileUrl;
    }

    public String getName() {
      return name;
    }
  }

  /** Thumbnail. */
  public static class Thumbnail {
    /** URL. */
    @SerializedName("@url")
    private String url;

    /** Width. */
    @SerializedName("@width")
    private int width;
    /** Height. */
    @SerializedName("@height")
    private int height;

    public String getUrl() {
      return url;
    }

    public int getWidth() {
      return width;
    }

    public int getHeight() {
      return height;
    }

  }

  /** List of RSS items. */
  @Model(wrapper = Rss.class, analyzer = RssItemsListAnalyzer.BEAN_NAME)
  public static class RssItemsList extends ArrayList<Item> {
  }

  @EnroscarBean(RssItemsListAnalyzer.BEAN_NAME)
  public static class RssItemsListAnalyzer implements ContentAnalyzer<Rss, RssItemsList> {

    public static final String BEAN_NAME = "RssItemsListAnalyzer";

    @Override
    public ResponseData<RssItemsList> analyze(final Context context, final RequestDescription requestDescription,
                                              final ResponseData<Rss> rssResponseData) {
//      ResponseData<RssItemsList> result = new ResponseData<RssItemsList>();
//      result.setErrorCode(rssResponseData.getErrorCode());
//      result.setMessage(rssResponseData.getMessage());
//
//      Rss rss = rssResponseData.getEntity();
//      if (rss == null) { return result; }
//      result.setEntity(rss.getChannel().getItems());
//      return result;
      return null;
    }
  }

}
