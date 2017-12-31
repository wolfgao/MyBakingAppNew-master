package com.example.wolfgao.mybakingapp;

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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.wolfgao.mybakingapp.data.MyBakingContract;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

/**
 * Created by gaochuang on 2017/11/7.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String TITLE = "cake_name";
    private RecyclerView mDetailRecycleView;
    private DetailRecyclerAdapter mDetailRecyclerAdapter;
    static final String DETAIL_URI = "recipe_detail";

    private Uri mUri;
    private String mTitle;

    private String DEBUG_TAG;

    private ViewGroup.LayoutParams mLayoutParams;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MyBakingContract.StepsEntry.TABLE_NAME + "." + MyBakingContract.StepsEntry._ID,
            MyBakingContract.StepsEntry.COLUMN_CAKE_KEY,
            MyBakingContract.StepsEntry.COLUMN_STEP_NO,
            MyBakingContract.StepsEntry.COLUMN_STEP_SHORT,
            MyBakingContract.StepsEntry.COLUMN_STEP_DESC,
            MyBakingContract.StepsEntry.COLUMN_STEP_VIDEO,
            MyBakingContract.CakesEntry.COLUMN_CAK_NAME
            // This works because the WeatherProvider returns location data joined with
            // weather data, even though they're stored in two different tables.

    };

    // These indices are tied to DETAIL_COLUMNS.  If DETAIL_COLUMNS changes, these
    // must change.
    public static final int COL_STEPS_ID = 0;
    public static final int COL_STEPS_CAKEID = 1; //new added
    public static final int COL_STEPS_NO = 2;
    public static final int COL_STEPS_SHORT = 3;
    public static final int COL_STEPS_DESC = 4;
    public static final int COL_STEPS_VIDEO = 5;
    public static final int COL_CAKES_NAME = 6;



    private TextView mStepShort;
    private TextView mStepDesc;
    private SimpleExoPlayerView simpleExoPlayerView;

    //Constructor
    public DetailFragment(){
        super();
        setHasOptionsMenu(true);
    }

    public void setTitle(String title) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        mTitle = sp.getString(TITLE, getString(R.string.app_name));
        getActivity().setTitle(mTitle);
    }

    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setHasOptionsMenu(true);
        DEBUG_TAG = getActivity().getClass().getName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //从父activity获取Uri
        //如果是空，说明是大屏，两个fragment，那么detail应该使用第一条记录
        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
        }

        //Step 1 初始化view
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mDetailRecycleView = (RecyclerView) rootView.findViewById(R.id.recipe_detail_recyclerview);
        //设置Layout部分 step 1#
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mDetailRecycleView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);

        //Step 2 设置adaptor
        mDetailRecyclerAdapter = new DetailRecyclerAdapter(getContext(), null,0);
        mDetailRecycleView.setAdapter(mDetailRecyclerAdapter);

        //增加分割线step 3#
        mDetailRecycleView.addItemDecoration(new android.support.v7.widget.DividerItemDecoration(getContext(),OrientationHelper.VERTICAL));

        //Set 删除动画 step 4#
        mDetailRecycleView.setItemAnimator(new DefaultItemAnimator());
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(null != mUri){
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        /**
        ViewParent vp = getView().getParent();
        if (vp instanceof CardView){
            ((View)vp).setVisibility(View.INVISIBLE);
        }
        */
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mDetailRecyclerAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mDetailRecyclerAdapter.swapCursor(null);
    }
}
