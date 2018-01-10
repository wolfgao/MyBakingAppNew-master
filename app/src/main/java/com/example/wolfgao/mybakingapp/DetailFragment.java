package com.example.wolfgao.mybakingapp;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.wolfgao.mybakingapp.data.MyBakingContract;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import java.util.Vector;

/**
 * Created by gaochuang on 2017/11/7.
 */

public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{

    public static final String TITLE = "cake_name";
    //private RecyclerView mDetailRecycleView;
    //private DetailRecyclerAdapter mDetailRecyclerAdapter;
    static final String DETAIL_URI = "recipe_detail";

    private Uri mUri;
    private String mTitle;
    private TextView mStepShort;
    private TextView mStepDesc;
    private SimpleExoPlayerView simpleExoPlayerView;
    private Button prevButton;
    private Button nextButton;
    private MyPlayer myPlayer;

    private String DEBUG_TAG;
    private int mStep_No = 0;
    private ViewGroup.LayoutParams mLayoutParams;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MyBakingContract.StepsEntry.TABLE_NAME + "." + MyBakingContract.StepsEntry._ID,
            MyBakingContract.StepsEntry.COLUMN_CAKE_KEY,
            MyBakingContract.StepsEntry.COLUMN_STEP_NO,
            MyBakingContract.StepsEntry.COLUMN_STEP_SHORT,
            MyBakingContract.StepsEntry.COLUMN_STEP_DESC,
            MyBakingContract.StepsEntry.COLUMN_STEP_VIDEO,
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


    //Constructor
    public DetailFragment(){
        super();
        setHasOptionsMenu(true);
    }

    public void setTitle() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getContext());
        mTitle = sp.getString(TITLE, getContext().getString(R.string.app_name));
        getActivity().setTitle(mTitle);
    }

    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setHasOptionsMenu(true);
        DEBUG_TAG = getActivity().getClass().getName();
        setTitle();
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
        mStepShort = (TextView) rootView.findViewById(R.id.step_short_desc);
        mStepDesc = (TextView) rootView.findViewById(R.id.step_desc);
        prevButton = (Button) rootView.findViewById(R.id.prev_button);
        nextButton = (Button) rootView.findViewById(R.id.next_button);
        myPlayer = new MyPlayer(getContext(),rootView);
        simpleExoPlayerView = (SimpleExoPlayerView)rootView.findViewById(R.id.step_video);

        /**
        // 不使用 recyclerview来呈现detail
        // mDetailRecycleView = (RecyclerView) rootView.findViewById(R.id.recipe_detail_recyclerview);
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
         */
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
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {

        final int steps_total = data.getCount();
        final Vector<ContentValues> stepVector = new Vector<ContentValues>(steps_total);

        int i = 0;
        if(!data.moveToFirst()) return;

        do {
            ContentValues stepValues = new ContentValues();
            stepValues.put(MyBakingContract.StepsEntry.COLUMN_STEP_NO,data.getString(DetailFragment.COL_STEPS_NO));
            stepValues.put(MyBakingContract.StepsEntry.COLUMN_STEP_SHORT,data.getString(DetailFragment.COL_STEPS_SHORT));
            stepValues.put(MyBakingContract.StepsEntry.COLUMN_STEP_DESC, data.getString(DetailFragment.COL_STEPS_DESC));
            stepValues.put(MyBakingContract.StepsEntry.COLUMN_STEP_VIDEO,data.getString(DetailFragment.COL_STEPS_VIDEO));
            i++;
            stepVector.add(stepValues);
        }
        while (data.moveToNext());

        //默认给各个view赋值第一条记录
        mStep_No =  stepVector.get(0).getAsInteger(MyBakingContract.StepsEntry.COLUMN_STEP_NO).intValue();
        updateView(mStep_No, stepVector);

        if(mStep_No == 0){
            prevButton.setVisibility(View.GONE);
        }

        prevButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //判断next是否gone，如果是复原
                if(nextButton.getVisibility() == View.GONE)
                    nextButton.setVisibility(View.VISIBLE);
                mStep_No--;
                updateView(mStep_No,stepVector);
                if(mStep_No == 0){
                    prevButton.setVisibility(View.GONE);
                }
            }
        });

        nextButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                //判断prevButton是否gone,如果是复原
                if(prevButton.getVisibility() == View.GONE)
                    prevButton.setVisibility(View.VISIBLE);
                if (mStep_No < steps_total-1) {//index从0开始
                    mStep_No++;
                    updateView(mStep_No, stepVector);
                }
                else {
                    nextButton.setVisibility(View.GONE);
                    Toast.makeText(getContext(),"No more steps", Toast.LENGTH_SHORT);
                }
            }
        });

    }

    private void updateView(int index, Vector<ContentValues> steps) {
        mStepShort.setText(steps.get(index).get(MyBakingContract.StepsEntry.COLUMN_STEP_NO)
                + "    " +
                steps.get(index).get(MyBakingContract.StepsEntry.COLUMN_STEP_SHORT));
        mStepDesc.setText(steps.get(index).getAsString(MyBakingContract.StepsEntry.COLUMN_STEP_DESC));
        simpleExoPlayerView.setVisibility(View.VISIBLE);
        simpleExoPlayerView.requestFocus();

        myPlayer.setIntent(steps.get(index).getAsString(MyBakingContract.StepsEntry.COLUMN_STEP_VIDEO));
        myPlayer.startPlay();
    }

    /**
     * 当Loader们的数据被重置的时候将会调用onLoadReset。该方法让你可以从就的数据中移除不再有用的数据。
     * 这里我们主要是点击上一步，下一步button
     * @param loader
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
