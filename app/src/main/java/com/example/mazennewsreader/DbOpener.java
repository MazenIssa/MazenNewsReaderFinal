package com.example.mazennewsreader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class DbOpener extends SQLiteOpenHelper {
    protected final static String DATABASE_NAME = "FavDB";
    protected final static int VERSION_NUM = 1;
    public final static String TABLE_NAME = "FAVOURITES";
    public final static String TITLE = "TITLE";
    public final static String DESC = "DESCRIPTION";
    public final static String DATE = "PUBLICATION_DATE";
    public final static String URL = "URL_LINK";
    public final static String COL_ID = "_id";

    public DbOpener(Context ctx) {
        super(ctx, DATABASE_NAME, null, VERSION_NUM);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_NAME + " (" + COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + TITLE + " TEXT, " + DESC + " TEXT, " + DATE + " TEXT, " + URL + " TEXT);");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("Database upgrade", "Old version:" + oldVersion + " newVersion:" + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("Database downgrade", "Old version:" + oldVersion + " newVersion:" + newVersion);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }
}
