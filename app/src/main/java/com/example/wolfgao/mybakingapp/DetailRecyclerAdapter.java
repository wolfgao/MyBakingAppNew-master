package com.example.wolfgao.mybakingapp;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.wolfgao.mybakingapp.thirdLib.RecyclerViewCursorAdapter;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

/**
 * Created by gaochuang on 2017/11/8.
 */

class DetailRecyclerAdapter extends RecyclerViewCursorAdapter<DetailRecyclerAdapter.DetailViewHolder> {
    private Context mContext;
    private LayoutInflater mInflater = null;

    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";
    public static final String ACTION_VIEW = "com.example.wolfgao.mybakingapp.action.VIEW";
    public static final String ACTION_DATA_UPDATED = "com.example.wolfgao.mybakingapp.ACTION_DATA_UPDATED";


    public String DEBUG_TAG;

    /**
     * Default constructor, fragment will call this class to make all data available.
     */
    public DetailRecyclerAdapter(Context ctx, Cursor c, int flags) {
        super(ctx, c, flags);
        mContext = ctx;
        DEBUG_TAG = ctx.getClass().getName();
        mInflater = LayoutInflater.from(ctx);
    }

    @Override
    public DetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutID = R.layout.recipe_detail_card;
        View view = mInflater.inflate(layoutID, parent, false);
        view.setFocusable(true);
        return new DetailViewHolder(view);
    }

    /**
     * 主要的实现每个item UI的方法
     * @param holder UI的持有者
     * @param cursor 数据的持有者
     */
    @Override
    public void onBindViewHolder(DetailViewHolder holder, Cursor cursor) {
        String step_no = cursor.getString(DetailFragment.COL_STEPS_NO);
        String step_short_desc = cursor.getString(DetailFragment.COL_STEPS_SHORT);
        String step_desc = cursor.getString(DetailFragment.COL_STEPS_DESC);
        String step_url = cursor.getString(DetailFragment.COL_STEPS_VIDEO);

        holder.tv_step_short.setText(step_no + "    " + step_short_desc);
        holder.tv_step_desc.setText(step_desc);
        holder.simpleExoPlayerView.setVisibility(View.VISIBLE);
        holder.simpleExoPlayerView.requestFocus();

        holder.myPlayer.setIntent(step_url);
        holder.myPlayer.startPlay();
    }

    @Override
    protected void onContentChanged() {

    }

    /**
     * 主要的实现每个item UI的方法

    /**
     * 这是一个adaptor内部类，主要是为了实现这个adaptor的UI展现。同时如果用户点击了任何一个item，生成新的activity
     * 同时在这个类里面利用ExoPlayer的库实现视频播放。
     */
    public class DetailViewHolder extends RecyclerView.ViewHolder {

        private TextView tv_step_short;
        private TextView tv_step_desc;
        private Button retryButton;
        private LinearLayout debugRootView;
        private SimpleExoPlayerView simpleExoPlayerView;
        private MyPlayer myPlayer;
        /**
         * Constructor
         *
         * @param itemView
         */
        public DetailViewHolder(View itemView) {
            super(itemView);
            //初始化各个子视图
            tv_step_short = (TextView) itemView.findViewById(R.id.step_short_desc);
            tv_step_desc = (TextView) itemView.findViewById(R.id.step_desc);
            simpleExoPlayerView = (SimpleExoPlayerView) itemView.findViewById(R.id.step_video);
            debugRootView = (LinearLayout) itemView.findViewById(R.id.controls_root);
            retryButton = (Button) itemView.findViewById(R.id.retry_button);
            //Initilize myPlayer
            myPlayer = new MyPlayer(mContext,itemView);
        }
    }
}
