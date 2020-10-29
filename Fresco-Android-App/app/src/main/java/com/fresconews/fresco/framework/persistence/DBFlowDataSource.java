package com.fresconews.fresco.framework.persistence;

import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.raizlabs.android.dbflow.list.FlowQueryList;
import com.raizlabs.android.dbflow.structure.Model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DBFlowDataSource<T extends Model> implements IDataSource<T> {
    private static final String TAG = DBFlowDataSource.class.getSimpleName();

    private FlowQueryList<T> items;
    private boolean alwaysRefresh = false;

    public DBFlowDataSource(FlowQueryList<T> items) {
        this.items = items;
    }

    public DBFlowDataSource(FlowQueryList<T> items, boolean alwaysRefresh) {
        this.items = items;
        this.alwaysRefresh = alwaysRefresh;
    }

    @Override
    public int getItemCount() {
        try {
            if (items == null) {
                return 0;
            }
            return items.size();
        }
        catch (IllegalStateException e) {
            e.printStackTrace();
            return 0;
        }
    }

    @Override
    public T get(int position) {
        return items.get(position);
    }

    @Override
    public void onBind(RecyclerView.Adapter adapter, int position) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onDetached() {
        try {
            items.close();
            items.endTransactionAndNotify();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public List<T> list() {
        List<T> viewModels = new ArrayList<>();
        if (items != null) {
            viewModels = items.getCopy();
        }
        return viewModels;
    }
}
