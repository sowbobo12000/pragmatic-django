package com.fresconews.fresco.v2.report;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.persistence.models.Gallery;
import com.fresconews.fresco.framework.persistence.models.User;

import static com.fresconews.fresco.v2.report.ReportBottomSheetDialogViewModel.OnReportAndBlockListener;

/**
 * Created by mauricewu on 11/18/16.
 */
public class ReportBottomSheetDialogFragment extends BottomSheetDialogFragment {
    private ReportBottomSheetDialogViewModel viewModel;
    private ViewDataBinding dataBinding;
    private Gallery gallery;
    private User user;
    private boolean fromComment = false;
    private boolean myGallery = false;
    private boolean myComment = false;
    private OnReportAndBlockListener listener;

    public static ReportBottomSheetDialogFragment newInstance(Gallery gallery, User user, OnReportAndBlockListener listener) {
        ReportBottomSheetDialogFragment fragment = new ReportBottomSheetDialogFragment();
        fragment.gallery = gallery;
        fragment.user = user;
        fragment.listener = listener;
        return fragment;
    }

    public static ReportBottomSheetDialogFragment newInstance(Gallery gallery, User user, boolean fromComment,
                                                              boolean myGallery, boolean myComment, OnReportAndBlockListener listener) {
        ReportBottomSheetDialogFragment fragment = new ReportBottomSheetDialogFragment();
        fragment.gallery = gallery;
        fragment.user = user;
        fragment.fromComment = fromComment;
        fragment.myGallery = myGallery;
        fragment.myComment = myComment;
        fragment.listener = listener;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (gallery != null) {
            viewModel = new ReportBottomSheetDialogViewModel(getActivity(), gallery, listener);
        }
        else if (user != null) {
            if (fromComment) {
                viewModel = new ReportBottomSheetDialogViewModel(getActivity(), user, listener, fromComment, myGallery, myComment);

            }
            else {
                viewModel = new ReportBottomSheetDialogViewModel(getActivity(), user, listener);
            }
        }

        if (viewModel == null) {
            return null;
        }
        dataBinding = DataBindingUtil.inflate(getLayoutInflater(null), R.layout.view_report_bottom_sheet, container, false);
        dataBinding.setVariable(BR.model, viewModel);

        dataBinding.addOnRebindCallback(new OnRebindCallback() {
            @Override
            public void onBound(ViewDataBinding binding) {
                if (viewModel != null) {
                    viewModel.onBound();
                }
            }
        });

        return dataBinding.getRoot();
    }

    public void refreshBlockText(Activity activity, User user) {
        if (viewModel != null && user != null) {
            viewModel.refreshBlockText(activity, user);
        }
    }

    public void refreshBlockText(Activity activity, String userId) {
        if (viewModel != null && userId != null) {
            viewModel.refreshBlockText(activity, userId);
        }
    }

}
