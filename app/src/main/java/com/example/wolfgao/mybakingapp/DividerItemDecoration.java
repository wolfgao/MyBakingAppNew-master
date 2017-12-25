package com.example.wolfgao.mybakingapp;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

/**
 * Created by gaochuang on 2017/10/23.
 * About this class, please go and see http://www.jianshu.com/p/b46a4ff7c10a
 * 我们在继承该类来设置分割线时，需要用到的方法只有两个：
 1.绘制分割线 public void onDraw(Canvas c, RecyclerView parent, State state)；
 2.设置偏移量 public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state)；
 */

public class DividerItemDecoration extends RecyclerView.ItemDecoration{
    private static final int[] ATTRS = new int[]{
            android.R.attr.listDivider
    };
    public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

    public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

    private Drawable mDivider;
    private int mOrientation;

    //constructor
    public DividerItemDecoration(Context cxt, int orientation){
        //About TypedArray, please go http://www.android-doc.com/reference/android/content/res/TypedArray.html
        //TypedArray是存储资源数组的容器，obtaiStyledAttributes()方法创建出来。
        // 不过创建完后，如果不在使用了，请注意调用recycle()方法把它释放。
        final TypedArray a = cxt.obtainStyledAttributes(ATTRS);
        mDivider = a.getDrawable(0);
        a.recycle();
        setOrientation(orientation);

    }

    private void setOrientation(int orientation) {
        if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
            throw new IllegalArgumentException("invalid orientation");
        }
        mOrientation = orientation;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);
        if (mOrientation == VERTICAL_LIST) {
            outRect.set(0, 0, 0, mDivider.getIntrinsicHeight());
        } else {
            outRect.set(0, 0, mDivider.getIntrinsicWidth(), 0);
        }
    }

    @Override
    /**
     * How to implementation onDraw method, please see http://www.jianshu.com/p/b46a4ff7c10a
     * 要实现分割线效果需要 getItemOffsets()和 onDraw()2个方法:
     * 首先用 getItemOffsets给item下方空出一定高度的空间（例子中是1dp），然后用onDraw绘制这个空间
     */
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        Log.v("recycleview-decoration", "onDraw()");
        super.onDraw(c, parent, state);
        if (mOrientation == VERTICAL_LIST) {
            drawVertical(c, parent);
        } else {
            drawHorizontal(c, parent);
        }
    }

    private void drawVertical(Canvas c, RecyclerView parent) {
        final int left = parent.getPaddingLeft();
        final int right = parent.getWidth() - parent.getPaddingRight();

        final int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int top = child.getBottom() + lp.bottomMargin;
            final int bottom = top + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);

        }
    }

    private void drawHorizontal(Canvas c, RecyclerView parent){
        final int top = parent.getPaddingTop();
        final int bottom = parent.getHeight() - parent.getPaddingBottom();

        final int count = parent.getChildCount();
        for (int i = 0; i < count; i++) {
            final View child = parent.getChildAt(i);
            final RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) child.getLayoutParams();
            final int left = child.getLeft() + lp.leftMargin;
            final int right = left + mDivider.getIntrinsicHeight();
            mDivider.setBounds(left, top, right, bottom);
            mDivider.draw(c);
        }
    }
}
