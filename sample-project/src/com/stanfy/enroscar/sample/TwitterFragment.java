package com.stanfy.enroscar.sample;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.TextView;

import com.stanfy.app.fragments.list.OneTimeLoadListFragment;
import com.stanfy.enroscar.sample.model.Tweet;
import com.stanfy.views.list.ModelListAdapter.ElementRenderer;

/**
 * Fragment that displays tweets of Twitter API.
 */
public class TwitterFragment extends OneTimeLoadListFragment<SampleApplication, Tweet> {

  /** Post renderer. */
  private static ElementRenderer<Tweet> RENDERER = new ElementRenderer<Tweet>(android.R.layout.simple_list_item_1) {
    @Override
    public void render(final Adapter adapter, final ViewGroup parent, final Tweet element, final View view, final Object holder, final int position) {
      final TextView textView = (TextView)view;
      textView.setText(element.getText());
      textView.setLines(4);
    }
  };

  @Override
  protected ElementRenderer<Tweet> createRenderer() { return RENDERER; }

  @Override
  public void onActivityCreated(final Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    setRequestBuilder(
        new TweetsRequestBuilder(getOwnerActivity())
        .setScreenname("twitterapi")
    );
  }

}
