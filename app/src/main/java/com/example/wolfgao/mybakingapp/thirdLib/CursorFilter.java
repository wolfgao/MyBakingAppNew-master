package com.example.wolfgao.mybakingapp.thirdLib;

import android.database.Cursor;
import android.widget.Filter;

/**
 * Copy from https://www.jianshu.com/p/333fe22cabc6
 * RecyclerView是谷歌推荐使用的也是现在比较常用的控件，用来代替ListView。CursorAdapter经常会配合数据库使用，
 * 然而在RecyclerView中使用CursorAdapter时会出错！查了一下，CursorAdapter只和ListView适配，RecyclerView并
 * 不兼容，看来需要一个改造过的CursorAdapter.
 *
 */

class CursorFilter extends Filter {

    CursorFilterClient mClient;

    interface CursorFilterClient {
        CharSequence convertToString(Cursor cursor);

        Cursor runQueryOnBackgroundThread(CharSequence constraint);

        Cursor getCursor();

        void changeCursor(Cursor cursor);
    }

    CursorFilter(CursorFilterClient client) {
        mClient = client;
    }

    @Override
    public CharSequence convertResultToString(Object resultValue) {
        return mClient.convertToString((Cursor) resultValue);
    }

    @Override
    protected FilterResults performFiltering(CharSequence constraint) {
        Cursor cursor = mClient.runQueryOnBackgroundThread(constraint);

        FilterResults results = new FilterResults();
        if (cursor != null) {
            results.count = cursor.getCount();
            results.values = cursor;
        } else {
            results.count = 0;
            results.values = null;
        }
        return results;
    }

    @Override
    protected void publishResults(CharSequence constraint, FilterResults results) {
        Cursor oldCursor = mClient.getCursor();

        if (results.values != null && results.values != oldCursor) {
            mClient.changeCursor((Cursor) results.values);
        }
    }
}