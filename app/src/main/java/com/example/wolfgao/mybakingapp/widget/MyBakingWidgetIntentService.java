package com.example.wolfgao.mybakingapp.widget;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.widget.RemoteViews;

import com.example.wolfgao.mybakingapp.MainActivity;
import com.example.wolfgao.mybakingapp.R;
import com.example.wolfgao.mybakingapp.data.MyBakingContract;

/**
 * Created by gaochuang on 2017/12/18.
 */

public class MyBakingWidgetIntentService extends IntentService {
    private static final String[] BAKING_COLUMNS = {
            MyBakingContract.CakesEntry.COLUMN_CAK_KEY,
            MyBakingContract.CakesEntry.COLUMN_CAK_NAME
    };

    //These indexes must match the projection
    private static final int INDEX_CAKE_ID = 0;
    private static final int INDEX_CAKE_NAME = 1;

    public MyBakingWidgetIntentService(){ super( "MyBakingWidgetIntentService");}

    protected void onHandleIntent(@NonNull Intent intent){
        // Retrieve all names info to to update
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(this,
                MyBakingWidget.class));

        //Get names from the ContentProvider
        Uri contentUri = MyBakingContract.CakesEntry.CONTENT_URI;
        Cursor data = getContentResolver().query(contentUri,BAKING_COLUMNS,null,null, null);
        if (data == null){
            return;
        }
        //移到第一行
        if (!data.moveToFirst()) {
            data.close();
            return;
        }

        String cakeId = data.getString(INDEX_CAKE_ID);
        String cakeName = data.getString(INDEX_CAKE_NAME);
        data.close();

        for (int appWidgetId : appWidgetIds){
            int layoutId = R.layout.my_baking_widget;
            RemoteViews views = new RemoteViews(getPackageName(), layoutId);
            // Add data to the remote views one by one
            views.setTextViewText(R.id.widget_cake_id, cakeId);
            views.setTextViewText(R.id.widget_cake_name,cakeName);

            // Create an Intent to launch MainActivity
            Intent launchIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, launchIntent, 0);
            views.setOnClickPendingIntent(R.id.widget, pendingIntent);

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }

    }
}
