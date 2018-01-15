package com.example.wolfgao.mybakingapp.widget;

/**
 * Created by gaochuang on 2018/1/11.
 */

public class ConfigureItem {
    private String itemName;
    private boolean isChecked;

    //default Constructor
    public ConfigureItem(String name, boolean b){
        super();
        this.itemName = name;
        this.isChecked = b;
    }

    public void setItemName(String name){
        this.itemName = name;
    }

    public void setChecked(boolean b){
        this.isChecked = b;
    }

    public String getItemName(){
        return itemName;
    }

    public boolean getChecked(){
        return isChecked;
    }

    @Override
    public String toString(){
        return "Item name is " + itemName + ", isChecked is "+ isChecked;
    }
}
