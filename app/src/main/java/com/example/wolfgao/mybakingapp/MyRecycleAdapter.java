package com.example.wolfgao.mybakingapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.wolfgao.mybakingapp.thirdLib.RecyclerViewCursorAdapter;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by gaochuang on 2017/10/22.
 * Modified by Gaochuang on 2017/12/29.
 * In this update, using the third lib class RecyclerViewCursorAdapter will can be added by RecyclerView.
 * On the other hand, it can handle data as a CursorAdapter, which was supposed to be used in Listview only.
 */

public class MyRecycleAdapter extends RecyclerViewCursorAdapter<MyRecycleAdapter.RecipeViewHolder> {

    public static final String ACTION_DATA_UPDATED = "com.example.wolfgao.mybakingapp.ACTION_DATA_UPDATED";
    public static final String APPWIDGET_UPDATE = "android.appwidget.action.APPWIDGET_UPDATE";
    private LayoutInflater inflater;
    final private Context mContext;
    public String LOG_TAG;

    /** 定义接口*/
    public static interface OnItemClick{
        void onClick(int position);
    }
    //定义click事件
    private OnItemClick onItemClick = null;

    /** 对外提供方法，接收示例对象*/
    public void setOnItemClick(OnItemClick onItemClick){
        this.onItemClick = onItemClick;
    }

    /**
     * @param ctx
     * @param c
     * @param flags
     */
    public MyRecycleAdapter(Context ctx, Cursor c, int flags) {
        super(ctx, c, flags);
        this.mContext = ctx;
        inflater = LayoutInflater.from(ctx);
        LOG_TAG = ctx.getClass().getName();
    }

    @Override
    public RecipeViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutID =R.layout.recipe_card;
        View view = inflater.inflate(layoutID, parent, false);
        view.setFocusable(true);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final RecipeViewHolder holder, Cursor cursor) {


        String recipeName = cursor.getString(FragmentRecipeMain.COL_CAKES_NAME);
        holder.tv_name.setText(recipeName);
        String recipeIngredients = cursor.getString(FragmentRecipeMain.COL_CAKES_INGRE);
        holder.tv_ingredients.setText(recipeIngredients);
        String imageUrl = cursor.getString(FragmentRecipeMain.COL_CAKES_IMG);

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
                holder.iv.setImageBitmap(bitmap);
                is.close();
            } catch (IOException e) {
                Log.e(LOG_TAG, "获取本地图片失败！"+ "\n" + e.getMessage());
            }
        }
        else { //加载网络图片
            //通过handler的方式来获得async从网络获得图片
            Handler handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    switch (msg.what) {
                        case 1:
                            Bitmap bitmap = (Bitmap) msg.obj;
                            break;
                        default:
                            break;
                    }
                }

            };
            DownloadImageTask imageTask =  new DownloadImageTask(imageUrl,handler);
            //We only input one url, so
            imageTask.execute();
        }

        if(onItemClick != null){
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClick.onClick(holder.getAdapterPosition()); //使用接口回调的方法将参数传递出来
                }
            });
        }
    }

    /**
     * Params: url String
     * Return: Bitmap object
     */
    private class DownloadImageTask extends AsyncTask<String, Integer, Bitmap> {

        String mUrl;
        Handler mHandler;

        public DownloadImageTask(String url, Handler handler){
            this.mUrl = url;
            this.mHandler = handler;
        }
        @Override
        protected Bitmap doInBackground(String... params) {
            InputStream is = null;
            Bitmap bitmap = null;
            //params是一个String数组
            int i =0;
            try {
                HttpURLConnection conn = (HttpURLConnection) (new URL(mUrl)).openConnection();
                conn.setDoInput(true);
                conn.connect();
                is = conn.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);
                is.close();
                return bitmap;
            } catch (MalformedURLException e) {
                Log.e(LOG_TAG, "网络URL不正确！" + "\n" + e.getMessage());
            } catch (IOException e) {
                Log.e(LOG_TAG, "加载网络图片错误！" + "\n" + e.getMessage());
            }
            if (isCancelled()) return null;
            return null;
        }

        @Override
        protected void onPostExecute(Bitmap result){
            super.onPostExecute(result);
            Message msg = mHandler.obtainMessage();
            if(result!=null){
                msg.what = 1;
                msg.obj = result;
            }else{
                msg.what = 2;
            }
            mHandler.sendMessage(msg);

        }
    }

    @Override
    protected void onContentChanged() {}

    /**
     * Cache of the children views for a forecast list item.
     */
    public class RecipeViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        final TextView tv_name;
        final ImageView iv;
        final TextView tv_ingredients;
        /**
         * Constructor
         * @param view
         */
        public RecipeViewHolder(View view) {
            super(view);
            tv_name = (TextView) view.findViewById(R.id.recipe_name);
            iv=(ImageView) view.findViewById(R.id.picture);
            tv_ingredients = (TextView)view.findViewById(R.id.recipe_ingredients);
            view.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
        }
    }
}
