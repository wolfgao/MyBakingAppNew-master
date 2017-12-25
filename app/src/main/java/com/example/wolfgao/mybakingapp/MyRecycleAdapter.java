package com.example.wolfgao.mybakingapp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * Created by gaochuang on 2017/10/22.
 */

public class MyRecycleAdapter extends RecyclerView.Adapter<MyRecycleAdapter.MyRecycleAdapterViewHolder> {
    public static final String ACTION_DATA_UPDATED = "com.example.wolfgao.mybakingapp.ACTION_DATA_UPDATED";
    private List<Object> mData;
    private LayoutInflater inflater;
    final private Context mContext;

    public String DEBUG_TAG;
    private boolean mUseDetailFragment;

    public void setTwoPane(boolean useDetailFragment) {
        mUseDetailFragment = useDetailFragment;
    }

    /** 定义接口*/
    interface OnItemClick{
        void onClick(int position);
    }
    //定义click事件
    private OnItemClick onItemClick;

    /** 对外提供方法，接收示例对象*/
    public void setOnItemClick(OnItemClick onItemClick){
        this.onItemClick = onItemClick;
    }

    /**
     * default constructor
     * @param ctx
     * @param mList
     */
    public MyRecycleAdapter(Context ctx, List<Object> mList) {
        this.mContext = ctx;
        this.mData= mList;
        inflater=LayoutInflater.from(mContext);
        DEBUG_TAG = ctx.getClass().getName();
    }

    @Override
    public MyRecycleAdapterViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutID =R.layout.recipe_card;
        View view = LayoutInflater.from(parent.getContext()).inflate(layoutID, parent, false);
        view.setFocusable(true);
        return new MyRecycleAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final MyRecycleAdapterViewHolder holder, final int position) {

        JSONObject recipeData = (JSONObject) mData.get(position);
        String recipeName = RecipeJsonData.getRecipeName(recipeData);
        //String recipeDesc = RecipeJsonData.getRecipeDesc(recipeData);
        holder.tv_name.setText(recipeName);
        //holder.tv_desc.setText(recipeDesc);
        String imageUrl = RecipeJsonData.getRecipeImageUrl(recipeData);

        //获取图片资源
        InputStream is = null;
        Bitmap bitmap = null;
        if (imageUrl == null || imageUrl.equals("")){
            /**
             * 如果从网络获取的路径为空，不得不从本地获取文件，我把图片文件路径放在assets路径下面
             */
            try {
                is = mContext.getAssets().open(recipeName+".jpg");
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "获取本地图片失败！"+ "\n" + e.getMessage());
            }
        }
        else { //加载网络图片
            try {
                HttpURLConnection conn = (HttpURLConnection)(new URL(imageUrl)).openConnection();
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
            } catch (MalformedURLException e) {
                Log.e(DEBUG_TAG,"网络URL不正确！"+"\n" + e.getMessage());
            } catch (IOException e) {
                Log.e(DEBUG_TAG, "加载网络图片错误！" +"\n" + e.getMessage());
            }
        }

        holder.iv.setImageBitmap(bitmap);

        if(onItemClick != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick.onClick(holder.getAdapterPosition()); //使用接口回调的方法将参数传递出来
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return mData==null? 0 :mData.size();
    }

    public Object getItem(int position){
        return mData.get(position);
    }


    public void addItem(Object o, int position){
        this.mData.add(o);
        notifyDataSetChanged();
        notifyItemChanged(position);
    }

    /**
     * Cache of the children views for a forecast list item.
     */
    public class MyRecycleAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView tv_name;
        //final TextView tv_desc;
        final ImageView iv;
        //final TextView tv_desc; 将这部分移到detail view
        /**
         * Constructor
         * @param view
         */
        public MyRecycleAdapterViewHolder(View view) {
            super(view);
            tv_name = (TextView) view.findViewById(R.id.recipe_name);
            //tv_desc=(TextView) view.findViewById(R.id.text_short_desc);
            iv=(ImageView) view.findViewById(R.id.picture);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
        }
    }
}
