package com.stanfy.utils;

import java.util.HashMap;
import java.util.Map;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.stanfy.DebugFlags;

/**
 * This class allows users to do multiple inserts into a table but
 * compile the SQL insert statement only once, which may increase
 * performance.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com) - taken from Android sources, modified to specify conflict policy.
 */
public final class DBInsertHelper {

  /** Debug flag. */
  private static final boolean DEBUG = DebugFlags.DEBUG_DB_UTILS;
  /** Logging tag. */
  private static final String TAG = "InsertHelper";

  /** Database. */
  private final SQLiteDatabase mDb;
  /** Table name. */
  private final String mTableName;
  /** Columns. */
  private HashMap<String, Integer> mColumns;
  /** SQL code. */
  private String mInsertSQL = null;
  /** Insert statement. */
  private SQLiteStatement mInsertStatement = null;
  /** Replace statement. */
  private SQLiteStatement mReplaceStatement = null;
  /** Prepared statement. */
  private SQLiteStatement mPreparedStatement = null;

  /** Conflict policy. */
  private String conflictPolicy;

  /**
   * {@hide}
   *
   * These are the columns returned by sqlite's "PRAGMA
   * table_info(...)" command that we depend on.
   */
  public static final int TABLE_INFO_PRAGMA_COLUMNNAME_INDEX = 1,
                          TABLE_INFO_PRAGMA_DEFAULT_INDEX = 4;

  /**
   * @param db the SQLiteDatabase to insert into
   * @param tableName the name of the table to insert into
   */
  public DBInsertHelper(final SQLiteDatabase db, final String tableName) {
    mDb = db;
    mTableName = tableName;
  }

  /** @param conflictPolicy the conflictPolicy to set */
  public void setConflictPolicy(final String conflictPolicy) {
    this.conflictPolicy = conflictPolicy;
  }

  private void buildSQL() {
    final StringBuilder sb = new StringBuilder(128);
    sb.append("INSERT INTO ");
    sb.append(mTableName);
    sb.append(" (");

    final StringBuilder sbv = new StringBuilder(128);
    sbv.append("VALUES (");

    int i = 1;
    Cursor cur = null;
    try {
      cur = mDb.rawQuery("PRAGMA table_info(" + mTableName + ")", null);
      mColumns = new HashMap<String, Integer>(cur.getCount());
      while (cur.moveToNext()) {
        final String columnName = cur.getString(TABLE_INFO_PRAGMA_COLUMNNAME_INDEX);
        final String defaultValue = cur.getString(TABLE_INFO_PRAGMA_DEFAULT_INDEX);

        mColumns.put(columnName, i);
        sb.append("'");
        sb.append(columnName);
        sb.append("'");

        if (defaultValue == null) {
          sbv.append("?");
        } else {
          sbv.append("COALESCE(?, ");
          sbv.append(defaultValue);
          sbv.append(")");
        }

        sb.append(i == cur.getCount() ? ") " : ", ");
        sbv.append(i == cur.getCount() ? ");" : ", ");
        ++i;
      }
    } finally {
      if (cur != null) {
        cur.close();
      }
    }

    sb.append(sbv);

    mInsertSQL = sb.toString();
    if (DEBUG) {
      Log.v(TAG, "insert statement is " + mInsertSQL);
    }
  }

  private SQLiteStatement getStatement(final boolean allowReplace) {
    final int offset = 6;
    if (allowReplace) {
      if (mReplaceStatement == null) {
        if (mInsertSQL == null) {
          buildSQL();
        }
        // chop "INSERT" off the front and prepend "INSERT OR REPLACE" instead.
        final String replaceSQL = "INSERT OR REPLACE" + mInsertSQL.substring(offset);
        mReplaceStatement = mDb.compileStatement(replaceSQL);
      }
      return mReplaceStatement;
    } else {
      if (mInsertStatement == null) {
        if (mInsertSQL == null) {
          buildSQL();
        }
        String insertSQL = mInsertSQL;
        if (conflictPolicy != null) {
          insertSQL = "INSERT OR " + conflictPolicy.toUpperCase() + mInsertSQL.substring(offset);
        }
        mInsertStatement = mDb.compileStatement(insertSQL);
      }
      return mInsertStatement;
    }
  }

  /**
   * Performs an insert, adding a new row with the given values.
   *
   * @param values the set of values with which  to populate the
   * new row
   * @param allowReplace if true, the statement does "INSERT OR
   *   REPLACE" instead of "INSERT", silently deleting any
   *   previously existing rows that would cause a conflict
   *
   * @return the row ID of the newly inserted row, or -1 if an
   * error occurred
   */
  private synchronized long insertInternal(final ContentValues values, final boolean allowReplace) {
    try {
      final SQLiteStatement stmt = getStatement(allowReplace);
      stmt.clearBindings();
      if (DEBUG) {
        Log.v(TAG, "--- inserting in table " + mTableName);
      }
      for (final Map.Entry<String, Object> e : values.valueSet()) {
        final String key = e.getKey();
        final int i = getColumnIndex(key);
        DatabaseUtils.bindObjectToProgram(stmt, i, e.getValue());
        if (DEBUG) {
          Log.v(TAG, "binding " + e.getValue() + " to column "
              + i + " (" + key + ")");
        }
      }
      return stmt.executeInsert();
    } catch (final SQLException e) {
      Log.e(TAG, "Error inserting " + values + " into table  " + mTableName, e);
      return -1;
    }
  }

  /**
   * Returns the index of the specified column. This is index is suitagble for use
   * in calls to bind().
   * @param key the column name
   * @return the index of the column
   */
  public int getColumnIndex(final String key) {
    getStatement(false);
    final Integer index = mColumns.get(key);
    if (index == null) {
      throw new IllegalArgumentException("column '" + key + "' is invalid");
    }
    return index;
  }

