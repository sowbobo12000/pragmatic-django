package com.fresconews.fresco.framework.mvvm.datasources;

import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Created by Ryan on 6/1/2016.
 */
public interface IDataSource<T> {
    int getItemCount();
    T get(int position);
    void onBind(RecyclerView.Adapter adapter, int position);
    void onDetached();
    List<T> list();
}
