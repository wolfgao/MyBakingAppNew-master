package com.example.wolfgao.mybakingapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
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

import java.util.ArrayList;
import java.util.List;

/**
 * 通过LoaderManager来完成Activity和Fragement之间的异步数据查询和同步：
 * LoaderManager：一个抽像类，关联到一个Activity或Fragment，管理一个或多个装载器的实例。这帮助一个应用管理那些与Activity或Fragment的生命周期相关的长时间运行的的操作。
 * 最常见的方式是与一个CursorLoader一起使用，然而应用是可以随便写它们自己的装载器以加载其它类型的数据。
 每个activity或fragment只有一个LoaderManager。但是一个LoaderManager可以拥有多个装载器。

 LoaderManager.LoaderCallbacks： 一个用于客户端与LoaderManager交互的回调接口。例如，你使用回调方法onCreateLoader()来创建一个新的装载器。
 * 参见 http://blog.csdn.net/yangdeli888/article/details/7911862
 * Created by gaochuang on 2017/10/27.
 */

public class FragmentRecipeMain extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, SharedPreferences.OnSharedPreferenceChangeListener{

    private RecyclerView mRecycleView;
    private List<Object> mList = new ArrayList<Object>();
    private MyRecycleAdapter mRecycleAdapter;
    private TextView mEmpty_View;
    private boolean mUseDetailFragment;
    private int mPosition = ListView.INVALID_POSITION;

    //用于反转或者resume时刻存取数据的标识
    private static final String SELECTED_KEY = "selected_position";
    public static final String LOG_TAG = FragmentRecipeMain.class.getSimpleName();

    private static final int RECIPE_LOADER = 0;

    //In Recipe view we are only showing a small subset of the stored data, so just define the columns we need.
    private static final String[] RECIPE_COLUMNS = {
            MyBakingContract.CakesEntry.TABLE_NAME + "." + MyBakingContract.CakesEntry._ID,
            MyBakingContract.CakesEntry.COLUMN_CAK_KEY,
            MyBakingContract.CakesEntry.COLUMN_CAK_NAME,
            MyBakingContract.CakesEntry.COLUMN_CAK_IMG
    };

    //There indices are tied to RECIPE_COLUMNS, if RECIPE_COLUMNS changes, these must be changed
    static final int COL_CAKES_ID = 0;
    static final int COL_CAKES_KEY = 1;
    static final int COL_CAKES_NAME = 2;
    static final int COL_CAKES_IMG = 3;

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //This is called when a new load to be created.
        //To only show Cakes name and Cakes Ingredients, so filter the query to return Recipes for all
        Uri recipeForAllShowed = MyBakingContract.CakesEntry.CONTENT_URI;
        return new CursorLoader(getActivity(),
            recipeForAllShowed,
            RECIPE_COLUMNS,
                null,
                null,
                null
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }


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
    }

    @Override
    public void onResume() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);
        super.onResume();
    }

    @Override
    public void onPause() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.unregisterOnSharedPreferenceChangeListener(this);
        super.onPause();
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
            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
            int resCode = sp.getInt(getContext().getString(R.string.pref_server_status_key), 400);
            switch (resCode) {
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
                    Log.e(LOG_TAG, "链接server失败，不可获知的原因，response code：" + resCode);
                    break;
            }
            mEmpty_View.setText(message);
            mEmpty_View.setVisibility(View.VISIBLE);
        }
        else{
            mEmpty_View.setVisibility(View.GONE);
        }
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
}
