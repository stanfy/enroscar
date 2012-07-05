package com.stanfy.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.stanfy.views.R;

/**
 * Database manager used by {@link AppContentProvider}.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public abstract class AppDatabaseManager extends SQLiteOpenHelper {

  /** Database name. */
  public static final String DB_NAME = "app.db";

  /** Context instance. */
  private final Context context;

  public AppDatabaseManager(final Context context, final CursorFactory factory, final int version) {
    super(context, DB_NAME, factory, version);
    this.context = context.getApplicationContext();
  }

  /**
   * @param id resource ID
   * @param params parameters
   * @return SQL string
   */
  protected String getSQL(final int id, final Object... params) { return context.getString(id, params); }

  protected void createTable(final SQLiteDatabase db, final int ddlId, final String tableName, final String idColumnName) {
    db.execSQL(getSQL(ddlId, tableName, idColumnName));
  }
  protected void createIndex(final SQLiteDatabase db, final String table, final String columnName) {
    final String indexName = "idx_" + table + "_" + columnName;
    db.execSQL(getSQL(R.string.sql_ddl_create_index, indexName, table, columnName));
  }

}
