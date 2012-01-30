package com.stanfy.content;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.stanfy.images.model.CachedImage;
import com.stanfy.serverapi.cache.APICacheDAO;
import com.stanfy.views.R;

/**
 * @author Roman Mazur (Stanfy - http://www.stanfy.com)
 */
public class AppDatabaseManager extends SQLiteOpenHelper {

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

  /**
   * Perform DDL operation related to images cache.
   * @param context context instance
   * @param db database instance
   */
  protected void createImagesCache(final Context context, final SQLiteDatabase db) {
    createTable(db, R.string.sql_ddl_cached_image, CachedImage.Contract.TABLE_NAME, CachedImage.Contract._ID);
    db.execSQL(getSQL(R.string.sql_ddl_cached_image_index_url, CachedImage.Contract.TABLE_NAME));
    db.execSQL(getSQL(R.string.sql_ddl_cached_image_index_type, CachedImage.Contract.TABLE_NAME));
    db.execSQL(getSQL(R.string.sql_ddl_cached_image_index_usage_ts, CachedImage.Contract.TABLE_NAME));
  }

  /**
   * Drop tables related to images cache.
   * @param context context instance
   * @param db database instance
   */
  protected void dropImagesCache(final Context context, final SQLiteDatabase db) {
    db.execSQL("DROP TABLE IF EXISTS " + CachedImage.Contract.TABLE_NAME);
  }

  @Override
  public void onCreate(final SQLiteDatabase db) {
    APICacheDAO.ensureCacheTable(db);
    createImagesCache(context, db);
  }

  @Override
  public void onUpgrade(final SQLiteDatabase db, final int oldVersion, final int newVersion) {
    APICacheDAO.ensureCacheTable(db);
    dropImagesCache(context, db);
    createImagesCache(context, db);
  }

}
