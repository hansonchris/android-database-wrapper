package com.hansonchris.android.databasewrapper;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author  Chris Hanson    <chrishanson.php@gmail.com>
 * @version 1.0
 * @since   2014-06-20
 * @see     https://github.com/hansonchris/android-database-wrapper/blob/master/README
 */
abstract public class Database 
{
    protected Context context;
    protected SQLiteOpenHelper openHelper;

    public Database(Context context)
    {
        this.context = context;
        openHelper = getSQLiteOpenHelper();
        getDatabase();
    }

    public void execSQL(String sql)
    {
        SQLiteDatabase db = getDatabase();
        db.execSQL(sql);
    }

    abstract protected SQLiteOpenHelper getSQLiteOpenHelper();

    protected SQLiteDatabase getDatabase() 
    {
        return openHelper.getWritableDatabase();
    }

    public int delete(String table, String whereClause, String[] whereArgs)
    {
        SQLiteDatabase db = getDatabase();
        int result = db.delete(table, whereClause, whereArgs);

        return result;
    }

    public long insert(String table, String nullColumnHack, ContentValues values)
    {
        SQLiteDatabase db = getDatabase();
        long result = db.insert(table, nullColumnHack, values);

        return result;
    }

    public Cursor query(
        boolean distinct,
        String table,
        String[] columns,
        String selection,
        String[] selectionArgs,
        String groupBy,
        String having,
        String orderBy,
        String limit
    ) {
        SQLiteDatabase db = getDatabase();
        Cursor result = db.query(distinct, table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

        return result;
    }

    public Cursor query(
        String table,
        String[] columns,
        String selection,
        String[] selectionArgs,
        String groupBy,
        String having,
        String orderBy
    ) {
        SQLiteDatabase db = getDatabase();
        Cursor result = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy);

        return result;
    }

    public Cursor query(
        String table,
        String[] columns,
        String selection,
        String[] selectionArgs,
        String groupBy,
        String having,
        String orderBy,
        String limit
    ) {
        SQLiteDatabase db = getDatabase();
        Cursor result = db.query(table, columns, selection, selectionArgs, groupBy, having, orderBy, limit);

        return result;
    }

    public int update(String table, ContentValues values, String whereClause, String[] whereArgs)
    {
        SQLiteDatabase db = getDatabase();
        int result = db.update(table, values, whereClause, whereArgs);

        return result;
    }

    public void closeDatabase()
    {
        try {
            openHelper.close();
        } catch (Exception e) {
            handleExceptionClosingDatabase();
        }
    }

    protected void handleExceptionClosingDatabase()
    {
        //can be overridden in subclasses
    }
}