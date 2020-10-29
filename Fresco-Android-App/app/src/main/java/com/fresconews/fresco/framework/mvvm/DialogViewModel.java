package com.fresconews.fresco.framework.mvvm;

import android.app.Activity;
import android.app.Dialog;
import android.databinding.DataBindingUtil;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;

import com.fresconews.fresco.BR;

/**
 * Created by mauricewu on 11/3/16.
 */
public class DialogViewModel extends ViewModel {
    protected Dialog dialog;
    protected Activity activity;
    protected boolean hasBeenBound = false;

    public DialogViewModel(Activity activity) {
        this.activity = activity;
    }

    @Override
    @CallSuper
    public void onBound() {
        if (!hasBeenBound) {
            hasBeenBound = true;
        }
    }

    public void show(@LayoutRes int layoutRes) {
        ViewDataBinding dataBinding = DataBindingUtil.inflate(LayoutInflater.from(activity), layoutRes, null, false);
        dataBinding.setVariable(BR.model, this);

        if (dialog == null) {
            dialog = new Dialog(activity);
            dialog.setContentView(dataBinding.getRoot());
            dialog.setCancelable(false);
        }
        if (!dialog.isShowing()) {
            dialog.show();
        }

        dataBinding.addOnRebindCallback(new OnRebindCallback() {
            @Override
            public void onBound(ViewDataBinding binding) {

                DialogViewModel.this.onBound();
            }
        });
    }

    protected void dismiss() {
        if (dialog != null) {
            dialog.dismiss();
        }
    }

    protected String getString(@StringRes int stringRes) {
        if (activity == null || stringRes == 0) {
            return "";
        }

        return activity.getString(stringRes);
    }

    public Dialog getDialog() {
        return dialog;
    }

    public boolean isShowing() {
        return dialog != null && dialog.isShowing();
    }
}
