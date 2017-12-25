package com.example.wolfgao.mybakingapp;

import android.content.Intent;
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

        //接收从MainActivity传递的数据，如果是竖屏，用户点击后才会有第一个intent传过来。
        //如果是横屏，默认为第一个元素。
        String recipe_detail = null;
        Intent intent = getIntent();

        if (intent != null) {
            recipe_detail = intent.getStringExtra("recipe_detail");
        }
        
        if (savedInstanceState == null) {
            // Create the detail fragment and add it to the activity
            // using a fragment transaction.
            DetailFragment detailFragment = new DetailFragment();
            Bundle bundle = new Bundle();
            bundle.putString("recipe_detail",recipe_detail);//这里的values就是我们要传的值
            detailFragment.setArguments(bundle);

            /** 对于复杂的数据对象，可以采用Parcel来进行通信
             Bundle arguments = new Bundle();
             arguments.putParcelable(DetailFragment.DETAIL_URI, getIntent().getData());

             DetailFragment fragment = new DetailFragment();
             fragment.setArguments(arguments);
             */
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.recipe_detail_container, detailFragment)
                    .commit();
        }

    }
}
