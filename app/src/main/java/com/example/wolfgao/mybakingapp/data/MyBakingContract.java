package com.example.wolfgao.mybakingapp.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by gaochuang on 2017/12/18.
 */

public class MyBakingContract {
    // The "Content authority" is a name for the entire content provider, similar to the
    // relationship between a domain name and its website.  A convenient string to use for the
    // content authority is the package name for the app, which is guaranteed to be unique on the
    // device.
    public static final String CONTENT_AUTHORITY = "com.example.wolfgao.mybakingapp";

    // Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
    // the content provider.
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_CAKES = "cakes";
    public static final String PATH_STEPS = "steps";

    /*
        Inner class that defines the table contents of the cakes table
    */
    public static final class CakesEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CAKES).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CAKES;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CAKES;

        public static final String TABLE_NAME = "cakes";
        public static final String COLUMN_CAK_KEY = "cake_id";
        public static final String COLUMN_CAK_NAME = "cake_name";
        public static final String COLUMN_CAK_INGRE = "ingredients";
        public static final String COLUMN_CAK_IMG = "image_url";

        //Return Uri with parameter cake id
        public static Uri buildCakeIdUri(String cake_id){
            return CONTENT_URI.buildUpon().appendPath(cake_id).build();
        }

        /**
         * 以'/'为间隔获取字段，因此get(0)应该是path，get(1)应该是后面的query
         * @param uri
         * @return String Id
         */
        public static String getCakeIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);

        }

        public static Uri buildIdUrl(long id){
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }

    public static final class StepsEntry implements BaseColumns{
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STEPS).build();
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STEPS;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STEPS;

        public static final String TABLE_NAME = "steps";
        //This is the foreign key for cakes table, references on cakes cake_id.
        public static final String COLUMN_CAKE_KEY = "cake_key";
        public static final String COLUMN_STEP_NO = "step_no";
        public static final String COLUMN_STEP_SHORT = "step_short";
        public static final String COLUMN_STEP_DESC = "step_desc";
        public static final String COLUMN_STEP_VIDEO = "step_video";

        //Using step name to get all data of steps
        public static Uri buildCakeKeyUri(String cake_key) {
            return CONTENT_URI.buildUpon().appendPath(cake_key).build();
        }

        public static String getCakeKeyFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        //return record id
        public static Uri buildStepsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }
    }
}
