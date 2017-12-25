package com.example.wolfgao.mybakingapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by gaochuang on 2017/12/18.
 */

public class MyBakingDBHelper extends SQLiteOpenHelper {
    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 2;

    public static final String DATABASE_NAME = "baking.db";

    public MyBakingDBHelper(Context cxt){
        super(cxt,DATABASE_NAME, null, DATABASE_VERSION);
    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_CAKES_TABLE = "CREATE TABLE " + MyBakingContract.CakesEntry.TABLE_NAME + " (" +
                MyBakingContract.CakesEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MyBakingContract.CakesEntry.COLUMN_CAK_NAME + " TEXT NOT NULL, " +
                MyBakingContract.CakesEntry.COLUMN_CAK_KEY + " TEXT NOT NULL, " +
                MyBakingContract.CakesEntry.COLUMN_CAK_IMG + " TEXT, " +
                MyBakingContract.CakesEntry.COLUMN_CAK_INGRE + " TEXT NOT NULL," +
                " FOREIGN KEY (" + MyBakingContract.CakesEntry.COLUMN_CAK_NAME + ") REFERENCES " +
                MyBakingContract.StepsEntry.TABLE_NAME + " (" + MyBakingContract.StepsEntry.COLUMN_CAKE_NAME + "))";

        final String SQL_CREATE_STEPS_TABLE = "CREATE TABLE " + MyBakingContract.StepsEntry.TABLE_NAME + " (" +
                MyBakingContract.StepsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MyBakingContract.StepsEntry.COLUMN_CAKE_NAME + " TEXT NOT NULL, " +
                MyBakingContract.StepsEntry.COLUMN_STEP_NO + " TEXT NOT NULL, " +
                MyBakingContract.StepsEntry.COLUMN_STEP_SHORT + " TEXT, " +
                MyBakingContract.StepsEntry.COLUMN_STEP_DESC + " TEXT, " +
                MyBakingContract.StepsEntry.COLUMN_STEP_VIDEO + ")";

        db.execSQL(SQL_CREATE_STEPS_TABLE);

        db.execSQL(SQL_CREATE_CAKES_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // This database is only a cache for online data, so its upgrade policy is
        // to simply to discard the data and start over
        // Note that this only fires if you change the version number for your database.
        // It does NOT depend on the version number for your application.
        // If you want to update the schema without wiping data, commenting out the next 2 lines
        // should be your top priority before modifying this method.
        db.execSQL("DROP TABLE IF EXISTS " + MyBakingContract.CakesEntry.TABLE_NAME);
        //sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + WeatherContract.WeatherDictEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + MyBakingContract.StepsEntry.TABLE_NAME);
        onCreate(db);
    }
}
