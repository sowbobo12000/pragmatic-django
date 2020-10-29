package com.fresconews.fresco.framework.mvvm.datasources;

import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.AssignmentViewModel;
import com.fresconews.fresco.framework.persistence.models.Assignment;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

public class AssignmentViewModelDataSource implements IDataSource<AssignmentViewModel> {
    private IDataSource<Assignment> dataSource;
    private WeakReference<ActivityViewModel> activityViewModel;

    public AssignmentViewModelDataSource(ActivityViewModel activityViewModel, IDataSource<Assignment> dataSource) {
        this.dataSource = dataSource;
        this.activityViewModel = new WeakReference<>(activityViewModel);
    }

    @Override
    public int getItemCount() {
        return dataSource.getItemCount();
    }

    @Override
    public AssignmentViewModel get(int position) {
        return new AssignmentViewModel(activityViewModel.get(), dataSource.get(position));
    }

    @Override
    public void onBind(RecyclerView.Adapter adapter, int position) {
        if (dataSource != null) {
            dataSource.onBind(adapter, position);
        }
    }

    @Override
    public void onDetached() {
        if (dataSource != null) {
            dataSource.onDetached();
        }
    }

    @Override
    public List<AssignmentViewModel> list() {
        List<AssignmentViewModel> viewModels = new ArrayList<>();
        if (dataSource != null) {
            for (int i = 0; i < dataSource.getItemCount(); i++) {
                viewModels.add(get(i));
            }
        }
        return viewModels;
    }
}
