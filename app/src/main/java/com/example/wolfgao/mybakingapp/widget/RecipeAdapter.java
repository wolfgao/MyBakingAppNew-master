package com.example.wolfgao.mybakingapp.widget;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import com.example.wolfgao.mybakingapp.R;

import java.util.List;

/**
 * Created by gaochuang on 2018/1/11.
 */

class RecipeAdapter extends BaseAdapter{
    List<ConfigureItem> mList;
    Context mContext;

    //default constructor
    public RecipeAdapter(Context context, List<ConfigureItem> list){
        this.mList = list;
        this.mContext = context;
    }

    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder;
        if(convertView==null){
            convertView=View.inflate(mContext, R.layout.widget_listitem_configure, null);
            viewHolder=new ViewHolder();
            viewHolder.textView=(TextView) convertView.findViewById(R.id.recipe_widget_item_for_select);
            viewHolder.checkBox=(CheckBox) convertView.findViewById(R.id.checkbox);
            convertView.setTag(viewHolder);
        }else{
            viewHolder=(ViewHolder) convertView.getTag();
        }
        viewHolder.textView.setText(mList.get(position).getItemName());

        //显示checkBox
        viewHolder.checkBox.setChecked(mList.get(position).getChecked());

        return convertView;
    }


    class ViewHolder{
        TextView textView;
        CheckBox checkBox;
    }
}
