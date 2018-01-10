package com.example.wolfgao.mybakingapp.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * Created by gaochuang on 2017/12/18.
 */

public class MyBakingProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    // Define some code to match URI
    static final int CAKES = 200;
    static final int CAKE_WITH_KEY = 202;
    static final int STEPS = 300;
    static final int STEP_WITH_NO = 301;
    static final int STEPS_WITH_CAKEID = 302;

    private MyBakingDBHelper mOpenHelper;

    /**
     * How to query a record when a table is joined with another table by a foreign key.
     */
    private static final SQLiteQueryBuilder sBakingStepsbyIdBuilder;
    static {
        sBakingStepsbyIdBuilder = new SQLiteQueryBuilder();
        // 在表中存在至少一个匹配时，INNER JOIN 关键字返回行。
        // SELECT column_name(s) FROM table_name1 INNER JOIN table_name2 ON table_name1.column_name=table_name2.column_name
        sBakingStepsbyIdBuilder.setTables(
                MyBakingContract.StepsEntry.TABLE_NAME
                        + " INNER JOIN "
                        + MyBakingContract.CakesEntry.TABLE_NAME +
                        " ON " + MyBakingContract.StepsEntry.TABLE_NAME +
                "." + MyBakingContract.StepsEntry.COLUMN_CAKE_KEY +
                        " = " + MyBakingContract.CakesEntry.TABLE_NAME +
                        "." + MyBakingContract.CakesEntry.COLUMN_CAK_KEY);
    }

    //Define the query string with conditions.
    private static final String sCakesIdSelection = MyBakingContract.CakesEntry.TABLE_NAME +
            "." + MyBakingContract.CakesEntry.COLUMN_CAK_KEY + " = ? ";

    public static final String sStepsCakeIdSelection = MyBakingContract.StepsEntry.TABLE_NAME +
            "." + MyBakingContract.StepsEntry.COLUMN_CAKE_KEY + " = ? ";

    public static final String sStepsStepNoSelection = MyBakingContract.StepsEntry.TABLE_NAME +
            "." + MyBakingContract.StepsEntry.COLUMN_CAKE_KEY + " = ? and " +
            MyBakingContract.StepsEntry.TABLE_NAME + "." + MyBakingContract.StepsEntry.COLUMN_STEP_NO + " = ? ";


    private static UriMatcher buildUriMatcher() {
        //Add all Path to the UriMatcher, then return a code when a match is found.
        //The code passed into the constructor represents the code to return for the root URI.
        // 下面是官方文档
        /**
         private static final UriMatcher sURIMatcher = new UriMatcher();
         static
         {
         sURIMatcher.addURI("contacts", "/people", PEOPLE);
         sURIMatcher.addURI("contacts", "/people/#", PEOPLE_ID);
         sURIMatcher.addURI("contacts", "/people/#/phones", PEOPLE_PHONES);
         sURIMatcher.addURI("contacts", "/people/#/phones/#", PEOPLE_PHONES_ID);
         sURIMatcher.addURI("contacts", "/people/#/contact_methods", PEOPLE_CONTACTMETHODS);
         sURIMatcher.addURI("contacts", "/people/#/contact_methods/#", PEOPLE_CONTACTMETHODS_ID);
         sURIMatcher.addURI("contacts", "/deleted_people", DELETED_PEOPLE);
         sURIMatcher.addURI("contacts", "/phones", PHONES);
         sURIMatcher.addURI("contacts", "/phones/filter/*", PHONES_FILTER);
         sURIMatcher.addURI("contacts", "/phones/#", PHONES_ID);
         sURIMatcher.addURI("contacts", "/contact_methods", CONTACTMETHODS);
         sURIMatcher.addURI("contacts", "/contact_methods/#", CONTACTMETHODS_ID);
         sURIMatcher.addURI("call_log", "/calls", CALLS);
         sURIMatcher.addURI("call_log", "/calls/filter/*", CALLS_FILTER);
         sURIMatcher.addURI("call_log", "/calls/#", CALLS_ID);
         }
         */
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MyBakingContract.CONTENT_AUTHORITY;

        matcher.addURI(authority,MyBakingContract.PATH_CAKES, CAKES);
        matcher.addURI(authority,MyBakingContract.PATH_STEPS, STEPS);
        matcher.addURI(authority,MyBakingContract.PATH_CAKES + "/#",CAKE_WITH_KEY);
        matcher.addURI(authority,MyBakingContract.PATH_STEPS + "/*", STEPS_WITH_CAKEID);
        //要匹配的content://com.example.wolfgao.mybakingapp/steps/1/0
        matcher.addURI(authority, MyBakingContract.PATH_STEPS + "/*/#", STEP_WITH_NO);
        return matcher;
    }

    @Override
    public boolean onCreate() {
        mOpenHelper = new MyBakingDBHelper(this.getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor = null;
        switch (sUriMatcher.match(uri)){
            case CAKE_WITH_KEY:
                retCursor = getCakeById(uri, projection, sortOrder);
                break;
            case CAKES:
                retCursor = null;
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MyBakingContract.CakesEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case STEPS:
                retCursor = null;
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MyBakingContract.StepsEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case STEPS_WITH_CAKEID:
                retCursor = getStepsByCakeID(uri, projection, sortOrder);
                break;
            case STEP_WITH_NO:
                retCursor = getStepsByCakeKeyAndNo(uri, projection, sortOrder);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private Cursor getCakeById(Uri uri, String[] projection, String sortOrder) {
        String id = MyBakingContract.CakesEntry.getCakeIdFromUri(uri);
        return new SQLiteQueryBuilder().query(mOpenHelper.getReadableDatabase(),
                projection,
                sCakesIdSelection,
                new String[]{id},
                null,
                null,
                sortOrder);
    }


    private Cursor getStepsByCakeKeyAndNo(Uri uri, String[] projection, String sortOrder) {
        String cake_key = MyBakingContract.StepsEntry.getCakeKeyFromStepCakeKeyNoUri(uri);
        String step_no = MyBakingContract.StepsEntry.getStepNoFromStepCakeKeyNoUri(uri);
        //因为steps的stepid实际上是cakes的cake name
        return sBakingStepsbyIdBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sStepsStepNoSelection,
                new String[]{cake_key,step_no},
                null,
                null,
                sortOrder);
    }

    private Cursor getStepsByCakeID(Uri uri, String[] projection, String sortOrder) {
        String cake_key = MyBakingContract.StepsEntry.getCakeKeyFromUri(uri);
        //因为steps的stepid实际上是cakes的cake key
        return sBakingStepsbyIdBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sStepsCakeIdSelection,
                new String[]{cake_key},
                null,
                null,
                sortOrder);
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match){
            case CAKE_WITH_KEY:
                return MyBakingContract.CakesEntry.CONTENT_ITEM_TYPE;
            case CAKES:
                return MyBakingContract.CakesEntry.CONTENT_TYPE;
            case STEPS_WITH_CAKEID:
                return MyBakingContract.StepsEntry.CONTENT_TYPE;
            case STEPS:
                return MyBakingContract.StepsEntry.CONTENT_TYPE;
            case STEP_WITH_NO:
                return MyBakingContract.StepsEntry.CONTENT_ITEM_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;
        switch (match) {
            case CAKES: {
                long _id = db.insert(MyBakingContract.CakesEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MyBakingContract.CakesEntry.buildIdUrl(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case STEPS: {
                long _id = db.insert(MyBakingContract.StepsEntry.TABLE_NAME, null, values);
                if ( _id > 0 )
                    returnUri = MyBakingContract.StepsEntry.buildStepsUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case CAKES:
                rowsDeleted = db.delete(
                        MyBakingContract.CakesEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case STEPS:
                rowsDeleted = db.delete(
                        MyBakingContract.StepsEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;
        // this makes delete all rows return the number of rows deleted
        if ( null == selection ) selection = "1";
        switch (match) {
            case CAKES:
                rowsUpdated = db.update(
                        MyBakingContract.CakesEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case STEPS:
                rowsUpdated = db.update(
                        MyBakingContract.StepsEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
