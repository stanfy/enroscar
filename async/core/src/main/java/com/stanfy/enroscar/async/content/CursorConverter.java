package com.stanfy.enroscar.async.content;

import android.database.Cursor;

/**
 * Converts {@link Cursor} to an object. It must not close a cursor.
 * @author Roman Mazur - Stanfy (http://stanfy.com)
 */
public interface CursorConverter<T> {

  T toObject(Cursor cursor);

}
