package com.fresconews.fresco.v2.settings;

import android.databinding.Bindable;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;

/**
 * Created by techjini on 21/7/16.
 * Dealt with by Blaze on 8/11/16
 */
public class SettingsViewModel extends ActivityViewModel<SettingsActivity> {
    private static final String TAG = SettingsViewModel.class.getSimpleName();

    private SettingsMeViewModel settingsMeViewModel;
    private SettingsPaymentViewModel settingsPaymentViewModel;
    private SettingsAssignmentViewModel settingsAssignmentViewModel;
    private SettingsSocialViewModel settingsSocialViewModel;
    private SettingsMiscViewModel settingsMiscViewModel;

    public SettingsViewModel(SettingsActivity activity) {
        super(activity);

        ((Fresco2) getActivity().getApplication()).getFrescoComponent().inject(this);

        setTitle(activity.getResources().getString(R.string.settings));
        setNavIcon(R.drawable.ic_menu);

        setSettingsMeViewModel(new SettingsMeViewModel(this));
        setSettingsAssignmentViewModel(new SettingsAssignmentViewModel(this));
        setSettingsPaymentViewModel(new SettingsPaymentViewModel(this));
        setSettingsSocialViewModel(new SettingsSocialViewModel(this));
        setSettingsMiscViewModel(new SettingsMiscViewModel(this));
    }

    @Bindable
    public SettingsMeViewModel getSettingsMeViewModel() {
        return settingsMeViewModel;
    }

    public void setSettingsMeViewModel(SettingsMeViewModel settingsMeViewModel) {
        this.settingsMeViewModel = settingsMeViewModel;
        notifyPropertyChanged(BR.settingsMeViewModel);
    }

    @Bindable
    public SettingsAssignmentViewModel getSettingsAssignmentViewModel() {
        return settingsAssignmentViewModel;
    }

    public void setSettingsAssignmentViewModel(SettingsAssignmentViewModel settingsAssignmentViewModel) {
        this.settingsAssignmentViewModel = settingsAssignmentViewModel;
        notifyPropertyChanged(BR.settingsAssignmentViewModel);
    }

    @Bindable
    public SettingsPaymentViewModel getSettingsPaymentViewModel() {
        return settingsPaymentViewModel;
    }

    public void setSettingsPaymentViewModel(SettingsPaymentViewModel settingsPaymentViewModel) {
        this.settingsPaymentViewModel = settingsPaymentViewModel;
        notifyPropertyChanged(BR.settingsPaymentViewModel);
    }

    @Bindable
    public SettingsSocialViewModel getSettingsSocialViewModel() {
        return settingsSocialViewModel;
    }

    public void setSettingsSocialViewModel(SettingsSocialViewModel settingsSocialViewModel) {
        this.settingsSocialViewModel = settingsSocialViewModel;
        notifyPropertyChanged(BR.settingsSocialViewModel);
    }

    @Bindable
    public SettingsMiscViewModel getSettingsMiscViewModel() {
        return settingsMiscViewModel;
    }

    public void setSettingsMiscViewModel(SettingsMiscViewModel settingsMiscViewModel) {
        this.settingsMiscViewModel = settingsMiscViewModel;
        notifyPropertyChanged(BR.settingsMiscViewModel);
    }

    @Override
    public void onBound() {
        super.onBound();

        settingsPaymentViewModel.onBound();
        settingsSocialViewModel.onBound();
    }
}
