package com.stanfy.views.utils;

import java.util.concurrent.LinkedBlockingQueue;

/**
 * Queue of tasks.
 * @author Roman Mazur - Stanfy (http://www.stanfy.com)
 */
class TasksQueue extends LinkedBlockingQueue<Runnable> {

  /** serialVersionUID. */
  private static final long serialVersionUID = -165973105801976098L;

  private void removeAll(final Object o) {
    boolean removed;
    do {
      removed = remove(o);
    } while (removed);
  }

  public void cancelTask(final String name) {
    if (isEmpty()) { return; }
    removeAll(new Object() {
      @Override
      public boolean equals(final Object o) {
        if (o instanceof Task) {
          return name.equals(((Task)o).getName());
        }
        return false;
      }
      @Override
      public int hashCode() { return 0; }
    });
  }

  public void cancelTaskByTag(final Object tag) {
    if (isEmpty()) { return; }
    removeAll(new Object() {
      @Override
      public boolean equals(final Object o) {
        if (o instanceof Task) {
          return tag.equals(((Task)o).getTag());
        }
        return false;
      }
      @Override
      public int hashCode() { return 0; }
    });
  }

}
