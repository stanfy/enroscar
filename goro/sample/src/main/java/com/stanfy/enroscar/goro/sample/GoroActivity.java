package com.stanfy.enroscar.goro.sample;

import android.app.Activity;
import android.os.Bundle;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Activity that demonstrates Goro.
 */
public class GoroActivity extends Activity {

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.goro);

    LinkedHashMap<String, List<Integer>> map = new LinkedHashMap<>();
    map.put("a", Arrays.asList(1, 2));
    map.put("b", Arrays.asList(2, 3));

    ((GoroView) findViewById(R.id.goro)).setData(map);
  }
}
