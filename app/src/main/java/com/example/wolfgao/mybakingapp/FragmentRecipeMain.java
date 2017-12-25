package com.example.wolfgao.mybakingapp;

import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

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
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by gaochuang on 2017/10/27.
 */

public class FragmentRecipeMain extends Fragment {
    private RecyclerView mRecycleView;
    private List<Object> mList;
    private MyRecycleAdapter mRecycleAdapter;
    private int mResponseCode;
    private String DEBUG_TAG;
    private TextView mEmpty_View;
    private boolean mUseDetailFragment;
    private int mPosition = ListView.INVALID_POSITION;

    //用于反转或者resume时刻存取数据的标识
    private static final String SELECTED_KEY = "selected_position";

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
    private final static String RECIPEINGREDIENTS = "ingredients";
    private final static String RECIPESTEPS = "steps";
    private final static String RECIPEIMAGE = "image";
    public static final String STEP_ID = "id";
    public static final String STEP_SHORT = "shortDescription";
    private final static String STEP_DESC = "description";
    private final static String STEP_VIDEO = "videoURL";

    public static final String ACTION_DATA_UPDATED = "com.example.wolfgao.mybakingapp.ACTION_DATA_UPDATED";

    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
    }

    public FragmentRecipeMain() {
        super();
    }
    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setHasOptionsMenu(true);
        DEBUG_TAG = getActivity().getClass().getName();
    }

    /**
     * 创建真正的view
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_recipe_main, container, false);
        //RecyclerView提供了高度自由化定制的功能，比如：通过LayoutManager（布局管理器），控制item的显示方式；
        //通过ItemDecoration，控制item间的背景；通过ItemAnimator，控制动态增删item的动画；
        mRecycleView = (RecyclerView)rootView.findViewById(R.id.recycler_main_page);
        mEmpty_View = (TextView)rootView.findViewById(R.id.empty_view);
        initData();

        //设置Layout部分 step 1#
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecycleView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);

        //设置Adaptor部分， step 2#
        mRecycleAdapter = new MyRecycleAdapter(getContext(), mList);
        mRecycleView.setAdapter(mRecycleAdapter);

        mRecycleAdapter.setOnItemClick(new MyRecycleAdapter.OnItemClick(){

            Intent intent = new Intent(getActivity().getApplicationContext(), DetailActivity.class );
            @Override
            public void onClick(int position) {
                if(mRecycleAdapter.getItemCount() != 0){
                    intent.putExtra("recipe_detail", mRecycleAdapter.getItem(position).toString());
                }
                //启动DetailActivity
                startActivity(intent);
                mPosition = position;
            }
        });

        //增加分割线step 3#
        mRecycleView.addItemDecoration(new android.support.v7.widget.DividerItemDecoration(getContext(),OrientationHelper.VERTICAL));

        //Set 删除动画 step 4#
        mRecycleView.setItemAnimator(new DefaultItemAnimator());

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never actually *lost*.
        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }

        //如果是横屏启动，默认加载第一个元素
        if(mUseDetailFragment){
            // In two-pane mode, show the detail view in this activity by adding or replacing the detail
            // fragment using a fragment transaction.
            if (savedInstanceState == null) {
                DetailFragment detailFragment = (DetailFragment)getActivity().getSupportFragmentManager()
                        .findFragmentById(R.id.recipe_detail_container);
                detailFragment.setDetailData(mRecycleAdapter.getItem(0).toString());
            }
        }

        mRecycleAdapter.setTwoPane(mUseDetailFragment);

        return rootView;
    }


    //作为公共接口来实现初始化，为widgets数据所用
    public void initData() {
        mList = new ArrayList<Object>();

        GetRecipeData recipeData = new GetRecipeData();
        recipeData.execute();
    }

    public void setTwoPane(boolean useDetailFragment) {
        mUseDetailFragment = useDetailFragment;
        if ( mRecycleAdapter!= null) {
            mRecycleAdapter.setTwoPane(mUseDetailFragment);
        }
    }
    /**
     *
     */
    private void updateEmptyView() {
        int message = R.string.empty_recipe_list;
        //Check if network is okay
        if (!Utility.checkNetworkStatus(getContext()) ) {
            message = R.string.empty_recipe_list_no_network;
            mEmpty_View.setText(message);
            mEmpty_View.setVisibility(View.VISIBLE);
            return;
        }
        //check if finished adaptor inilization.
        if(mRecycleAdapter.getItemCount() == 0){
            //Network is okay,but may not get data from server, so need check the response code.
            if (mResponseCode != 200) { //200 okay
                switch (mResponseCode) {
                    case 404: //Not Found 无法找到指定位置的资源,这也是一个常用的应答
                        message = R.string.empty_recipe_list_not_found;
                        break;
                    case 403: //Forbidden 资源不可用,服务器理解客户的请求，但拒绝处理它。
                        message = R.string.empty_recipe_list_forbidden;
                        break;
                    case 408: //Request Timeout 在服务器许可的等待时间内，客户一直没有发出任何请求。
                        message = R.string.empty_recipe_list_timeout;
                        break;
                    case 500: //Internal Server Error 服务器遇到了意料不到的情况，不能完成客户的请求。
                        message = R.string.empty_recipe_list_server_error;
                        break;
                    default: //other values，服务器不知道发生什么原因，我们都归结为不可获知的错误
                        message = R.string.empty_recipe_list_unknown;
                        Log.e(DEBUG_TAG, "链接server失败，不可获知的原因，response code：" + mResponseCode);
                        break;
                }
                mEmpty_View.setText(message);
                mEmpty_View.setVisibility(View.VISIBLE);
            }
        }
        else{
                mEmpty_View.setVisibility(View.GONE);
        }
    }

    /**
     * Back to this screen, need to restore the data and finish the UI
     */
    @Override
    public void onResume() {

        super.onResume();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        // When tablets rotate, the currently selected list item needs to be saved.
        // When no item is selected, mPosition will be set to Listview.INVALID_POSITION,
        // so check for that before storing.
        if (mPosition != ListView.INVALID_POSITION) {
            outState.putInt(SELECTED_KEY, mPosition);
        }
        super.onSaveInstanceState(outState);
    }
    /**
     * 此处增加这部分代码主要是有时候AsyncTask只会执行一次，第一次失败后，不会在此执行
     * 参见文章：
     * http://blog.csdn.net/hitlion2008/article/details/7983449
     */
    private static ExecutorService SINGLE_TASK_EXECUTOR;
    private static ExecutorService LIMITED_TASK_EXECUTOR;
    private static ExecutorService FULL_TASK_EXECUTOR;

    static {
        SINGLE_TASK_EXECUTOR = (ExecutorService) Executors.newSingleThreadExecutor();
        LIMITED_TASK_EXECUTOR = (ExecutorService) Executors.newFixedThreadPool(5);
        FULL_TASK_EXECUTOR = (ExecutorService) Executors.newCachedThreadPool();
    };
    /**
     * 私有类，开启background线程开始干活，由于只是get，不需要上传参数，因此前两个参数都是void
     * 返回一个Json String进行处理
     */
    private class GetRecipeData extends AsyncTask<Void, Void, String> {

        private String serverURL;
        private String RecipeJson = null;

        //onPreExecute方法用于在执行后台任务前做一些UI操作
        @Override
        protected void onPreExecute() {
            //Can't open the website provided by Udacity, so I created a website myself to provide Json file.
            //https://d17h27t6h515a5.cloudfront.net/topher/2017/May/59121517_baking/baking.json
            String serverIP = Utility.getServerIP(getActivity().getApplicationContext(), "config.xml", "ip");
            String filePath = Utility.getServerIP(getActivity().getApplicationContext(), "config.xml", "filePath");
            serverURL = serverIP + filePath;
            Log.i(DEBUG_TAG, "onPreExecute() called，server IP is "+serverIP+"; The file path is " + filePath);
        }

        @Override
        protected void onPostExecute(String JsonStr) {
            String id;
            String cakeName;
            String image;
            String ingredients;


            Log.i(DEBUG_TAG, "onPostExecute() called");
            if (JsonStr == null) {
                Log.e(DEBUG_TAG,"Failed to get Json string from server.");
            }
            else {
                try {
                    RecipeJsonData recipeJsonData = new RecipeJsonData(JsonStr);
                    JSONArray cakeArray = recipeJsonData.getObjectArray();

                    int num = cakeArray.length();
                    // Insert the new weather information into the database
                    Vector<ContentValues> cakeVector = new Vector<ContentValues>(cakeArray.length());

                    for (int i = 0; i < num; i++) {
                        JSONObject cake = cakeArray.getJSONObject(i);
                        id = cake.getString(RECIPEID);
                        cakeName = cake.getString(RECIPENAME); //using cake name as step id to direct to steps table.
                        ingredients = cake.getString(RECIPEINGREDIENTS);
                        image = cake.getString(RECIPEIMAGE);
                        JSONArray stepArray = cake.getJSONArray(RECIPESTEPS);

                        ContentValues cakeValues = new ContentValues();
                        cakeValues.put(MyBakingContract.CakesEntry.COLUMN_CAK_KEY, id);
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
                            stepValues.put(MyBakingContract.StepsEntry.COLUMN_CAKE_NAME,cakeName);
                            stepValues.put(MyBakingContract.StepsEntry.COLUMN_STEP_NO,step_no);
                            stepValues.put(MyBakingContract.StepsEntry.COLUMN_STEP_SHORT,step_short);
                            stepValues.put(MyBakingContract.StepsEntry.COLUMN_STEP_DESC, step_desc);
                            stepValues.put(MyBakingContract.StepsEntry.COLUMN_STEP_VIDEO,step_video);

                            stepVector.add(stepValues);
                            //插入数据库
                            int inserted = insertDB(stepVector, MyBakingContract.StepsEntry.CONTENT_URI);
                            Log.i(DEBUG_TAG, "成功插入steps 表 "+inserted+" 条记录！");
                        }

                        int inserted = insertDB(cakeVector, MyBakingContract.CakesEntry.CONTENT_URI);
                        Log.i(DEBUG_TAG, "成功插入cakess 表 "+inserted+" 条记录！");
                        updateWidgets();
                        mRecycleAdapter.addItem(cakeArray.get(i), i);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.e(DEBUG_TAG,"初始化Json发送错误！");
                }
                // This will only happen if there was an error getting or parsing the forecast.
            }
            updateEmptyView();
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
            Context context = getContext();
            // Setting the package ensures that only components in our app will receive the broadcast
            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED)
                    .setPackage(context.getPackageName());
            context.sendBroadcast(dataUpdatedIntent);
        }

        @Override
        protected String doInBackground(Void... params) {
            //获取SharedPreference的值，传入参数进行解析
            HttpURLConnection conn = null;
            InputStream is = null;
            BufferedReader reader = null;

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
                mResponseCode = conn.getResponseCode();
                Log.d(DEBUG_TAG, "The response code is: " + mResponseCode);

                if (mResponseCode == 200) {//200 status_ok
                    is = conn.getInputStream();
                    if (is == null) {
                        //do nothing, no any return data
                        RecipeJson = null;
                        return null;
                    }
                    //we get data from web site, but need convert them to Jsonstring
                    reader = new BufferedReader(new InputStreamReader(is));
                    String line;
                    StringBuffer buffer = new StringBuffer();

                    while ((line = reader.readLine()) != null) {
                        buffer.append(line + "\n");
                    }
                    if (buffer.length() == 0) {
                        RecipeJson = null;
                        return null;
                    }
                    RecipeJson = buffer.toString();
                    //Print returned Json information.
                    Log.i(DEBUG_TAG, RecipeJson);
                    // Makes sure that the InputStream is closed after the app finished using it.
                    is.close();
                }
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                RecipeJson = null;
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
            return RecipeJson;
        }

    }
}
