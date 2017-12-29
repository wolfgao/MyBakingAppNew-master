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
        //This is the foreign key direct to the steps table
        public static final String COLUMN_CAK_NAME = "cake_name";
        public static final String COLUMN_CAK_INGRE = "ingredients";
        public static final String COLUMN_CAK_IMG = "image_url";

        //Return Uri with parameter cake name
        public static Uri buildNamesUri(String name) {
            return CONTENT_URI.buildUpon().appendPath(name).build();
        }

        //Return Uri with parameter cake id
        public static Uri buildIdUri(String id){
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        // Return Uri with parameter record id "_ID".
        public static Uri buildCakesUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }



        /**
         * Uri结构三种方式
         [scheme:]scheme-specific-part[#fragment]
         [scheme:][//authority][path][?query][#fragment]
         [scheme:][//host:port][path][?query][#fragment]
         content://media/internal/images  这个URI将返回设备上存储的所有图片
         content://contacts/people/  这个URI将返回设备上的所有联系人信息
         content://contacts/people/45 这个URI返回单个结果（联系人信息中ID为45的联系人记录）转为Uri为：
         Uri person = ContentUris.withAppendedId(People.CONTENT_URI,  45);
         */

        /**
         * 以'/'为间隔获取字段，因此get(0)应该是path，get(1)应该是后面的query
         * @param uri
         * @return String Id
         */
        public static String getIdFromUri(Uri uri){
            return uri.getPathSegments().get(1);

        }

        public static String getNameFromUri(Uri uri){
            return uri.getPathSegments().get(1);

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
        //This is the foreign key for cakes table.
        public static final String COLUMN_CAKE_NAME = "cake_name";
        public static final String COLUMN_STEP_NO = "step_no";
        public static final String COLUMN_STEP_SHORT = "step_short";
        public static final String COLUMN_STEP_DESC = "step_desc";
        //This is the foreign key direct to the steps table
        public static final String COLUMN_STEP_VIDEO = "step_video";

        //Using step id to get all data of steps
        public static Uri buildIdUri(String id) {
            return CONTENT_URI.buildUpon().appendPath(id).build();
        }

        // Return Uri with parameter record id.
        public static Uri buildStepsUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static String getIdFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }
    }
}
