package com.example.wolfgao.mybakingapp.widget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.example.wolfgao.mybakingapp.R;
import com.example.wolfgao.mybakingapp.data.MyBakingContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaochuang on 2018/1/5.
 * 参见文章：
 * https://www.cnblogs.com/joy99/p/6346829.html
 * 　1.2 RemoteViewsService
 　　RemoteViewsService，是管理RemoteViews的服务。一般，当AppWidget 中包含 GridView、ListView、StackView
 等集合视图时，才需要使用RemoteViewsService来进行更新、管理。RemoteViewsService 更新集合视图的一般步骤是：
 　(1) 通过 setRemoteAdapter() 方法来设置 RemoteViews 对应 RemoteViewsService 。
 　(2) 之后在 RemoteViewsService 中，实现 RemoteViewsFactory 接口。然后，在 RemoteViewsFactory 接口中对集
 合视图的各个子项进行设置，例如 ListView 中的每一Item。

 　　1.3 RemoteViewsFactory
 　　通过RemoteViewsService中的介绍，我们知道RemoteViewsService是通过 RemoteViewsFactory来具体管理layout中
 集合视图的，RemoteViewsFactory是RemoteViewsService中的一个内部接口。RemoteViewsFactory提供了一系列的方法管理
 集合视图中的每一项。例如：
 　　RemoteViews getViewAt(int position)
 　　通过getViewAt()来获取“集合视图”中的第position项的视图，视图是以RemoteViews的对象返回的。
 　　int getCount()
 　　通过getCount()来获取“集合视图”中所有子项的总数。
 */

public class ListViewService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        Log.d("ListViewService", Thread.currentThread().getName()); //main
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    @Override
    public void onStart(Intent intent, int startId){
        super.onCreate();
    }
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    class ListRemoteViewsFactory implements RemoteViewsFactory {

        private final String[] BAKING_COLUMNS = {
                MyBakingContract.CakesEntry.COLUMN_CAK_KEY,
                MyBakingContract.CakesEntry.COLUMN_CAK_NAME
        };

        //These indexes must match the projection
        private static final int INDEX_CAKE_ID = 0;
        private static final int INDEX_CAKE_NAME = 1;
        private String tag = "ListRemoteViewsFactory";
        private Context mContext;
        private Cursor mCursor;
        private int mAppWidgetId;
        private List<String> mList = new ArrayList<String>();

        public ListRemoteViewsFactory(Context context, Intent intent) {
            Log.d(tag,"ListRemoteViewsFactory constructed");
            mContext = context;
            //Get names from the ContentProvider
            Uri contentUri = MyBakingContract.CakesEntry.CONTENT_URI;
            mCursor = getContentResolver().query(contentUri,BAKING_COLUMNS,null,
                    null, null);
            mAppWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);

            if(Looper.myLooper() == null){
                Looper.prepare();
            }
        }

        @Override
        public void onCreate() {
            if (mCursor == null){
                return;
            }
            //移到第一行
            if (!mCursor.moveToFirst()) {
                mCursor.close();
                return;
            }
            do {
                mList.add(mCursor.getString(INDEX_CAKE_ID) +
                "    " + mCursor.getString(INDEX_CAKE_NAME));
            }while (mCursor.moveToNext());
        }

        @Override
        public void onDataSetChanged() {
        }

        @Override
        public void onDestroy() {
            mCursor.close();
            mList.clear();
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position < 0 || position >= mList.size())
                return null;
            String content = mList.get(position);

            final RemoteViews views = new RemoteViews(mContext.getPackageName(),
                    R.layout.widget_list_item);
            views.setTextViewText(R.id.widget_cake, content);

            //在这里设计每一个item的点击事件，可以在OnReceive那里接收
            Intent intent = new Intent();
            intent.putExtra(MyBakingWidgetProvider.EXTRA_LIST_ITEM_TEXT, position);
            views.setOnClickFillInIntent(R.id.widget_cake, intent);

            return views;
        }

        /* 在更新界面的时候如果耗时就会显示 正在加载... 的默认字样，但是你可以更改这个界面
         * 如果返回null 显示默认界面
         * 否则 加载自定义的，返回RemoteViews
         */
        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }
    }
}
