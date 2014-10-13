package com.stanfy.enroscar.goro.sample.tape.tasks;

import android.content.Context;
import android.util.Log;

import com.squareup.tape.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

/** A task with some token executed on some queue. */
public class TransactionTask implements Task<Context> {

  private final String token;

  public TransactionTask(final String token) {
    this.token = token;
  }

  @Override
  public void execute(final Context context) {
    Log.i("TransactionTask", "Execute transaction " + token);

    InputStream stream = null;
    try {
      stream = new URL("http://stanfy.com").openStream();
      stream.read();
    } catch (Exception e) {
      Log.e("TransactionTask", "Failed " + token, e);
      throw new TransactionException();
    } finally {
      if (stream != null) {
        try {
          stream.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  public String getToken() {
    return token;
  }

}
