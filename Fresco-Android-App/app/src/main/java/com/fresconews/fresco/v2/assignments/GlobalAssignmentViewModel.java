package com.fresconews.fresco.v2.assignments;

import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.AssignmentViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.AssignmentViewModelDataSource;
import com.fresconews.fresco.framework.persistence.managers.AssignmentManager;
import com.fresconews.fresco.framework.mvvm.datasources.ListDataSource;
import com.fresconews.fresco.framework.recyclerview.RecyclerViewBindingAdapter;
import com.fresconews.fresco.v2.utils.DialogUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.google.android.gms.maps.model.LatLng;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class GlobalAssignmentViewModel extends ActivityViewModel<GlobalAssignmentActivity> {
    public BindableView<RecyclerView> recyclerView = new BindableView<>();

    @Inject
    AssignmentManager assignmentManager;

    private boolean expiredAssignment = false;

    public GlobalAssignmentViewModel(GlobalAssignmentActivity activity, boolean expiredAssignment) {
        super(activity);
        ((Fresco2) getActivity().getApplication()).getFrescoComponent().inject(this);
        setTitle(activity.getString(R.string.global_assignments));
        setNavIcon(R.drawable.ic_close_gray);
        this.expiredAssignment = expiredAssignment;
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }
        super.onBound();

        if (recyclerView.get() == null) {
            return;
        }

        assignmentManager.find(new LatLng(40, -74), 100)
                         .onErrorReturn(throwable -> null)
                         .subscribe(notUsed -> {
                             assignmentManager.getGlobalAssignments()
                                              .observeOn(AndroidSchedulers.mainThread())
                                              .subscribeOn(Schedulers.io())
                                              .onErrorReturn(throwable -> null)
                                              .subscribe(assignments -> {
                                                  AssignmentViewModelDataSource dataSource = new AssignmentViewModelDataSource(this, new ListDataSource<>(assignments)); //todo is this dangerous if null
                                                  RecyclerViewBindingAdapter<AssignmentViewModel> adapter = new RecyclerViewBindingAdapter<>(R.layout.global_assignment_view, dataSource);

                                                  recyclerView.get().setLayoutManager(new LinearLayoutManager(getActivity()));
                                                  recyclerView.get().setAdapter(adapter);
                                              });
                         });

        //Check if we were sent here from a push notification (implied) that's expired
        if (expiredAssignment) {
            DialogUtils.showFrescoDialog(getActivity(), R.string.expired, R.string.error_assignment_expired, R.string.dismiss,
                    (dialogInterface, i) -> dialogInterface.dismiss());
        }
    }
}
