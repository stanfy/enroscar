package com.stanfy.enroscar.sample;

import java.util.List;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

import com.stanfy.app.fragments.list.RequestBuilderListFragment;
import com.stanfy.enroscar.sample.model.Tweet;
import com.stanfy.serverapi.request.RequestBuilder;
import com.stanfy.views.LoadableImageView;
import com.stanfy.views.list.ModelListAdapter.ElementRenderer;

/**
 * Fragment that displays tweets of Twitter API.
 */
public class TwitterFragment extends RequestBuilderListFragment<Tweet, List<Tweet>> {

  /** Row view holder. */
  private static class TweetViewHolder {
    LoadableImageView image;
    TextView text;
  }

  /** Post renderer. */
  private static ElementRenderer<Tweet> RENDERER = new ElementRenderer<Tweet>(R.layout.row_tweet) {
    @Override
    public void render(final Adapter adapter, final ViewGroup parent, final Tweet element, final View view, final Object holder, final int position) {
      final TweetViewHolder h = (TweetViewHolder)holder;
      h.text.setText(element.getText());
      h.image.setImageURI(element.getProfileImageUri());
    }
    @Override
    public Object createHolder(final View view) {
      final TweetViewHolder h = new TweetViewHolder();
      h.image = (LoadableImageView)view.findViewById(R.id.tweet_image);
      h.text = (TextView)view.findViewById(R.id.tweet_text);
      return h;
    }
  };

  @Override
  protected ElementRenderer<Tweet> createRenderer() { return RENDERER; }

  @Override
  protected RequestBuilder<List<Tweet>> createRequestBuilder() {
    return new TweetsRequestBuilder(getActivity())
        .setScreenname("twitterapi")
        .asLoadMoreList()
        .setLimit(10)
        .setOffset(1);
  }

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    startLoad();
  }

}
