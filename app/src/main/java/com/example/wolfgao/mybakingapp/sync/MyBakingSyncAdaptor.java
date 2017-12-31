package com.example.wolfgao.mybakingapp.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.wolfgao.mybakingapp.MainActivity;
import com.example.wolfgao.mybakingapp.R;
import com.example.wolfgao.mybakingapp.RecipeJsonData;
import com.example.wolfgao.mybakingapp.Utility;
import com.example.wolfgao.mybakingapp.data.MyBakingContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

/**
 * Created by gaochuang on 2017/12/26.
 */

public class MyBakingSyncAdaptor extends AbstractThreadedSyncAdapter {


    // Interval at which to sync with the server, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;   //per 3 hours
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL/3;  //once an hour
    //private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24; //1000 days
    private static String DEBUG_TAG;
    private String mRecipeJson;
    private Context mContext;

    //
    private static final String[] NOTIFY_CAKE_PROJECTION = new String[] {
            MyBakingContract.CakesEntry.COLUMN_CAK_KEY,
            MyBakingContract.CakesEntry.COLUMN_CAK_NAME,
            MyBakingContract.CakesEntry.COLUMN_CAK_INGRE
    };

    // these indices must match the projection
    private static final int INDEX_CAKE_ID = 0;
    private static final int INDEX_CAKE_NAME = 1;
    private static final int INDEX_CAKE_INGRE = 2;

    private final static String RECIPEID = "id";
    private final static String RECIPENAME = "name";
    private final static String RECIPESTEPS = "steps";
    private final static String RECIPEIMAGE = "image";
    public static final String STEP_ID = "id";
    public static final String STEP_SHORT = "shortDescription";
    private final static String STEP_DESC = "description";
    private final static String STEP_VIDEO = "videoURL";

    public static final String ACTION_DATA_UPDATED = "com.example.wolfgao.mybakingapp.ACTION_DATA_UPDATED";


    //Constructor
    public MyBakingSyncAdaptor(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
        mContext = context;
        DEBUG_TAG = mContext.getPackageName();
    }

    /**
     * Helper method to have the sync adapter sync immediately
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);

        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    private static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));
        // If the password doesn't exist, the account doesn't exist
        if ( null == accountManager.getPassword(newAccount) ) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1) here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        // Since we've created an account
        MyBakingSyncAdaptor.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        // Without calling setSyncAutomatically, our periodic sync will not be enabled.
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    private static void configurePeriodicSync(Context context, int syncInterval, int syncFlextime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, syncFlextime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        //获取SharedPreference的值，传入参数进行解析
        HttpURLConnection conn = null;
        InputStream is = null;
        BufferedReader reader = null;
        String serverURL;

        //Can't open the website provided by Udacity, so I created a website myself to provide Json file.
        //https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json
        String serverIP = Utility.getServerIP(mContext.getApplicationContext(), "config.xml", "ip");
        String filePath = Utility.getServerIP(mContext.getApplicationContext(), "config.xml", "filePath");
        serverURL = serverIP + filePath;
        Log.i(DEBUG_TAG, "onPreExecute() called，server IP is "+serverIP+"; The file path is " + filePath);

        try {

            URL myURL = new URL(serverURL);
            Log.i(DEBUG_TAG, myURL.toString() + "\n" + "File:" + myURL.getFile());

            conn = (HttpURLConnection) myURL.openConnection();
            //设置链接超时5s
            conn.setConnectTimeout(8000);
            //设置读取超时3秒
            conn.setReadTimeout(3*1000);
            // 设置编码格式
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Accept-Language", "zh-CN");
            conn.setRequestProperty(
                    "Accept",
                    "image/gif, image/jpeg, image/pjpeg, image/pjpeg, " +
                            "application/x-shockwave-flash, application/xaml+xml, " +
                            "application/vnd.ms-xpsdocument, application/x-ms-xbap, " +
                            "application/x-ms-application, application/vnd.ms-excel, " +
                            "application/vnd.ms-powerpoint, application/msword, */*");
            // 设置HTTP获取方式
            conn.setRequestMethod("GET");
            //设置为长连接
            //conn.setRequestProperty("Connection", "Keep-Alive");
            conn.connect();
            int mResponseCode = conn.getResponseCode();
            Log.d(DEBUG_TAG, "The response code is: " + mResponseCode);

