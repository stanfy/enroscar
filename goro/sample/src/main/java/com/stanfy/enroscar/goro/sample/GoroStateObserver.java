package com.stanfy.enroscar.goro.sample;

import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;

import com.stanfy.enroscar.goro.GoroListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
* Goro state observer. Updates view state.
*/
public class GoroStateObserver implements GoroListener {

  private final GoroView goroView;
  private final Map<String, List<Integer>> data;

  public GoroStateObserver(final GoroView goroView, final Bundle state) {
    this.goroView = goroView;
    if (state != null) {
      //noinspection ConstantConditions
      data = state.<State>getParcelable("goroState").data;
    } else {
      data = new LinkedHashMap<>();
    }
    update();
  }

  private void update() {
    goroView.setData(new LinkedHashMap<>(data));
  }

  @Override
  public void onTaskSchedule(Callable<?> task, String queue) {
    List<Integer> tasks = data.get(queue);
    if (tasks == null) {
      tasks = new LinkedList<>();
      data.put(queue, tasks);
    }
    tasks.add(((GoroActivity.SimpleTask) task).getNumber());
    update();
  }

  @Override
  public void onTaskStart(Callable<?> task) { }

  @Override
  public void onTaskFinish(Callable<?> task, Object result) {
    int n = ((GoroActivity.SimpleTask) task).getNumber();
    List<Integer> list = data.get(((GoroActivity.SimpleTask) task).queue);
    list.remove(Integer.valueOf(n));
    update();
  }

  @Override
  public void onTaskCancel(Callable<?> task) { }

  @Override
  public void onTaskError(Callable<?> task, Throwable error) { }

  public void save(final Bundle state) {
    state.putParcelable("goroState", new State(data));
  }

  /** What can be saved. */
  public static class State implements Parcelable {

    public static final Creator<State> CREATOR = new Creator<State>() {
      @Override
      public State createFromParcel(final Parcel source) {
        return new State(source);
      }

      @Override
      public State[] newArray(int size) {
        return new State[0];
      }
    };

    private final Map<String, List<Integer>> data;

    public State(final Map<String, List<Integer>> data) {
      this.data = data;
    }

    State(final Parcel in) {
      int count = in.readInt();
      data = new LinkedHashMap<>(count);
      for (int i = 0; i < count; i++) {
        String key = in.readString();
        int numbersCount = in.readInt();
        ArrayList<Integer> numbers = new ArrayList<>(numbersCount);
        for (int j = 0; j < numbersCount; j++) {
          numbers.add(in.readInt());
        }
        data.put(key, numbers);
      }
    }

    @Override
    public int describeContents() {
      return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
      dest.writeInt(data.size());
      for (Map.Entry<String, List<Integer>> entry : data.entrySet()) {
        dest.writeString(entry.getKey());
        dest.writeInt(entry.getValue().size());
        for (int n : entry.getValue()) {
          dest.writeInt(n);
        }
      }
    }
  }

}
