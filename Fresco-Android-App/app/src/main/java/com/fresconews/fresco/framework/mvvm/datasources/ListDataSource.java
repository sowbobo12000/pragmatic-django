package com.fresconews.fresco.framework.mvvm.datasources;

import android.support.v7.widget.RecyclerView;

import java.util.List;

/**
 * Created by ryan on 6/28/2016.
 */
public class ListDataSource<T> implements IDataSource<T> {

    private List<T> items;

    public ListDataSource(List<T> items) {
        this.items = items;
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    @Override
    public T get(int position) {
        return items.get(position);
    }

    @Override
    public void onBind(RecyclerView.Adapter adapter, int position) {

    }

    @Override
    public void onDetached() {
    }

    @Override
    public List<T> list() {
        return items;
    }
}