            if (mResponseCode == 200) {//200 status_ok
                is = conn.getInputStream();
                if (is == null) {
                    //do nothing, no any return data
                    mRecipeJson = null;
                    return;
                }
                //we get data from web site, but need convert them to Jsonstring
                reader = new BufferedReader(new InputStreamReader(is));
                String line;
                StringBuffer buffer = new StringBuffer();

                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }
                if (buffer.length() == 0) {
                    mRecipeJson = null;
                    return;
                }
                mRecipeJson = buffer.toString();
                getRecipeInfoFromJason(mRecipeJson);
                //Print returned Json information.
                Log.i(DEBUG_TAG, mRecipeJson);
                // Makes sure that the InputStream is closed after the app finished using it.
                is.close();
            }
            else{//要么server出问题，要么网络链接出问题，需要显示给用户
                setServerStatus(mContext,mResponseCode);
            }
        } catch (IOException e) {
            Log.e(DEBUG_TAG, "Error ", e);
            // If the code didn't successfully get the weather data, there's no point in attempting
            // to parse it.
            mRecipeJson = null;
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e("PlaceholderFragment", "Error closing stream", e);
                }
            }


        }
    }

    /**
     *  此静态方法主要是通过SharedPreferences来保存错误状态
     * @param c  Context
     * @param mResponseCode int 就是网络给的response code
     */
    private static void setServerStatus(Context c, int mResponseCode) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_server_status_key), mResponseCode);
        spe.commit();
    }

    /**
     *
     * @param jsonStr
     * @return
     */

    private void getRecipeInfoFromJason(String jsonStr){
        String cakeId;
        String cakeName;
        String image;
        String ingredients;

        try {
            RecipeJsonData recipeJsonData = new RecipeJsonData(jsonStr);
            JSONArray cakeArray = recipeJsonData.getObjectArray();

            int num = cakeArray.length();
            // Insert the new weather information into the database
            Vector<ContentValues> cakeVector = new Vector<ContentValues>(cakeArray.length());

            for (int i = 0; i < num; i++) {
                JSONObject cake = cakeArray.getJSONObject(i);
                cakeId = cake.getString(RECIPEID);
                cakeName = cake.getString(RECIPENAME); //using cake name as step id to direct to steps table.
                ingredients = RecipeJsonData.getRecipeDesc(cake);
                image = cake.getString(RECIPEIMAGE);
                JSONArray stepArray = cake.getJSONArray(RECIPESTEPS);

                ContentValues cakeValues = new ContentValues();
                cakeValues.put(MyBakingContract.CakesEntry.COLUMN_CAK_KEY, cakeId);
                cakeValues.put(MyBakingContract.CakesEntry.COLUMN_CAK_NAME, cakeName);
                cakeValues.put(MyBakingContract.CakesEntry.COLUMN_CAK_INGRE, ingredients);
                cakeValues.put(MyBakingContract.CakesEntry.COLUMN_CAK_IMG,image);

                cakeVector.add(cakeValues);


                int stepNo = stepArray.length();
                Vector<ContentValues> stepVector = new Vector<ContentValues>(stepNo);

                for (int j = 0; j<stepNo; j++){
                    String step_no = stepArray.getJSONObject(j).getString(STEP_ID);
                    String step_short = stepArray.getJSONObject(j).getString(STEP_SHORT);
                    String step_desc = stepArray.getJSONObject(j).getString(STEP_DESC);
                    String step_video = stepArray.getJSONObject(j).getString(STEP_VIDEO);
                    ContentValues stepValues = new ContentValues();
                    stepValues.put(MyBakingContract.StepsEntry.COLUMN_CAKE_KEY,cakeId);
                    stepValues.put(MyBakingContract.StepsEntry.COLUMN_STEP_NO,step_no);
                    stepValues.put(MyBakingContract.StepsEntry.COLUMN_STEP_SHORT,step_short);
                    stepValues.put(MyBakingContract.StepsEntry.COLUMN_STEP_DESC, step_desc);
                    stepValues.put(MyBakingContract.StepsEntry.COLUMN_STEP_VIDEO,step_video);

                    stepVector.add(stepValues);
                }
                //插入数据库
                int inserted = insertDB(stepVector, MyBakingContract.StepsEntry.CONTENT_URI);
                Log.i(DEBUG_TAG, "成功插入steps 表 "+inserted+" 条记录！");
            }
            int inserted = insertDB(cakeVector, MyBakingContract.CakesEntry.CONTENT_URI);
            Log.i(DEBUG_TAG, "成功插入cakess 表 "+inserted+" 条记录！");

        } catch (JSONException e) {
            e.printStackTrace();
            Log.e(DEBUG_TAG,"初始化Json发送错误！");
        }
    }

    private int insertDB(Vector<ContentValues> vector, Uri contentUri) {
        int inserted = 0;
        if(vector.size()>0){
            ContentValues[] cvArray = new ContentValues[vector.size()];
            vector.toArray(cvArray);
            inserted = getContext().getContentResolver().bulkInsert(contentUri, cvArray);
        }
        updateWidgets();
        return inserted;
    }

    private void updateWidgets() {

        // Setting the package ensures that only components in our app will receive the broadcast
        Intent dataUpdatedIntent = new Intent(mContext, MainActivity.class);
        dataUpdatedIntent.setPackage(mContext.getPackageName());
        dataUpdatedIntent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
        mContext.sendBroadcast(dataUpdatedIntent);
    }

    private void notifyWeather(){

    }
    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
