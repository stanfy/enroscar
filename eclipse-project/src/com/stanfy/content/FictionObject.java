package com.stanfy.content;

/**
 * Fiction object. Often it's inserted by analyzers to original lists in order to introduce list sections.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public interface FictionObject extends UniqueObject {

  /** @return display name */
  String getDisplayName();

}
