package com.fresconews.fresco.v2.settings.dialogs;

import android.app.Activity;
import android.view.View;

import com.fresconews.fresco.framework.mvvm.DialogViewModel;

import rx.functions.Action1;

/**
 * Created by mauricewu on 11/4/16.
 */
public class LogOutDialogViewModel extends DialogViewModel {

    private OnLogOutListener listener;

    public LogOutDialogViewModel(Activity activity, OnLogOutListener listener) {
        super(activity);
        this.listener = listener;
    }

    public Action1<View> cancel = view -> dismiss();

    public Action1<View> logOut = view -> {
        if (listener != null) {
            listener.onLogOutClick();
        }
        dismiss();
    };

    public interface OnLogOutListener {
        void onLogOutClick();
    }
}
