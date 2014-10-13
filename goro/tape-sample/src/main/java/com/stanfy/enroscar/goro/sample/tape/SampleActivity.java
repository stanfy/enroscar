package com.stanfy.enroscar.goro.sample.tape;

import android.net.ConnectivityManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.squareup.tape.TaskQueue;
import com.stanfy.enroscar.async.Action;
import com.stanfy.enroscar.async.Async;
import com.stanfy.enroscar.async.Send;
import com.stanfy.enroscar.goro.BoundGoro;
import com.stanfy.enroscar.goro.sample.R;
import com.stanfy.enroscar.goro.sample.tape.tasks.TapeHandler;
import com.stanfy.enroscar.goro.sample.tape.tasks.TransactionTask;
import com.stanfy.enroscar.goro.support.AsyncGoro;

import java.io.IOException;
import java.util.concurrent.Callable;

import javax.inject.Inject;

/**
 * Puts a task to a queue.
 */
public class SampleActivity extends FragmentActivity {

  @Inject BoundGoro goro;
  @Inject TokenGenerator tokenGenerator;
  @Inject ConnectivityManager connectivityManager;
  @Inject TaskQueue<TransactionTask> tape;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    SampleApplication.graph(this, new SampleActivityModule(this)).inject(this);
    setContentView(R.layout.activity_sample);

    final SampleActivityOperator operator = SampleActivityOperator.build()
        .operations(this)
        .withinActivity(this)
        .get();

    operator.when().enqueueIsFinished()
        .doOnResult(new Action<String>() {
          @Override
          public void act(String token) {
            Toast.makeText(getApplicationContext(), "Task " + token + " is scheduled", Toast.LENGTH_SHORT).show();
            goro.schedule(TapeHandler.create(getApplicationContext(), goro));
          }
        })
        .doOnError(new Action<Throwable>() {
          @Override
          public void act(Throwable throwable) {
            // task is not written to the tape...
            // space troubles?
            Log.e("ShitHappens", "oops", throwable);
            Toast.makeText(getApplicationContext(), "XXX Cannot schedule your operation XXX", Toast.LENGTH_SHORT).show();
          }
        });

    findViewById(R.id.button_enqueue).setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        operator.enqueue();
      }
    });
  }

  @Override
  protected void onStart() {
    super.onStart();
    goro.bind();
  }

  @Override
  protected void onStop() {
    super.onStop();
    goro.unbind();
  }

  @Send
  Async<String> enqueue() {
    return new AsyncGoro(goro).schedule(new Enqueue(tokenGenerator, tape));
  }

  /**
   * Creates a new task with token that will have to be executed on the queue 'tansactions-queue'
   * and writes it to the tape.
   */
  private static final class Enqueue implements Callable<String> {
    private final TokenGenerator generator;
    private final TaskQueue<TransactionTask> tape;

    Enqueue(final TokenGenerator generator, final TaskQueue<TransactionTask> tape) {
      this.generator = generator;
      this.tape = tape;
    }

    @Override
    public String call() throws IOException {
      Log.i("123123", "Adding to tape " + tape);
      String token = generator.nextToken();
      tape.add(new TransactionTask(token));
      return token;
    }
  }

}
