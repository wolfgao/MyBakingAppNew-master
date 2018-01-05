package com.example.wolfgao.mybakingapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.example.wolfgao.mybakingapp.sync.MyBakingSyncAdaptor;

public class MainActivity extends AppCompatActivity implements FragmentRecipeMain.Callback {


    private final String DEBUG_TAG = getClass().getName();
    private boolean mTwoPane; //If wide screen, we can provide two pane
    private static final String DETAILFRAGMENT_TAG = "DFTAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (findViewById(R.id.recipe_detail_container) != null) {
            // The detail container view will be present only in the large-screen layouts (res/layout-sw600dp).
            // If this view is present, then the activity should be in two-pane mode.
            mTwoPane = true;
            // In two-pane mode, show the detail view in this activity by adding or replacing the detail
            // fragment using a fragment transaction.
            if (savedInstanceState == null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.recipe_detail_container, new DetailFragment(), DETAILFRAGMENT_TAG)
                        .commit();
            }
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        FragmentRecipeMain fragmentRecipeMain = (FragmentRecipeMain) getSupportFragmentManager()
                .findFragmentById(R.id.fragment_recipe_main);

        MyBakingSyncAdaptor.initializeSyncAdapter(this);

        //关于Back的action bar
        /** 在主界面不需要顶部返回按键，因此删除这部分代码
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if(actionBar != null){
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
         */
    }

    @Override
    protected void onResume() {
        super.onResume();
        DetailFragment df = (DetailFragment)getSupportFragmentManager().findFragmentByTag(DETAILFRAGMENT_TAG);
        if ( null != df ) {
            //TODO:继续显示当前菜单步骤细节

        }
    }

    /**在主界面不需要顶部返回按键，因此删除这部分代码
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                this.finish(); // back button
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
     */

    @Override
    public void onItemSelected(Uri contentUri) {
        if(mTwoPane){
            // In two-pane mode, show the detail view in this activity by
            // adding or replacing the detail fragment using fragment transaction.
            Bundle args = new Bundle();
            args.putParcelable(DetailFragment.DETAIL_URI, contentUri);

            DetailFragment fragment = new DetailFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.recipe_detail_container, fragment, DETAILFRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, DetailActivity.class)
                    .setData(contentUri);
            startActivity(intent);
        }
    }
}
