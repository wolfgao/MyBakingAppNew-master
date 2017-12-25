package com.example.wolfgao.mybakingapp;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.OrientationHelper;
import android.support.v7.widget.RecyclerView;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by gaochuang on 2017/11/7.
 */

public class DetailFragment extends Fragment {

    private RecyclerView mDetailRecycleView;
    private List<String> mList = new ArrayList<String>();
    private String mRecipe_detail = null;
    private DetailRecyclerAdapter mDetailRecyclerAdapter;
    static final String DETAIL_URI = "recipe_detail";
    private final String RECIPE_NAME = "name";
    private final String RECIPE_INGRE = "ingredients";

    private String mRecipeName=null;
    private String mRecipeIngre = null;
    private String mRecipe_step = null;
    private TextView tv_ingredients;
    private JSONArray mJsonArraySteps;


    private String DEBUG_TAG;

    private ViewGroup.LayoutParams mLayoutParams;

    //Constructor
    public DetailFragment(){
        super();
    }

    public void onCreate(Bundle savedInstance) {
        super.onCreate(savedInstance);
        setHasOptionsMenu(true);
        DEBUG_TAG = getActivity().getClass().getName();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        //Step 1 初始化view
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
        mDetailRecycleView = (RecyclerView) rootView.findViewById(R.id.recipe_detail_recyclerview);
        //设置Layout部分 step 1#
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mDetailRecycleView.setLayoutManager(layoutManager);
        layoutManager.setOrientation(OrientationHelper.VERTICAL);

        //初始化材料view
        tv_ingredients = (TextView)rootView.findViewById(R.id.recipe_ingre);
        tv_ingredients.setMovementMethod(ScrollingMovementMethod.getInstance());

        Bundle bundle = getArguments();
        if(bundle !=null) {
            mRecipe_detail = bundle.getString("recipe_detail");
            initDetailData();
        }
        //Step 2 设置adaptor
        mDetailRecyclerAdapter = new DetailRecyclerAdapter(getContext(), mList);
        mDetailRecycleView.setAdapter(mDetailRecyclerAdapter);

        //增加分割线step 3#
        mDetailRecycleView.addItemDecoration(new android.support.v7.widget.DividerItemDecoration(getContext(),OrientationHelper.VERTICAL));

        //Set 删除动画 step 4#
        mDetailRecycleView.setItemAnimator(new DefaultItemAnimator());
        return rootView;
    }


    private void initDetailData() {
        if(mRecipe_detail != null){
            try{
                JSONObject recipe_data = new JSONObject(mRecipe_detail);
                mRecipeName = recipe_data.getString(RECIPE_NAME);
                mRecipeIngre = RecipeJsonData.getRecipeDesc(recipe_data);
                mJsonArraySteps = recipe_data.getJSONArray("steps");
                mRecipe_step = mJsonArraySteps.toString();
                int nSize = mJsonArraySteps.length();
                for (int i = 0; i < nSize; i++) {
                    mList.add(mJsonArraySteps.getString(i));
                }

            }catch (JSONException e){

            }finally {

            }
            this.getActivity().setTitle(mRecipeName);
            tv_ingredients.setText(mRecipeIngre);
        }
    }

    //当横屏的时候进行初始化
    public void setDetailData(String s) {
        mRecipe_detail =s;
        initDetailData();
        if(mDetailRecyclerAdapter != null){
            mDetailRecyclerAdapter.notifyDataSetChanged();
        }
    }

}
