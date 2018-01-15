package com.example.wolfgao.mybakingapp.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import com.example.wolfgao.mybakingapp.DetailActivity;
import com.example.wolfgao.mybakingapp.MainActivity;
import com.example.wolfgao.mybakingapp.MyRecycleAdapter;
import com.example.wolfgao.mybakingapp.R;

/**
 * 本次项目评审有着硬性规定：用户可以选择任一菜谱显示在小部件上。，下面提供两种实现思路（当然，根据你自己的理解来实现的话就更棒了）：
 - 当用户添加小部件的时候，提供一个选择界面来让用户选择显示哪一个食谱信息；
 - 在App中提供一个设置界面来配置小部件的显示信息。
    - onEnable() ：当小部件第一次被添加到桌面时回调该方法，可添加多次，但只在第一次调用。对用广播的 Action为
                ACTION_APPWIDGET_ENABLE。
 　　- onUpdate():  当小部件被添加时或者每次小部件更新时都会调用一次该方法，配置文件中配置小部件的更新周期
                updatePeriodMillis，每次更新都会调用。对应广播 Action 为：ACTION_APPWIDGET_UPDATE和
                ACTION_APPWIDGET_RESTORED 。
 　　- onDisabled(): 当最后一个该类型的小部件从桌面移除时调用，对应的广播的 Action 为 ACTION_APPWIDGET_DISABLED。
 　　- onDeleted(): 每删除一个小部件就调用一次。对应的广播的 Action 为： ACTION_APPWIDGET_DELETED 。
 　　- onRestored(): 当小部件从备份中还原，或者恢复设置的时候，会调用，实际用的比较少。对应广播的 Action 为
                ACTION_APPWIDGET_RESTORED。
 　　- onAppWidgetOptionsChanged(): 当小部件布局发生更改的时候调用。对应广播的 Action 为
            ACTION_APPWIDGET_OPTIONS_CHANGED。
 */
public class MyBakingWidgetProvider extends AppWidgetProvider {

