package com.example.wolfgao.mybakingapp.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RemoteViews;

import com.example.wolfgao.mybakingapp.R;
import com.example.wolfgao.mybakingapp.data.MyBakingContract;

import java.util.ArrayList;
import java.util.List;

/**此activity在用户选择创建widget时候就会弹出，选择配置
 * 参考了http://glgjing.github.io/blog/2015/11/05/android-kai-fa-zhi-app-widget-xiang-jie/
 * Created by gaochuang on 2018/1/11.
 */

public class SelectRecipeActivity extends Activity {
    public static final String PREFS_WIDGET_TITLE = "com.example.wolfgao.mybakingapp";
    private static final String PREF_PREFIX_KEY = "appwidget_";
    public static final String SELECTED_ITEM = "SELECTED_ITEMS";
    private static final String PREFS_WIDGET_LIST = "com.example.wolfgao.mybakingapp.selected_list";

    int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private EditText mAppWidgetTitle;
    private Button saveButton;
    private Button cancelButton;
    private ListView mRecipe_list_view;
    private List<ConfigureItem> mList = new ArrayList<ConfigureItem>();
    private ArrayList<String> mSelectList = new ArrayList<String>();
    private Cursor mCursor;

    private final String[] BAKING_COLUMNS = {
            MyBakingContract.CakesEntry.COLUMN_CAK_KEY,
            MyBakingContract.CakesEntry.COLUMN_CAK_NAME
    };

    //These indexes must match the projection
    private static final int INDEX_CAKE_ID = 0;
    private static final int INDEX_CAKE_NAME = 1;

    //defatult constructor
    public SelectRecipeActivity(){}

    @Override
    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        // 当您的配置活动首次打开时,将活动结果与EXTRA_APPWIDGET_ID一起设置为RESULT_CANCELED,
        // If you return RESULT_OK using Activity.setResult(), the AppWidget will be added, and you
        // will receive an ACTION_APPWIDGET_UPDATE broadcast for this AppWidget.
        // If you returnRESULT_CANCELED, the host will cancel the add and not display this AppWidget,
        // and you will receive a ACTION_APPWIDGET_DELETED broadcast.
        setResult(RESULT_CANCELED);

        setContentView(R.layout.app_widget_configure);
        mAppWidgetTitle = (EditText) findViewById(R.id.edit_changeTitle);
        saveButton = (Button)findViewById(R.id.saveButton);
        cancelButton = (Button)findViewById(R.id.cancelButton);

        mRecipe_list_view = (ListView)findViewById(R.id.widget_recipe_list);

        //1.从Launched Activity(主屏) 找到 Widget ID (保存在Launched Activity Intent extras 的EXTRA_APPWIDGET_ID)
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        //判断app widget ID是否为空.(App Widget 是否添加到Launched Activity)
        if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
            return;
        }
        //2. 执行你的 Widget 自定义配置逻辑
        //初始化list adapter并赋值给listview
        initData();
        RecipeAdapter recipeAdapter = new RecipeAdapter(getApplicationContext(), mList);
        mRecipe_list_view.setAdapter(recipeAdapter);
        mRecipe_list_view.setOnItemClickListener(new OnItemClickListener(){

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // 取得ViewHolder对象
                RecipeAdapter.ViewHolder viewHolder = (RecipeAdapter.ViewHolder) view.getTag();
                // 改变CheckBox的状态
                viewHolder.checkBox.toggle();
                // 将CheckBox的选中状况记录下来
                mList.get(position).setChecked(viewHolder.checkBox.isChecked());
                // 调整选定条目
                if (viewHolder.checkBox.isChecked() == true) {
                    mSelectList.add(mList.get(position).getItemName());
                }

            }
        });

        // 点击保存后就会传递数据到appwidget provider
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //1. 获取context
                final Context context = SelectRecipeActivity.this;
                //2.App Widget 配置逻辑,这边按需配置即可
                String widgetText = mAppWidgetTitle.getText().toString();

                //保存配置到SP中
                saveTitlePref(context, mAppWidgetId, widgetText);

                //3. 配置完 Widget 配置,拿到AppWidgetManager实例
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

                //4.通过调用updateAppWidget（int,RemoteViews）通过RemoteViews布局更新App Widget
                RemoteViews views = new RemoteViews(context.getPackageName(),
                        R.layout.my_baking_widget);
                appWidgetManager.updateAppWidget(mAppWidgetId, views);

                //5.结束 Activity,将设置的参数用 inent.putXX 传递出去(要做回显的可以保存一份到SP)
                Intent resultValue = new Intent();
                resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);

                //将list保存在sp中
                saveListPref(context,mAppWidgetId,mSelectList);

                setResult(RESULT_OK, resultValue);
                finish();

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        setResult(RESULT_CANCELED);
                        finish();
                        onDestroy();
                    }
                }
        );

        mAppWidgetTitle.setText(loadTitlePref(SelectRecipeActivity.this, mAppWidgetId));

    }




    //Activity 退出时候保存改变的值
    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    private void initData() {
        //Get names from the ContentProvider
        Uri contentUri = MyBakingContract.CakesEntry.CONTENT_URI;
        mCursor = getContentResolver().query(contentUri,BAKING_COLUMNS,null,
                null, null);
        if (mCursor == null){
            return;
        }
        //移到第一行
        if (!mCursor.moveToFirst()) {
            mCursor.close();
            return;
        }
        do {
            ConfigureItem configureItem = new ConfigureItem((mCursor.getString(INDEX_CAKE_ID) +
                    "    " + mCursor.getString(INDEX_CAKE_NAME)), false);
            mList.add(configureItem);
        }while (mCursor.moveToNext());
    }

    /**
     * 下面都是SP数据处理
     **/
    // Write the prefix to the SharedPreferences object for this widget
    public static void saveTitlePref(Context context, int appWidgetId, String text) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET_TITLE, 0).edit();
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, text);
        prefs.apply();
    }

    // Read the prefix from the SharedPreferences object for this widget.
    // If there is no preference saved, get the default from a resource
    public static String loadTitlePref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET_TITLE, 0);
        String titleValue = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (titleValue != null) {
            return titleValue;
        } else {
            return context.getString(R.string.appwidget_text);
        }
    }

    public static void deleteTitlePref(Context context, int appWidgetId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET_TITLE, 0).edit();
        prefs.remove(PREF_PREFIX_KEY + appWidgetId);
        prefs.apply();
    }

    public static void saveListPref(Context context, int appWidgetId, ArrayList<String> mSelectList) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_WIDGET_LIST, 0).edit();
        String list ="";
        for (int i = 0; i < mSelectList.size(); i++) {
            list +=mSelectList.get(i)+",";
        }
        prefs.putString(PREF_PREFIX_KEY + appWidgetId, list);
        prefs.apply();
    }

    public static String[] loadListPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_WIDGET_LIST, 0);
        String[] list = null;
        String selectedStr = prefs.getString(PREF_PREFIX_KEY + appWidgetId, null);
        if (selectedStr != null){
            list = selectedStr.trim().split(",");
        }
        return list;
    }

}
