package com.example.wolfgao.mybakingapp;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by gaochuang on 2017/10/23.
 */

public class RecipeJsonData {
    // Json架构
    private final static String RECIPENAME = "name";
    private final static String RECIPEINGREDIENTS = "ingredients";
    private final static String RECIPESTEPS = "steps";
    private final static String RECIPEIMAGE = "image";
    private final static String STEP_DESC = "description";
    private final static String STEP_VIDEO = "videoURL";
    private static String DEBUG_TAG = "com.example.wolfgao.mybakingapp.RecipeJsonData";

    //the count number of Json array
    private int mCount = 0;
    private JSONArray mRecipeArray;
    private JSONObject mIngredientsArray[][];
    private JSONObject mStepsArray[][];
    private String mNameArray[];
    private String mPicArray[];
    /**
     * 构造函数，应该要thrown JsonException
     * @param recipeJson 从server获取的Json字符串
     */
    public RecipeJsonData(String recipeJson) throws JSONException {
        mRecipeArray = new JSONArray(recipeJson);
        if (mRecipeArray != null) {
            mCount = mRecipeArray.length();
        }
        parseData();
    }

    /**
     *
     * @return string[] 因为原始Json就是一个数组，我们解析后返回一个数组
     */
    private void parseData(){
        if (mCount == 0){
            return ;
        }
        mNameArray = new String[mCount];
        mPicArray = new String[mCount];

        for (int i = 0; i < mCount; i++) {
            try {
                JSONObject oneRecord = (JSONObject) mRecipeArray.get(i);
                mNameArray[i] = oneRecord.getString(RECIPENAME);

                //给ingredients赋值
                JSONArray ingredientsArray = oneRecord.getJSONArray(RECIPEINGREDIENTS);
                int nSize = ingredientsArray.length();
                mIngredientsArray = new JSONObject[mCount][nSize];
                for (int j = 0; j < nSize; j++) {
                    mIngredientsArray[i][j] = (JSONObject) ingredientsArray.get(j);
                }

                //给Steps赋值
                JSONArray stepsArray = oneRecord.getJSONArray(RECIPESTEPS);
                int nSize2 = stepsArray.length();
                mStepsArray = new JSONObject[mCount][nSize2];
                for (int k = 0; k < nSize2; k++) {
                    mStepsArray[i][k] = (JSONObject) stepsArray.get(k);
                }

                //给图片赋值
                mPicArray[i] = oneRecord.getString(RECIPEIMAGE);

            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     *
     * @return int mCount recipe数组的size
     */
    public int getCount(){
        return mCount;
    }

    /**
     *
     * @return String[] recipe name list
     */
    public String[] getNameList(){
        return mNameArray;
    }

    /**
     * @return JSONArray because this Json actually is an array.
     */
    public JSONArray getObjectArray()
    {
        return mRecipeArray;
    }

    /**
     * Static method to get recipe data from one JSON record
     */
    public static String getRecipeName(JSONObject jso){
        try {
            return jso.getString(RECIPENAME);
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, e.getMessage());
        }
        return null;
    }

    public static String getRecipeDesc(JSONObject recipeData) {
        String recipeStr = null;
        try {
            JSONArray recipeIng =  recipeData.getJSONArray(RECIPEINGREDIENTS);
            recipeStr = "配方如下："+"\n";
            int ingNum = recipeIng.length();
            for (int i = 0; i < ingNum; i++) {
                JSONObject recipeIngPart = (JSONObject) recipeIng.get(i);
                recipeStr += "quantity: " + recipeIngPart.getString("quantity")
                            +" " + recipeIngPart.getString("measure") + "\t"
                            + "ingredient: " + recipeIngPart.getString("ingredient") + "\n";
            }
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, e.getMessage());
        }
        return recipeStr;
    }

    public static String getRecipeImageUrl(JSONObject recipeData) {
        try {
            return recipeData.getString(RECIPEIMAGE);
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, e.getMessage());
        }
        return null;
    }

    /**
     *
     * @param step JSONObject
     * @return String step video url
     */
    public static String getStepVideo(JSONObject step) {
        try {
            return step.getString(STEP_VIDEO);
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, e.getMessage());
        }
        return null;
    }

    /**
     *
     * @param step JSONObject
     * @return String step desc
     */
    public static String getStepDesc(JSONObject step) {
        try {
            return step.getString(STEP_DESC);
        } catch (JSONException e) {
            Log.e(DEBUG_TAG, e.getMessage());
        }
        return null;
    }
}
