package com.example.wolfgao.mybakingapp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.exoplayer2.ui.SimpleExoPlayerView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

/**
 * Created by gaochuang on 2017/11/8.
 */

class DetailRecyclerAdapter extends RecyclerView.Adapter<DetailRecyclerAdapter.DetailRecycleAdapterViewHolder> {

    private final String STEP_DESC = "description";
    private final String STEP_SHORT = "shortDescription";
    private final String STEP_VIDEO = "videoURL";

    private List<String> mData;
    private int mLen = 0;
    private Context mContext;
    private String mUrl = null;
    private LayoutInflater mInflater = null;

    public static final String PREFER_EXTENSION_DECODERS = "prefer_extension_decoders";
    public static final String ACTION_VIEW = "com.example.wolfgao.mybakingapp.action.VIEW";
    public String DEBUG_TAG;

    /**
     * Default constructor, fragment will call this class to make all data available.
     *
     * @param context get this activity context from detail activity
     * @param list    get this list from parent fragment.
     */
    public DetailRecyclerAdapter(Context context, List<String> list) {
        super();
        mData = list;
        mLen = list.size();
        mContext = context;
        DEBUG_TAG = context.getClass().getName();
        mInflater = LayoutInflater.from(context);
    }

    @Override
    public DetailRecycleAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutID = R.layout.recipe_detail_card;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutID, parent, false);
        view.setFocusable(true);

        return new DetailRecycleAdapterViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mData == null ? 0 : mData.size();
    }

    public String getItem(int position) {
        return mData.get(position);
    }

    public void addItem(String s, int position) {
        this.mData.add(s);
        this.mLen = mData.size();
        notifyDataSetChanged();
        notifyItemChanged(position);
    }

    /**
     * 主要的实现每个item UI的方法
     *
     * @param holder
     * @param position
     */
    @Override
    public void onBindViewHolder(final DetailRecycleAdapterViewHolder holder, final int position) {
        String recipeDetailStr = mData.get(position);
        String step_short_desc = null;
        String step_desc = null;
        try {
            JSONObject recipeDetailData = new JSONObject(recipeDetailStr);
            step_short_desc = recipeDetailData.getString(STEP_SHORT);
            step_desc = recipeDetailData.getString(STEP_DESC);
            mUrl = recipeDetailData.getString(STEP_VIDEO);

        } catch (JSONException e) {
            Log.e(DEBUG_TAG, e.getMessage());
        }
        holder.tv_step_short.setText(step_short_desc);
        holder.tv_step_desc.setText(step_desc);
        holder.simpleExoPlayerView.setVisibility(View.VISIBLE);
        holder.simpleExoPlayerView.requestFocus();

        holder.myPlayer.setIntent(mUrl);
        holder.myPlayer.startPlay();
    }

    /**
     * 这是一个adaptor内部类，主要是为了实现这个adaptor的UI展现。同时如果用户点击了任何一个item，生成新的activity
     * 同时在这个类里面利用ExoPlayer的库实现视频播放。
     */
    public class DetailRecycleAdapterViewHolder extends RecyclerView.ViewHolder {

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
        public DetailRecycleAdapterViewHolder(View itemView) {
            super(itemView);
            //初始化各个子视图
            tv_step_short = (TextView) itemView.findViewById(R.id.step_short_desc);
            tv_step_desc = (TextView) itemView.findViewById(R.id.step_desc);
            //不想在此用一个layout file去生成一个新的窗口，因此用addContentView的方法在原有窗体上加载
            simpleExoPlayerView = (SimpleExoPlayerView) itemView.findViewById(R.id.step_video);
            debugRootView = (LinearLayout) itemView.findViewById(R.id.controls_root);
            retryButton = (Button) itemView.findViewById(R.id.retry_button);
            //Initilize myPlayer
            myPlayer = new MyPlayer(mContext,itemView);
        }
    }
}
