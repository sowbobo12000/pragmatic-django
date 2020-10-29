package com.fresconews.fresco.v2.settings;

import android.databinding.Bindable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.SwitchCompat;
import android.view.View;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableBoolean;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.network.responses.NetworkUserSettings;
import com.fresconews.fresco.framework.network.responses.NetworkUserSettingsOptions;
import com.fresconews.fresco.v2.settings.dialogs.MapRadiusDialogViewModel;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.NetworkUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.google.android.gms.maps.SupportMapFragment;

import java.util.Locale;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Created by mauricewu on 10/28/16.
 */
public class SettingsAssignmentViewModel extends BaseSettingsViewModel {
    private static final String TAG = SettingsAssignmentViewModel.class.getSimpleName();

    public double notificationRadiusMiles;
    public String notificationRadiusFeet;
    public BindableBoolean assignmentNotificationsOn = new BindableBoolean();

    private MapRadiusDialogViewModel mapRadiusDialogViewModel;

    public SettingsAssignmentViewModel(ActivityViewModel activityViewModel) {
        super(activityViewModel);

        userManager.getUserSettings()
                   .observeOn(AndroidSchedulers.mainThread())
                   .onErrorReturn(throwable -> null)
                   .subscribe(networkUserSettings -> {
                       if (networkUserSettings == null) {
                           return;
                       }
                       for (NetworkUserSettings netSets : networkUserSettings) {
                           if (netSets.getType().equals("notify-user-dispatch-new-assignment")) {
                               NetworkUserSettingsOptions optts = netSets.getNetworkUserSettingsOptions();
                               if (optts.isSendEmail() || optts.isSendFresco() || optts.isSendPush() || optts.isSendSms()) {
                                   assignmentNotificationsOn.set(true);
                               }
                           }
                       }
                   });
    }

    @Bindable
    public String getNotificationRadiusFeet() {
        if (notificationRadiusFeet == null || notificationRadiusFeet.equals("")) {
            userManager.me().onErrorReturn(throwable -> null).subscribe(user -> {
                if (user != null && user.getRadius() != null) {
                    setNotificationRadiusFeet(String.format(Locale.getDefault(), "%d mi.", (int) Double.parseDouble(user.getRadius()))); //imogen might want different way of displaying
                    setNotificationRadiusMiles(Double.parseDouble(user.getRadius()));
                }
            });
        }
        return notificationRadiusFeet;
    }

    public void setNotificationRadiusFeet(String notificationRadiusFeet) {
        this.notificationRadiusFeet = notificationRadiusFeet;
        notifyPropertyChanged(BR.notificationRadiusFeet);
    }

    @Bindable
    public double getNotificationRadiusMiles() {
        return notificationRadiusMiles;
    }

    public void setNotificationRadiusMiles(double notificationRadiusMiles) {
        this.notificationRadiusMiles = notificationRadiusMiles;
        notifyPropertyChanged(BR.notificationRadiusMiles);
    }

    private void saveNotificationRadius() {
        String userId = "";
        userManager.setNotificationRadius(notificationRadiusMiles); //API
        if (sessionManager.isLoggedIn()) {
            userId = sessionManager.getCurrentSession().getUserId();
        }
        userManager.getUser(userId)
                   .onErrorReturn(throwable -> null)
                   .filter(user -> user != null)
                   .subscribe(user -> {
                       LogUtils.i(TAG, "Saving user notification radius locally to - " + Double.toString(notificationRadiusMiles));
                       setNotificationRadiusFeet(String.format(Locale.getDefault(), "%d mi.", (int) notificationRadiusMiles)); // bound to settings screen
                       user.setRadius(Double.toString(notificationRadiusMiles)); //Locally
                       user.save();
                   });
    }

    public Action1<SwitchCompat> assignmentNotificationsSwitch = switchCompatBindableView -> {
        if (switchCompatBindableView.isChecked()) {
            notificationRadiusMiles = 10;
            setNotificationRadiusMiles(10);

            assignmentNotificationsOn.set(true);

            userManager.updateAssignmentNotifications(true, true, true, true)
                       .observeOn(AndroidSchedulers.mainThread())
                       .onErrorReturn(throwable -> null)
                       .subscribe(networkSettings -> {
                           if (networkSettings == null) {
                               //grey things
                               //flip the switch
                               assignmentNotificationsOn.set(false);
                           }

                           // if users miles are 0, set to 10 miles.
                           if (userManager.getNetworkUserMe() == null) {
                               userManager.me()
                                          .onErrorReturn(throwable -> null)
                                          .filter(user -> user != null)
                                          .subscribe(user -> { //todo does this still get triggered
                                              if (Double.parseDouble(user.getRadius()) == 0) {
                                                  saveNotificationRadius();
                                              }
                                          });
                           }
                           else {
                               if (Double.parseDouble(userManager.getNetworkUserMe().getRadius()) == 0) {
                                   saveNotificationRadius();
                               }
                           }
                       });
        }
        else {
            //grey things
            assignmentNotificationsOn.set(false);

            userManager.updateAssignmentNotifications(false, false, false, false)
                       .observeOn(AndroidSchedulers.mainThread())
                       .onErrorReturn(throwable -> null)
                       .subscribe(networkSettings -> {
                           if (networkSettings == null) {
                               //ungrey things out
                               //flip the switch
                               assignmentNotificationsOn.set(true);
                           }
                       });
        }
    };

    public Action1<View> showNotificationRadiusDialog = view -> {
        if (assignmentNotificationsOn.get()) {
            if (mapRadiusDialogViewModel == null) {
                mapRadiusDialogViewModel = new MapRadiusDialogViewModel(getActivity(), new MapRadiusDialogViewModel.MapRadiusDialogListener() {
                    @Override
                    public void onBound() {
                        mapRadiusDialogViewModel.getDialog().setOnDismissListener(dialogInterface -> {
                            SupportMapFragment mapFragment = ((SupportMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.map));
                            FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                            ft.remove(mapFragment);
                            ft.commit();
                            mapRadiusDialogViewModel = null;
                        });
                    }

                    @Override
                    public void onSaveRadiusClick(Double radius) {
                        mapRadiusDialogViewModel = null;
                        //Set user radius to API and locally
                        String userId = "";
                        if (NetworkUtils.isNetworkAvailable(getActivity())) {
                            userManager.setNotificationRadius(radius); //API
                            if (sessionManager.isLoggedIn()) {
                                userId = sessionManager.getCurrentSession().getUserId();
                                userManager.getUser(userId)
                                           .observeOn(AndroidSchedulers.mainThread())
                                           .subscribeOn(Schedulers.io())
                                           .onErrorReturn(throwable -> null)
                                           .subscribe(user -> {
                                               if (user != null) {
                                                   setNotificationRadiusFeet(String.format(Locale.getDefault(), "%d mi.", (int) radius.doubleValue())); // bound to settings screen
                                                   setNotificationRadiusMiles(radius);
                                                   user.setRadius(Double.toString(radius)); //Locally
                                                   user.save();
                                               }
                                           });
                            }
                        }
                        else {
                            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_check_internet);
                        }
                    }
                });
            }

            if (!mapRadiusDialogViewModel.isShowing) {
                mapRadiusDialogViewModel.show(R.layout.view_change_notification_radius);
            }
        }
    };
}
