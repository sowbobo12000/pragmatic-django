package com.fresconews.fresco.v2.login;

import android.app.Activity;
import android.app.Dialog;
import android.databinding.Bindable;
import android.view.View;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.mvvm.ViewModel;
import com.fresconews.fresco.framework.persistence.managers.AuthManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.v2.home.HomeActivity;

import javax.inject.Inject;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

/**
 * Created by wumau on 9/19/2016.
 */
public class TOSDialogViewModel extends ViewModel {
    private static final String TAG = TOSDialogViewModel.class.getSimpleName();
    public final static int TOS_TYPE = 13;
    public final static int UPDATED_TOS_TYPE = 17;
    public final static int PRIVACY_TYPE = 19;

    @Inject
    UserManager userManager;

    @Inject
    AuthManager authManager;

    public TOSDialogViewModelListener listener;
    private Activity activity;
    private Dialog dialog;
    private String title;
    private String positiveButtonText;
    private String bodyText;
    private boolean negativeButtonHidden;

    public TOSDialogViewModel(Activity activity, String bodyText, int type, TOSDialogViewModelListener listener) {
        this.activity = activity;
        this.listener = listener;
        setBodyText(bodyText);

        switch (type) {
            case TOS_TYPE:
                displayTOS();
                break;
            case UPDATED_TOS_TYPE:
                displayUpdatedTOS();
                break;
            case PRIVACY_TYPE:
                displayPrivacyPolicy();
                break;
        }

        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);
    }

    public void show(View root) {
        if (dialog == null) {
            dialog = new Dialog(activity);
            dialog.setContentView(root);
            dialog.setCancelable(false);
        }
        dialog.show();
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Bindable
    public String getBodyText() {
        return bodyText;
    }

    public void setBodyText(String bodyText) {
        this.bodyText = bodyText;
    }

    @Bindable
    public String getPositiveButtonText() {
        return positiveButtonText;
    }

    public void setPositiveButtonText(String positiveButtonText) {
        this.positiveButtonText = positiveButtonText;
    }

    @Bindable
    public boolean isNegativeButtonHidden() {
        return negativeButtonHidden;
    }

    public void setNegativeButtonHidden(boolean negativeButtonHidden) {
        this.negativeButtonHidden = negativeButtonHidden;
    }

    private void displayTOS() {
        setTitle(activity.getString(R.string.tos));
        setPositiveButtonText(activity.getString(R.string.close));
        setNegativeButtonHidden(true);
    }

    private void displayUpdatedTOS() {
        setTitle(activity.getString(R.string.updated_tos));
        setPositiveButtonText(activity.getString(R.string.i_agree));
        setNegativeButtonHidden(false);
    }

    private void displayPrivacyPolicy() {
        setTitle(activity.getString(R.string.privacy_policy));
        setPositiveButtonText(activity.getString(R.string.close));
        setNegativeButtonHidden(true);
    }

    public Action1<View> logOut = view -> {
        authManager.logout();
        HomeActivity.start(activity, true);
    };

    public Action1<View> close = view -> {
        userManager.acceptTerms()
                   .observeOn(AndroidSchedulers.mainThread())
                   .onErrorReturn(throwable -> null)
                   .subscribe(networkUser -> {
                       if (networkUser != null && listener != null) {
                           if (dialog != null) {
                               dialog.dismiss();
                           }
                           listener.onAcceptClick();
                       }
                   });
    };

    public interface TOSDialogViewModelListener {
        void onAcceptClick();
    }
}
