package com.stanfy.enroscar.goro.sample;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Displays state of Goro queues.
 */
public class GoroView extends View {

  /** Max height for one queue. */
  private static final int MAX_QUEUE_HEIGHT = 100;
  /** Width of a brick. */
  private static final int BRICK_WIDTH = 50;

  /** What should be rendered. */
  private Map<String, List<Integer>> data = Collections.emptyMap();

  private int maxQueueHeight;
  private int brickWidth;

  private Paint queuePaint, taskPaint, textPaint;

  public GoroView(final Context context) {
    super(context);
    init();
  }

  public GoroView(final Context context, final AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public GoroView(final Context context, final AttributeSet attrs, final int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  public void setData(final Map<String, List<Integer>> data) {
    this.data = data;
    invalidate();
  }

  private void init() {
    Resources res = getResources();
    //noinspection ConstantConditions
    float density = res.getDisplayMetrics().density;
    maxQueueHeight = (int)(density * MAX_QUEUE_HEIGHT + 0.5);
    brickWidth = (int)(density * BRICK_WIDTH + 0.5);

    queuePaint = new Paint();
    taskPaint = new Paint();
    textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    queuePaint.setColor(0xff8000);
    taskPaint.setColor(0x000f88);
    textPaint.setColor(0xffffff);

  }

  @Override
  protected void onDraw(final Canvas canvas) {
    if (data.isEmpty()) {
      return;
    }

    int availableHeight = getHeight();
    int maxHeight = data.size() * maxQueueHeight;
    if (availableHeight > maxHeight) {
      availableHeight = maxHeight;
    }

    int queueHeight = availableHeight / data.size();

    int y = 0;
    for (Map.Entry<String, List<Integer>> queue : data.entrySet()) {
      Log.d(VIEW_LOG_TAG, "Draw " + y);
      canvas.drawRect(0, y, brickWidth, y + queueHeight, queuePaint);
    }
  }
}
