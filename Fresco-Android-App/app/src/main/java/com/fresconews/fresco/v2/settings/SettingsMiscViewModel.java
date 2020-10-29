package com.fresconews.fresco.v2.settings;

import android.content.SharedPreferences;
import android.view.View;

import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.v2.aboutfresco.AboutFrescoActivity;
import com.fresconews.fresco.v2.home.HomeActivity;
import com.fresconews.fresco.v2.settings.dialogs.DisableAccountDialogViewModel;
import com.fresconews.fresco.v2.settings.dialogs.LogOutDialogViewModel;
import com.zendesk.sdk.support.SupportActivity;

import io.smooch.ui.ConversationActivity;
import rx.functions.Action1;

/**
 * Created by mauricewu on 11/4/16.
 */
public class SettingsMiscViewModel extends BaseSettingsViewModel {
    private static final String TAG = SettingsMiscViewModel.class.getSimpleName();
    private static final String ANALYTICS_PERMISSIONS = "FIRST_RUN_INFO";
    private static final String SHOWED_SUSPENSION = "SHOWED_SUSPENSION";

    private LogOutDialogViewModel logOutDialogViewModel;
    private DisableAccountDialogViewModel disableAccountDialogViewModel;

    public SettingsMiscViewModel(ActivityViewModel activityViewModel) {
        super(activityViewModel);
    }

    private void signOut() {
        if (activityViewModel.notificationSubscription != null && !activityViewModel.notificationSubscription.isUnsubscribed()) {
            activityViewModel.notificationSubscription.unsubscribe();
        }
        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ANALYTICS_PERMISSIONS, getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHOWED_SUSPENSION, false); //have to show suspension again when user logs in next.
        editor.apply();
        authManager.logout();
        getActivity().finish();
        HomeActivity.start(getActivity(), true);
    }

    public Action1<View> showLogOutDialog = view -> {
        if (logOutDialogViewModel == null) {
            logOutDialogViewModel = new LogOutDialogViewModel(getActivity(), this::signOut);
        }
        if (!logOutDialogViewModel.isShowing()) {
            logOutDialogViewModel.show(R.layout.view_log_out);
        }
    };

    public Action1<View> showDisableAccountDialog = view -> {
        if (disableAccountDialogViewModel == null) {
            disableAccountDialogViewModel = new DisableAccountDialogViewModel(getActivity(), this::signOut);
        }
        if (!disableAccountDialogViewModel.isShowing()) {
            disableAccountDialogViewModel.show(R.layout.view_disable_account);
        }
    };

    public Action1<View> openSmoochChat = view -> ConversationActivity.show(getActivity());

    public Action1<View> openZendesk = view -> new SupportActivity.Builder()
        .show(getActivity());

    public Action1<View> goToAboutFresco = view -> AboutFrescoActivity.start(getActivity());
}
