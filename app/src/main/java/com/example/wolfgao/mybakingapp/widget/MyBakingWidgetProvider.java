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
    //public final static String ITEM_CLICK = "com.example.wolfgao.mybaking.widget.action.CLICK";
    public final static String EXTRA_LIST_ITEM_TEXT = "com.example.wolfgao.mybaking.widget.item_text";
    private static String tag = "appWidgetProvider";

    private void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) {
        Log.d(tag, "onUpdate function is working...");
        //创建一个RemoteView并设置他的adapter
        mRemoteViews = new RemoteViews(context.getPackageName(), R.layout.my_baking_widget);
        mRemoteViews.setEmptyView(R.id.widget_list, R.id.widget_empty);

        // 设置 ListView 的adapter:
        // (01) intent: 对应启动 ListViewService(RemoteViewsService) 的intent
        // (02) setRemoteAdapter: 设置 ListView 的适配器
        Intent adapter = new Intent(context, ListViewService.class);
        adapter.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        adapter.setData(Uri.parse(adapter.toUri(Intent.URI_INTENT_SCHEME)));
        mRemoteViews.setRemoteAdapter(appWidgetId, R.id.widget_list, adapter);

        //设置点击item。分为两部，第一步、为所有的item设置模板
        /**给每一个item设计一个点击事件，这里不需要，我们给整个widget设计一个，只要点击就回到主activity
         Intent clickIntent = new Intent(context, MyBakingWidgetProvider.class);
        clickIntent.setAction(ITM_CLICK);
        clickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntentTemplate = PendingIntent.getBroadcast(
                context, 0, clickIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //设置为模板PendingIntent,表示如果有相似的intent（模板），使用之并更新extras
        mRemoteViews.setPendingIntentTemplate(R.id.widget_list, pendingIntentTemplate);
        */

        //跳转的业务逻辑——跳到MainActivity
        Intent homeIntent = new Intent(context, MainActivity.class);
        //将Intent包装成一个PendingIntent
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, homeIntent, 0);
        //为控件增加一个点击事件：点击任何以一个item的text都会跳转到主界面
        mRemoteViews.setOnClickPendingIntent(R.id.widget_list_item, pendingIntent);

        // 部署到具体的widget
        appWidgetManager.updateAppWidget(appWidgetId, mRemoteViews);
    }

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

    public void onReceive(@NonNull Context context, @NonNull Intent intent){
        super.onReceive(context, intent);
        //获得broadcast过来Intent 的action作为过滤调节
        String action = intent.getAction();
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        if(action.equals(MyRecycleAdapter.APPWIDGET_UPDATE)){
            // Retrieve all of the Today widget ids: these are the widgets we need to update
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context,
                    MyBakingWidgetProvider.class));
            onUpdate(context, appWidgetManager, appWidgetIds);

        }/**
        else if(action.equals(ITEM_CLICK)){
            // 处理点击广播事件
            int widgetID = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            if (widgetID == AppWidgetManager.INVALID_APPWIDGET_ID){
                //无效的widget
                return;
            }
            //获得有效的事件
            String itemText = intent.getStringExtra(EXTRA_LIST_ITEM_TEXT);
            Toast.makeText(context, itemText, Toast.LENGTH_LONG).show();
        }*/
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

