package com.stanfy.enroscar.sample;

import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

import com.stanfy.enroscar.fragments.RequestBuilderListFragment;
import com.stanfy.enroscar.images.views.LoadableImageView;
import com.stanfy.enroscar.net.operation.RequestBuilder;
import com.stanfy.enroscar.sample.model.Rss;
import com.stanfy.enroscar.views.list.adapter.ElementRenderer;
import com.stanfy.enroscar.views.list.adapter.RendererBasedAdapter;

/**
 * Fragment that displays tweets of Twitter API.
 */
public class FlickrFragment extends RequestBuilderListFragment<Rss.Item, Rss.RssItemsList> {

  /** Row view holder. */
  private static class FlickrViewHolder {
    LoadableImageView image;
    TextView text;
  }

  /** Post renderer. */
  private static ElementRenderer<Rss.Item> RENDERER = new ElementRenderer<Rss.Item>(R.layout.row_tweet) {
    @Override
    public void render(final Adapter adapter, final ViewGroup parent, final Rss.Item element, final View view, final Object holder, final int position) {
      final FlickrViewHolder h = (FlickrViewHolder)holder;
      h.text.setText(element.getTitle());
      h.image.setImageURI(Uri.parse(element.getThumbnail().getUrl()));
    }
    @Override
    public Object createHolder(final View view) {
      final FlickrViewHolder h = new FlickrViewHolder();
      h.image = (LoadableImageView)view.findViewById(R.id.tweet_image);
      h.text = (TextView)view.findViewById(R.id.tweet_text);
      return h;
    }
  };

//  @Override
//  protected ElementRenderer<Rss.Item> createRenderer() { return RENDERER; }
//
//  @Override
//  protected RequestBuilder<Rss.RssItemsList> createRequestBuilder() {
//    return new SimpleRequestBuilder<Rss.RssItemsList>(getActivity()) {}
//        .setUrl("http://ycpi.api.flickr.com/services/feeds/photos_public.gne")
//        .addParam("format", "rss2")
//        .setFormat("xml");
//  }

  @Override
  protected RequestBuilder<Rss.RssItemsList> createRequestBuilder() {
    return null;
  }

  @Override
  protected RendererBasedAdapter<Rss.Item> createAdapter() {
    return null;
  }

  @Override
  public void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    startLoad();
  }

  @Override
  public void onCreateOptionsMenu(final Menu menu, final MenuInflater inflater) {
    menu.add("refresh");
  }

  @Override
  public boolean onOptionsItemSelected(final MenuItem item) {
    reload();
    return true;
  }

}