  /**
   * Bind the value to an index. A prepareForInsert() or prepareForReplace()
   * without a matching execute() must have already have been called.
   * @param index the index of the slot to which to bind
   * @param value the value to bind
   */
  public void bind(final int index, final double value) {
    mPreparedStatement.bindDouble(index, value);
  }

  /**
   * Bind the value to an index. A prepareForInsert() or prepareForReplace()
   * without a matching execute() must have already have been called.
   * @param index the index of the slot to which to bind
   * @param value the value to bind
   */
  public void bind(final int index, final float value) {
    mPreparedStatement.bindDouble(index, value);
  }

  /**
   * Bind the value to an index. A prepareForInsert() or prepareForReplace()
   * without a matching execute() must have already have been called.
   * @param index the index of the slot to which to bind
   * @param value the value to bind
   */
  public void bind(final int index, final long value) {
    mPreparedStatement.bindLong(index, value);
  }

  /**
   * Bind the value to an index. A prepareForInsert() or prepareForReplace()
   * without a matching execute() must have already have been called.
   * @param index the index of the slot to which to bind
   * @param value the value to bind
   */
  public void bind(final int index, final int value) {
    mPreparedStatement.bindLong(index, value);
  }

  /**
   * Bind the value to an index. A prepareForInsert() or prepareForReplace()
   * without a matching execute() must have already have been called.
   * @param index the index of the slot to which to bind
   * @param value the value to bind
   */
  public void bind(final int index, final boolean value) {
    mPreparedStatement.bindLong(index, value ? 1 : 0);
  }

  /**
   * Bind null to an index. A prepareForInsert() or prepareForReplace()
   * without a matching execute() must have already have been called.
   * @param index the index of the slot to which to bind
   */
  public void bindNull(final int index) {
    mPreparedStatement.bindNull(index);
  }

  /**
   * Bind the value to an index. A prepareForInsert() or prepareForReplace()
   * without a matching execute() must have already have been called.
   * @param index the index of the slot to which to bind
   * @param value the value to bind
   */
  public void bind(final int index, final byte[] value) {
    if (value == null) {
      mPreparedStatement.bindNull(index);
    } else {
      mPreparedStatement.bindBlob(index, value);
    }
  }

  /**
   * Bind the value to an index. A prepareForInsert() or prepareForReplace()
   * without a matching execute() must have already have been called.
   * @param index the index of the slot to which to bind
   * @param value the value to bind
   */
  public void bind(final int index, final String value) {
    if (value == null) {
      mPreparedStatement.bindNull(index);
    } else {
      mPreparedStatement.bindString(index, value);
    }
  }

  /**
   * Performs an insert, adding a new row with the given values.
   * If the table contains conflicting rows, an error is
   * returned.
   *
   * @param values the set of values with which to populate the
   * new row
   *
   * @return the row ID of the newly inserted row, or -1 if an
   * error occurred
   */
  public long insert(final ContentValues values) {
    return insertInternal(values, false);
  }

  /**
   * Execute the previously prepared insert or replace using the bound values
   * since the last call to prepareForInsert or prepareForReplace.
   *
   * <p>Note that calling bind() and then execute() is not thread-safe. The only thread-safe
   * way to use this class is to call insert() or replace().
   *
   * @return the row ID of the newly inserted row, or -1 if an
   * error occurred
   */
  public long execute() {
    if (mPreparedStatement == null) {
      throw new IllegalStateException("you must prepare this inserter before calling "
          + "execute");
    }
    try {
      if (DEBUG) {
        Log.v(TAG, "--- doing insert or replace in table " + mTableName);
      }
      return mPreparedStatement.executeInsert();
    } catch (final SQLException e) {
      Log.e(TAG, "Error executing InsertHelper with table " + mTableName, e);
      return -1;
    } finally {
      // you can only call this once per prepare
      mPreparedStatement = null;
    }
  }

  /**
   * Prepare the InsertHelper for an insert. The pattern for this is:
   * <ul>
   * <li>prepareForInsert()
   * <li>bind(index, value);
   * <li>bind(index, value);
   * <li>...
   * <li>bind(index, value);
   * <li>execute();
   * </ul>
   */
  public void prepareForInsert() {
    mPreparedStatement = getStatement(false);
    mPreparedStatement.clearBindings();
  }

  /**
   * Prepare the InsertHelper for a replace. The pattern for this is:
   * <ul>
   * <li>prepareForReplace()
   * <li>bind(index, value);
   * <li>bind(index, value);
   * <li>...
   * <li>bind(index, value);
   * <li>execute();
   * </ul>
   */
  public void prepareForReplace() {
    mPreparedStatement = getStatement(true);
    mPreparedStatement.clearBindings();
  }

  /**
   * Performs an insert, adding a new row with the given values.
   * If the table contains conflicting rows, they are deleted
   * and replaced with the new row.
   *
   * @param values the set of values with which to populate the
   * new row
   *
   * @return the row ID of the newly inserted row, or -1 if an
   * error occurred
   */
  public long replace(final ContentValues values) {
    return insertInternal(values, true);
  }

  /**
   * Close this object and release any resources associated with
   * it.  The behavior of calling <code>insert()</code> after
   * calling this method is undefined.
   */
  public void close() {
    if (mInsertStatement != null) {
      mInsertStatement.close();
      mInsertStatement = null;
    }
    if (mReplaceStatement != null) {
      mReplaceStatement.close();
      mReplaceStatement = null;
    }
    mInsertSQL = null;
    mColumns = null;
  }
}
