package com.stanfy.enroscar.sample.other;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

import com.stanfy.enroscar.sample.R;
import com.stanfy.enroscar.views.qa.BasicQuickAction;
import com.stanfy.enroscar.views.qa.BasicQuickActionsAdapter;
import com.stanfy.enroscar.views.qa.QuickActionsBar;
import com.stanfy.enroscar.views.qa.QuickActionsWidget;

/**
 * Sample for quick actions.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class QuickActionsSampleActivity extends Activity {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.qa);
    final BasicQuickActionsAdapter adapter = new BasicQuickActionsAdapter(this,
        new BasicQuickAction(this, R.drawable.ic_launcher, R.string.qademo_action1),
        new BasicQuickAction(this, R.drawable.ic_launcher, R.string.qademo_action2)
    );
    final QuickActionsBar qaBar = new QuickActionsBar(this);
    qaBar.setQuickActionsAdapter(adapter);
    findViewById(R.id.qa_button).setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(final View v) {
        qaBar.show(v);
      }
    });
    qaBar.setOnQuickActionClickListener(new QuickActionsWidget.OnQuickActionClickListener() {
      @Override
      public void onQuickActionClicked(final QuickActionsWidget widget, final int position) {
        Log.d("EnroscarQA", "Pos " + position);
      }
    });
  }

}
