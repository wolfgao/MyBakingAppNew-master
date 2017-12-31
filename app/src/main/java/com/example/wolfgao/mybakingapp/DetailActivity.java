package com.example.wolfgao.mybakingapp;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by gaochuang on 2017/11/7.
 */

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //接收从MainActivity传递的uri，如果是竖屏，用户点击后才会有第一个intent传过来。
        //如果是横屏，默认为第一个元素。
        if (savedInstanceState == null) {
            //创建一个detail fragment并且添加到activity中
            Bundle arguments = new Bundle();
            arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

            DetailFragment detailFragment = new DetailFragment();
            detailFragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.recipe_detail_container, detailFragment)
                    .commit();
        }

    }
}
