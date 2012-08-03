package com.stanfy.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.stanfy.app.beans.Bean;
import com.stanfy.app.beans.EnroscarBean;
import com.stanfy.views.R;

/**
 * Database manager used by {@link AppContentProvider}.
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
@EnroscarBean(value = AppDatabaseManager.BEAN_NAME, contextDependent = true)
public abstract class AppDatabaseManager extends SQLiteOpenHelper implements Bean {

  /** Bean name. */
  public static final String BEAN_NAME = "AppDatabaseManager";

  /** Database name. */
  public static final String DB_NAME_DEFAULT = "app.db";

  /** Context instance. */
  private final Context context;

  protected AppDatabaseManager(final Context context, final int version) {
    this(context, null, version);
  }

  protected AppDatabaseManager(final Context context, final CursorFactory factory, final int version) {
    this(context, DB_NAME_DEFAULT, factory, version);
  }

  protected AppDatabaseManager(final Context context, final String dbName, final CursorFactory factory, final int version) {
    super(context, dbName, factory, version);
    this.context = context.getApplicationContext();
  }

//  NB! requires API 11

//  protected AppDatabaseManager(final Context context, final String dbName, final CursorFactory factory, final int version, final DatabaseErrorHandler errorHandler) {
//    super(context, dbName, factory, version, errorHandler);
//    this.context = context.getApplicationContext();
//  }

  /**
   * @return application context instance
   */
  public Context getContext() { return context; }

  /**
   * @param id resource ID
   * @param params parameters
   * @return SQL string
   */
  protected String getSQL(final int id, final Object... params) { return context.getString(id, params); }

  protected void createIndex(final SQLiteDatabase db, final String table, final String columnName) {
    final String indexName = "idx_" + table + "_" + columnName;
    db.execSQL(getSQL(R.string.sql_ddl_create_index, indexName, table, columnName));
  }

}
