package com.stanfy.enroscar.goro.sample;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
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
  private int spacing;

  private Paint queuePaint, strokePaint, taskPaint, textPaint;
  private Paint.FontMetrics textMetrics = new Paint.FontMetrics();

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
    spacing = (int)(density * 5);

    queuePaint = new Paint();
    strokePaint = new Paint();
    taskPaint = new Paint();
    textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    queuePaint.setColor(0xffff8000);
    strokePaint.setColor(Color.WHITE);
    strokePaint.setStrokeWidth(1 * density);
    strokePaint.setStyle(Paint.Style.STROKE);
    taskPaint.setColor(0xff000f88);

    textPaint.setColor(0xffffffff);
    textPaint.setTextSize(20 * density);
    textPaint.setTextAlign(Paint.Align.CENTER);

    if (isInEditMode()) {
      LinkedHashMap<String, List<Integer>> map = new LinkedHashMap<>();
      map.put("a", Arrays.asList(1, 2));
      map.put("b", Arrays.asList(2, 3));
      setData(map);
    }
  }

  @Override
  protected void onDraw(final Canvas canvas) {
    if (data.isEmpty()) {
      return;
    }

    int availableHeight = getHeight();
    int maxHeight = data.size() * (maxQueueHeight + spacing) + spacing;
    if (availableHeight > maxHeight) {
      availableHeight = maxHeight;
    }

    int queueHeight = (availableHeight - spacing) / data.size() - spacing;

    int y = spacing;
    for (Map.Entry<String, List<Integer>> queue : data.entrySet()) {
      int y2 = y + queueHeight;

      canvas.drawRect(spacing, y, brickWidth + spacing, y2, queuePaint);
      canvas.drawRect(spacing, y, brickWidth + spacing, y2, strokePaint);
      textPaint.getFontMetrics(textMetrics);

      float textY = y + queueHeight / 2 - textMetrics.ascent / 2;
      canvas.drawText(queue.getKey(), brickWidth / 2 + spacing, textY,
          textPaint);

      int x = brickWidth + 2 * spacing;
      for (Integer task : queue.getValue()) {
        int x2 = x + brickWidth;
        canvas.drawRect(x, y, x2, y2, taskPaint);
        canvas.drawRect(x, y, x2, y2, strokePaint);
        canvas.drawText(String.valueOf(task), x + brickWidth / 2, textY, textPaint);
        x = x2 + spacing;
      }

      y = y2 + spacing;
    }
  }

}
