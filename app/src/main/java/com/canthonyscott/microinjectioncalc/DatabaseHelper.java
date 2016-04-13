package com.canthonyscott.microinjectioncalc;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

class DatabaseHelper extends SQLiteOpenHelper{

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "MicroinjectionDatabase";

    public DatabaseHelper (Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(FeedReaderContract.SQL_CREATE_MO_ENTRIES);
        db.execSQL(FeedReaderContract.SQL_CREATE_RNA_ENTRIES);
        this.addToMOTable("AvgMO", 8400.0, db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2){
            db.execSQL(FeedReaderContract.SQL_CREATE_RNA_ENTRIES);
        }
    }

    public Cursor getAllRowsFromMOTable(SQLiteDatabase db){
        // String for the requested columns below
        String[] projection = {FeedReaderContract.FeedEntry._ID, FeedReaderContract.FeedEntry.COLUMN_NAME_GENE, FeedReaderContract.FeedEntry.COLUMN_NAME_MOLWT};
        // String for the sortOrder command
        String sortOrder = FeedReaderContract.FeedEntry._ID + " DESC";
        //Cursor object to receive the Db query
        // Query the Db to retrieve all rows from the MO Table

        return db.query(
                FeedReaderContract.FeedEntry.MORPHOLINOS_TABLE,
                projection,null,null,null,null,sortOrder);

    }

    // This method simplifies adding MOs into the selected Database
    public void addToMOTable (String name, Double mw, SQLiteDatabase db){
        // set values to be added
        ContentValues values = new ContentValues();
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_GENE, name);
        values.put(FeedReaderContract.FeedEntry.COLUMN_NAME_MOLWT, mw);
        db.insert(FeedReaderContract.FeedEntry.MORPHOLINOS_TABLE, null, values);
    }

    public int removeFromMOTable (SQLiteDatabase db, Integer id){
        int rowsRemoved;
        String table = FeedReaderContract.FeedEntry.MORPHOLINOS_TABLE;
        String whereClause = "_id" + "=?";
        String [] whereArgs = new String[] {id.toString()};
        rowsRemoved = db.delete(table,whereClause,whereArgs);
        Log.d("DatabaseHelper","Remove Query sent to SQL db");
        return rowsRemoved;
    }

}
