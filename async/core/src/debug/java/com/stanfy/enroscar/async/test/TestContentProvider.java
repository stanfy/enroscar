package com.stanfy.enroscar.async.test;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

/**
 * Test content provider.
 */
public class TestContentProvider extends ContentProvider {

  private Db db;

  @Override
  public boolean onCreate() {
    db = new Db(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
    return db.getReadableDatabase().query("test", projection, selection, selectionArgs, null, null, sortOrder);
  }

  @Override
  public String getType(Uri uri) {
    return null;
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    return null;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    return 0;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    return 0;
  }

  /** Test db. */
  private static class Db extends SQLiteOpenHelper {

    public Db(Context context) {
      super(context, "test", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      db.execSQL("create table test(a TEXT, b TEXT)");
      ContentValues values = new ContentValues();
      values.put("a", "one fish");
      values.put("b", "two fish");
      db.insert("test", null, values);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
  }

}
