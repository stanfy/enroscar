package com.stanfy.content;

import java.io.Serializable;

/**
 * Fiction object. Often it's inserted by analyzers to original lists in order to introduce list sections.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class FictionObject implements UniqueObject, Serializable {

  /** serialVersionUID. */
  private static final long serialVersionUID = 3443008734514707082L;

  /** @return display name */
  public abstract String getDisplayName();

  @Override
  public long getId() { return -1; }

}