    private RemoteViews mRemoteViews;
    private Intent mAdapter;
    private Intent inputIntent;
    public final static String ITEM_CLICK = "com.example.wolfgao.mybaking.widget.action.CLICK";
    public final static String EXTRA_LIST_ITEM_POS = "com.example.wolfgao.mybaking.widget.item_pos";
    public final static String EXTRA_LIST_ITEM_TEXT = "com.example.wolfgao.mybaking.widget.item_text";
    private static String tag = "appWidgetProvider";

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.d(tag, "onUpdate function is working...");
        if (mRemoteViews == null)
            mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.my_baking_widget);
        mRemoteViews.setEmptyView(R.id.widget_list, R.id.widget_empty);
        // 设置 ListView 的adapter:
        // (01) intent: 对应启动 ListViewService(RemoteViewsService) 的intent
        // (02) setRemoteAdapter: 设置 ListView 的适配器
        //mAdapter = new Intent(context, ListViewService.class);
        //mAdapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //mAdapter.setData(Uri.parse(mAdapter.toUri(Intent.URI_INTENT_SCHEME)));
        //mRemoteViews.setRemoteAdapter(appWidgetId, R.id.widget_list, mAdapter);


        //跳转的业务逻辑——跳到MainActivity
        //Intent homeIntent = new Intent(context, MainActivity.class);
        //将Intent包装成一个PendingIntent
        //PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, homeIntent, 0);
        //点击title的text都会跳转到主界面
        //mRemoteViews.setOnClickPendingIntent(R.id.list_widget_title, pendingIntent);

        //也为了每一个item提高事件
        //Intent toastIntent = new Intent(context, MyBakingWidgetProvider.class);
        //toastIntent.setAction(MyBakingWidgetProvider.ITEM_CLICK);
        //toastIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        //mAdapter.setData(Uri.parse(mAdapter.toUri(Intent.URI_INTENT_SCHEME)));
        //PendingIntent toastPendingIntent = PendingIntent.getBroadcast(context, 0, toastIntent,
        //        PendingIntent.FLAG_UPDATE_CURRENT);
        //mRemoteViews.setPendingIntentTemplate(R.id.widget_list, toastPendingIntent);


        // 部署到具体的widget
        appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews);
    }

    /**
     * onUpdate()：是最重要的回调函数，根据 updatePeriodMillis 定义的定期刷新操作会调用该函数，此外当用户添加
     * Widget 时也会调用该函数，可以在这里进行必要的初始化操作。但如果在<appwidget-provider> 中声明了android:
     * configure 的 Activity，在用户添加 Widget 时，不会调用 onUpdate()，需要由 configure Activity去负责
     * 调用 AppWidgetManager.updateAppWidget() 完成 Widget 更新，后续的定时更新还是会继续调用 onUpdate()的。
     */

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them
        for(int appWidgetId:appWidgetIds){
            updateAppWidget(context, appWidgetManager,appWidgetId);
        }
        //super.onUpdate(context,appWidgetManager,appWidgetIds);
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
        super.onEnabled(context);
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Override
    public void onRestored(Context context,int[] oldAppWidgetIds,int[] newAppWidgetIds){
        // Enter relevant functionality for when the widget is restored
        super.onRestored(context,oldAppWidgetIds,newAppWidgetIds);
    }

    //如果从配置返回的intent带有extras，就会在这里完成widget的更新
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager,
                                          int appWidgetId, Bundle newExtras){
        super.onAppWidgetOptionsChanged(context,appWidgetManager,appWidgetId,newExtras);

        //更新title
        if (mRemoteViews == null)
            mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.my_baking_widget);
        mRemoteViews.setTextViewText(R.id.list_widget_title,
                SelectRecipeActivity.loadTitlePref(context,appWidgetId));

        //传值
        mAdapter = new Intent(context, ListViewService.class);
        mAdapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        mAdapter.putExtras(newExtras);
        mRemoteViews.setRemoteAdapter(appWidgetId, R.id.widget_list, mAdapter);

        //跳转的业务逻辑——跳到MainActivity
        Intent homeIntent = new Intent(context, MainActivity.class);
        //将Intent包装成一个PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, homeIntent, 0);
        //点击title的text都会跳转到主界面
        mRemoteViews.setOnClickPendingIntent(R.id.list_widget_title, pendingIntent);

        //也为了每一个item提高事件
        Intent itemIntent = new Intent(context, MyBakingWidgetProvider.class);
        itemIntent.setAction(MyBakingWidgetProvider.ITEM_CLICK);
        itemIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        mAdapter.setData(Uri.parse(mAdapter.toUri(Intent.URI_INTENT_SCHEME)));
        PendingIntent itemtPendingIntent = PendingIntent.getBroadcast(context, 0, itemIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setPendingIntentTemplate(R.id.widget_list, itemtPendingIntent);

        // 部署到具体的widget
        appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews);
    }

    public void onReceive(@NonNull Context context, @NonNull Intent intent){
        super.onReceive(context, intent);
        //获得broadcast过来Intent 的action作为过滤调节
        //初始化
        if (mRemoteViews == null)
            mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.my_baking_widget);
        String action = intent.getAction();
        //变成类变量
        this.inputIntent = intent;
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        if(action.equals(MyRecycleAdapter.APPWIDGET_UPDATE)){
            // Retrieve all of the Today widget ids: these are the widgets we need to update
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                    MyBakingWidgetProvider.class));
            onUpdate(context, appWidgetManager, appWidgetIds);

        }
        //Sent when the custom extras for an AppWidget change.
        else if(action.equals("android.appwidget.action.APPWIDGET_UPDATE_OPTIONS")){
            //触发AppWidgetProvider.onAppWidgetOptionsChanged()
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            onAppWidgetOptionsChanged(context,appWidgetManager,appWidgetId,intent.getExtras());
        }
        else if(action.equals(ITEM_CLICK)){
            // 处理点击广播事件
            int widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            int viewIndex = intent.getIntExtra(EXTRA_LIST_ITEM_TEXT,0);
            //跳转的业务逻辑——跳到DetailActivity
            Intent detailIntent = new Intent(context, DetailActivity.class);
            //将Intent包装成一个PendingIntent
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, detailIntent, 0);
            //
            mRemoteViews.setOnClickPendingIntent(R.id.widget_cake, pendingIntent);

            Toast.makeText(context, "Touch view at " + viewIndex, Toast.LENGTH_SHORT).show();
        }
        else if (AppWidgetManager.ACTION_APPWIDGET_DELETED.equals(action)) {
            Bundle extras = intent.getExtras();
            if (extras != null && extras.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID)) {
                final int appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                this.onDeleted(context, new int[] { appWidgetId });
            }
        } else if (AppWidgetManager.ACTION_APPWIDGET_ENABLED.equals(action)) {
            this.onEnabled(context);
        } else if (AppWidgetManager.ACTION_APPWIDGET_DISABLED.equals(action)) {
            this.onDisabled(context);
        } else if (AppWidgetManager.ACTION_APPWIDGET_RESTORED.equals(action)) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                int[] oldIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_OLD_IDS);
                int[] newIds = extras.getIntArray(AppWidgetManager.EXTRA_APPWIDGET_IDS);
                if (oldIds != null && oldIds.length > 0) {
                    this.onRestored(context, oldIds, newIds);
                    this.onUpdate(context, AppWidgetManager.getInstance(context), newIds);
                }
            }
        }

    }
}

