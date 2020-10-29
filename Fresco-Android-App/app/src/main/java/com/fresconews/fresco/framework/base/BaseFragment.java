package com.fresconews.fresco.framework.base;

import android.app.Activity;
import android.databinding.DataBindingUtil;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.view.View;
import android.view.ViewGroup;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.framework.mvvm.FragmentViewModel;
import com.trello.rxlifecycle.components.support.RxFragment;

/**
 * Created by ryan on 8/11/2016.
 */
public class BaseFragment extends RxFragment {
    private static final String TAG = BaseFragment.class.getSimpleName();

    private FragmentViewModel mViewModel;
    private ViewDataBinding mDataBinding;

    protected View setViewModel(@LayoutRes int layoutId, FragmentViewModel viewModel, ViewGroup container) {
        mViewModel = viewModel;
        mDataBinding = DataBindingUtil.inflate(getLayoutInflater(null), layoutId, container, false);
        mDataBinding.setVariable(BR.model, viewModel);

        mDataBinding.addOnRebindCallback(new OnRebindCallback() {
            @Override
            public void onBound(ViewDataBinding binding) {
                mViewModel.onBound();
            }
        });

        return mDataBinding.getRoot();
    }

    public FragmentViewModel getViewModel() {
        return mViewModel;
    }

    public ViewDataBinding getDataBinding() {
        return mDataBinding;
    }
}
