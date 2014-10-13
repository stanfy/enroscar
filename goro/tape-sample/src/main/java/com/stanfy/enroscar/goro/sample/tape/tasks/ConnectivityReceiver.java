package com.stanfy.enroscar.goro.sample.tape.tasks;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.stanfy.enroscar.goro.GoroService;
import com.stanfy.enroscar.goro.sample.tape.SampleApplication;

import javax.inject.Inject;

public class ConnectivityReceiver extends BroadcastReceiver {

  @Inject ConnectivityManager connectivityManager;

  @Override
  public void onReceive(final Context context, final Intent intent) {
    SampleApplication.graph(context).inject(this);

    NetworkInfo connection = connectivityManager.getActiveNetworkInfo();
    if (connection != null && connection.isConnected()) {
      context.startService(GoroService.taskIntent(context, new TapeHandler()));
    }
  }
}
