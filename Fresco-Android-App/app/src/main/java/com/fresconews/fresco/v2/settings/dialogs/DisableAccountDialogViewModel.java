package com.fresconews.fresco.v2.settings.dialogs;

import android.app.Activity;
import android.view.View;

import com.fresconews.fresco.framework.mvvm.DialogViewModel;

import rx.functions.Action1;

/**
 * Created by mauricewu on 11/4/16.
 */
public class DisableAccountDialogViewModel extends DialogViewModel {

    private OnDisableAccountListener listener;

    public DisableAccountDialogViewModel(Activity activity, OnDisableAccountListener listener) {
        super(activity);

        this.listener = listener;
    }

    public Action1<View> cancel = view -> dismiss();

    public Action1<View> disable = view -> {
        if (listener != null) {
            listener.onDisableAccountClick();
        }
        dismiss();
    };

    public interface OnDisableAccountListener {
        void onDisableAccountClick();
    }
}
