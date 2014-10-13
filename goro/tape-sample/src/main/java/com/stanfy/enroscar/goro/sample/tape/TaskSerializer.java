package com.stanfy.enroscar.goro.sample.tape;

import android.util.Log;

import com.google.gson.Gson;
import com.squareup.tape.FileObjectQueue;
import com.stanfy.enroscar.goro.sample.tape.tasks.TransactionTask;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

class TaskSerializer implements FileObjectQueue.Converter<TransactionTask> {

  private final Gson gson = new Gson();

  @Override
  public TransactionTask from(final byte[] bytes) throws IOException {
    return gson.fromJson(new InputStreamReader(new ByteArrayInputStream(bytes), "UTF-8"), TransactionTask.class);
  }

  @Override
  public void toStream(final TransactionTask tokenTask, final OutputStream outputStream) throws IOException {
    Writer writer = new OutputStreamWriter(outputStream, "UTF-8");
    gson.toJson(tokenTask, writer);
    writer.close();
  }

}
