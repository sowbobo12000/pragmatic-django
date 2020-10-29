package com.fresconews.fresco.framework.mvvm;

import android.app.Activity;
import android.support.annotation.CallSuper;
import android.view.View;

import com.fresconews.fresco.framework.base.BaseFragment;

/**
 * Created by ryan on 8/11/2016.
 */
public abstract class FragmentViewModel<T extends BaseFragment> extends ViewModel {
    private static final String TAG = FragmentViewModel.class.getSimpleName();

    private T fragment;
    protected boolean hasBeenBound = false;

    public FragmentViewModel(T fragment) {
        this.fragment = fragment;
    }

    @Override
    @CallSuper
    public void onBound() {
        if (!hasBeenBound) {
            hasBeenBound = true;
        }
    }

    public T getFragment() {
        return fragment;
    }

    public Activity getActivity() {
        return fragment.getActivity();
    }

    protected View getRoot() {
        if (getActivity() != null && getFragment() != null && getFragment().getDataBinding() != null) {
            return getFragment().getDataBinding().getRoot();
        }
        return null;
    }
}
