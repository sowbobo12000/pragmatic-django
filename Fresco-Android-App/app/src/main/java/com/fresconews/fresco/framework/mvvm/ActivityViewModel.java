package com.fresconews.fresco.framework.mvvm;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.databinding.Bindable;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.MenuRes;
import android.support.annotation.PluralsRes;
import android.support.annotation.StringRes;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.BuildConfig;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.injection.helpers.InjectionTarget;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.FeedManager;
import com.fresconews.fresco.framework.persistence.managers.FrescoUploadService2;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.framework.persistence.models.UploadStatusMessage;
import com.fresconews.fresco.v2.assignments.AssignmentMapActivity;
import com.fresconews.fresco.v2.camera.CameraWrapperActivity;
import com.fresconews.fresco.v2.dev.DevOptionsActivity;
import com.fresconews.fresco.v2.home.HomeActivity;
import com.fresconews.fresco.v2.login.LoginActivity;
import com.fresconews.fresco.v2.navdrawer.HeaderViewModel;
import com.fresconews.fresco.v2.onboarding.OnboardingActivity;
import com.fresconews.fresco.v2.search.SearchActivity;
import com.fresconews.fresco.v2.settings.SettingsActivity;
import com.fresconews.fresco.v2.storiespreview.StoriesPreviewActivity;
import com.fresconews.fresco.v2.utils.DialogUtils;
import com.fresconews.fresco.v2.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import io.smooch.ui.ConversationActivity;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;

public abstract class ActivityViewModel<T extends BaseActivity> extends ViewModel {
    private static final String TAG = ActivityViewModel.class.getSimpleName();

    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private static final int POLL_HIGHLIGHTS_FOLLOWING_DELAY = 40;

    @Inject
    SessionManager sessionManager;

    @Inject
    AnalyticsManager analyticsManager;

    @Inject
    FeedManager feedManager;

    @Inject
    GalleryManager galleryManager;

    public BindableView<NavigationView> navigationView = new BindableView<>();
    public BindableView<Toolbar> toolbar = new BindableView<>();
    public BindableView<Toolbar> errorToolbar = new BindableView<>();

    private T activity;
    private String title;
    private String prevTitle;

    protected DrawerLayout drawerLayout;
    public Subscription notificationSubscription;

    private HeaderViewModel headerViewModel;
    private InjectionTarget injection;
    private boolean errorToolbarShown = false;
    private boolean errorDialogShown = false;
    protected boolean hasBeenBound = false;

    @DrawableRes
    private int navIcon;

    @Bindable
    @MenuRes
    public int uploadErrorMenu = R.menu.upload_error_menu;

    public ActivityViewModel() {
    }

    public ActivityViewModel(T activity) {
        this.activity = activity;
        injection = new InjectionTarget();
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(injection);
    }

    public void onResume() {
        refreshNavDrawer();
        if (drawerLayout != null) {
            drawerLayout.closeDrawer(GravityCompat.START);
            drawerLayout.closeDrawers();
        }
        poll();
    }

    public void onPause() {
        if (notificationSubscription != null && !notificationSubscription.isUnsubscribed()) {
            notificationSubscription.unsubscribe();
        }
        if (Fresco2.highlightsFollowingSubscription != null && !Fresco2.highlightsFollowingSubscription.isUnsubscribed()) {
            Fresco2.highlightsFollowingSubscription.unsubscribe();
        }
    }

    @Override
    @CallSuper
    public void onBound() {
        if (!hasBeenBound) {
            hasBeenBound = true;
            if (navigationView.get() != null) {
                headerViewModel = new HeaderViewModel(getActivity());
                ViewDataBinding headerView = DataBindingUtil.inflate(activity.getLayoutInflater(), R.layout.drawer_header, null, false);
                headerView.setVariable(BR.model, headerViewModel);
                navigationView.get().addHeaderView(headerView.getRoot());
                navigationView.get().getMenu().findItem(R.id.dev_options).setVisible(BuildConfig.DEBUG);
                UploadStatusMessage currentUploadStatus = injection.mUploadManager.getCurrentUploadStatus();
                handleUploadStatusMessage(currentUploadStatus);

                refreshNavDrawer();
            }
        }
        poll();
    }

    @Override
    @CallSuper
    public void onUnBound() {
        hasBeenBound = false;
    }

    public T getActivity() {
        return activity;
    }

    public void setActivity(T activity) {
        this.activity = activity;
    }

    protected String getString(@StringRes int stringRes) {
        if (activity == null || stringRes == 0) {
            return "";
        }
        return activity.getString(stringRes);
    }

    protected String getString(@StringRes int stringRes, Object... formatArgs) {
        if (activity == null || stringRes == 0) {
            return "";
        }
        return activity.getString(stringRes, formatArgs);
    }

    protected String getQuantityString(@PluralsRes int pluralRes, int quantity, Object... formatArgs) {
        if (activity == null || pluralRes == 0) {
            return "";
        }
        return activity.getResources().getQuantityString(pluralRes, quantity, formatArgs);
    }

    public void handleUploadStatusMessage(UploadStatusMessage message) {
        switch (message.getUploadStatus()) {
            case UploadStatusMessage.UPLOADING:
                onUploadProgress(Math.max(1, message.getProgress()));
                LogUtils.i(TAG, "upload progress - " + message.getProgress());
                break;
            case UploadStatusMessage.DONE:
                onUploadCompleted();
                break;
            case UploadStatusMessage.ERROR:
                onUploadProgress(0);
                if (message.getErrorMessage() != null && !message.getErrorMessage().equals("shown")) {
                    onUploadError(message.getErrorMessage());
                }
                else {
                    onUploadError();
                }
                message.setErrorMessage("shown");
                break;
        }
    }

    public void refreshHeader(Session session) {
        if (headerViewModel == null) {
            return;
        }
        headerViewModel.setSession(session);
    }

    private void refreshNavDrawer() {
        if (hasBeenBound && navigationView.get() != null) {
            if (isLoggedIn()) {
                navigationView.get().getMenu().findItem(R.id.home).setTitle(getActivity().getString(R.string.home));
            }
            else {
                navigationView.get().getMenu().findItem(R.id.home).setTitle(getActivity().getString(R.string.highlights));
            }
            if (activity instanceof HomeActivity) {
                navigationView.get().getMenu().findItem(R.id.home).setChecked(true);
            }
            else if (activity instanceof StoriesPreviewActivity) {
                navigationView.get().getMenu().findItem(R.id.stories).setChecked(true);
            }
            else if (activity instanceof AssignmentMapActivity) {
                navigationView.get().getMenu().findItem(R.id.assignments).setChecked(true);
            }
            else if (activity instanceof SettingsActivity) {
                navigationView.get().getMenu().findItem(R.id.settings).setChecked(true);
            }
            else if (activity instanceof DevOptionsActivity) {
                navigationView.get().getMenu().findItem(R.id.dev_options).setChecked(true);
            }
        }
    }

    private boolean isDrawerActivity() {
        return activity != null && ((activity instanceof HomeActivity) || (activity instanceof StoriesPreviewActivity) || (activity instanceof AssignmentMapActivity) || (activity instanceof SettingsActivity));
    }

    public Action1<DrawerLayout> onDrawerLayoutCreated = drawerLayout -> this.drawerLayout = drawerLayout;

    public Action1<View> onNavIconClicked = view -> drawerLayout.openDrawer(GravityCompat.START);

    public Func1<MenuItem, Boolean> onNavItemSelected = menuItem -> {
        drawerLayout.closeDrawer(GravityCompat.START);
        drawerLayout.closeDrawers();

        switch (menuItem.getItemId()) {
            case R.id.home:
                if (!(activity instanceof HomeActivity)) {
                    HomeActivity.start(activity, true, navigationView.get().getMenu().findItem(R.id.home).getActionView().getVisibility() == View.VISIBLE);
                }
                break;
            case R.id.stories:
                if (!(activity instanceof StoriesPreviewActivity)) {
                    StoriesPreviewActivity.start(activity);
                }
                break;
            case R.id.settings:
                //If user not logged in, go to login, not settings.
                if (isLoggedIn()) {
                    if (!(activity instanceof SettingsActivity)) {
                        SettingsActivity.start(activity);
                    }
                }
                else {
                    if (!(activity instanceof LoginActivity)) {
                        OnboardingActivity.start(activity, "SETTINGS");
                    }
                }
                break;
            case R.id.assignments:
                if (!(activity instanceof AssignmentMapActivity)) {
                    AssignmentMapActivity.start(activity);
                }
                break;
            case R.id.dev_options:
                if (!(activity instanceof DevOptionsActivity)) {
                    DevOptionsActivity.start(activity);
                }
                break;
        }

        return true;
    };

    public Action1<View> cancelUpload = view -> {
        FrescoUploadService2.cancelUpload(getActivity());
        hideErrorToolbar();
    };

    public Func1<MenuItem, Boolean> retryUpload = menuItem -> {
        FrescoUploadService2.retryUpload(getActivity());
        onUploadProgress(0);
        hideErrorToolbar();
        return true;
    };

    private void poll() {
        if (isLoggedIn() && isDrawerActivity() && !(activity instanceof HomeActivity)) {
            if ((Fresco2.highlightsFollowingSubscription == null || Fresco2.highlightsFollowingSubscription.isUnsubscribed())) {
                Session session = sessionManager.getCurrentSession();
                Fresco2.highlightsFollowingSubscription = feedManager.pollFollowing(session.getUserId(), POLL_HIGHLIGHTS_FOLLOWING_DELAY)
                                                                     .observeOn(AndroidSchedulers.mainThread())
                                                                     .onErrorReturn(throwable -> null)
                                                                     .filter(aBoolean -> aBoolean != null && navigationView.get() != null && isDrawerActivity() && !(activity instanceof HomeActivity))
                                                                     .subscribe(aBoolean -> {
                                                                         if (aBoolean) {
                                                                             galleryManager.pollHighlights(POLL_HIGHLIGHTS_FOLLOWING_DELAY)
                                                                                           .observeOn(AndroidSchedulers.mainThread())
                                                                                           .onErrorReturn(throwable -> null)
                                                                                           .filter(aBoolean2 -> aBoolean2 != null && navigationView.get() != null && isDrawerActivity() && !(activity instanceof HomeActivity))
                                                                                           .subscribe(aBoolean2 -> {
                                                                                               if (!aBoolean2) {
                                                                                                   navigationView.get().getMenu().findItem(R.id.home).getActionView().setVisibility(View.VISIBLE);
                                                                                               }
                                                                                           });
                                                                         }
                                                                         else {
                                                                             navigationView.get().getMenu().findItem(R.id.home).getActionView().setVisibility(View.VISIBLE);
                                                                         }
                                                                     });
            }
        }
        else {
            if (Fresco2.highlightsFollowingSubscription != null && !Fresco2.highlightsFollowingSubscription.isUnsubscribed()) {
                Fresco2.highlightsFollowingSubscription.unsubscribe();
            }
            if (navigationView.get() != null) {
                navigationView.get().getMenu().findItem(R.id.home).getActionView().setVisibility(View.GONE);
            }
        }
    }

    @Bindable
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
        notifyPropertyChanged(BR.title);
    }

    @DrawableRes
    @Bindable
    public int getNavIcon() {
        return navIcon;
    }

    public void setNavIcon(@DrawableRes int navIcon) {
        this.navIcon = navIcon;
        notifyPropertyChanged(BR.navIcon);
    }

    public Action1<View> goBack = view -> {
        ActivityCompat.finishAfterTransition(activity);
    };

    public void openCamera() {
        analyticsManager.openedCamera();
        CameraWrapperActivity.start(getActivity());
    }

    private List<String> getPermissionsDenied() {
        List<String> permissionsToAllow = new ArrayList<>();
        for (String permission : CAMERA_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(getActivity(), permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToAllow.add(permission);
            }
        }

        return permissionsToAllow;
    }

    private void checkCameraPermissions() {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.CAMERA) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.RECORD_AUDIO) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                DialogUtils.showFrescoDialog(getActivity(), R.layout.view_permissions,
                        (dialogInterface, i) -> {
                            List<String> permissionsDenied = getPermissionsDenied();
                            ActivityCompat.requestPermissions(getActivity(), permissionsDenied.toArray(new String[permissionsDenied.size()]), BaseActivity.PERMISSIONS_CAMERA_REQUEST_CODE);
                            dialogInterface.dismiss();
                        },
                        (dialogInterface, i) -> dialogInterface.dismiss());
            }
            else {
                ActivityCompat.requestPermissions(getActivity(), CAMERA_PERMISSIONS, BaseActivity.PERMISSIONS_CAMERA_REQUEST_CODE);
            }
        }
        else {
            openCamera();
        }
    }

    public Action1<View> openCamera = view -> checkCameraPermissions();

    public Action1<View> search = view -> SearchActivity.start(activity, null, true, false);

    //Where upload progress is actually set on the upload bar
    private void onUploadProgress(int progress) { //max has to be 100
        hideErrorToolbar();
        if (toolbar.get() != null && toolbar.get().getBackground() != null) {
            if (progress < 10) {
                progress = 10;
            }
            toolbar.get().getBackground().setLevel(progress * 100); //max is 10,000
            toolbar.get().setTitle(R.string.uploading);
        }
        prevTitle = getTitle();
    }

    private void onUploadCompleted() {
        hideErrorToolbar();
        if (toolbar.get() != null) {
            if (toolbar.get().getBackground() != null) {
                toolbar.get().getBackground().setLevel(0);
            }
            if (prevTitle != null && !prevTitle.isEmpty()) {
                toolbar.get().setTitle(prevTitle);
            }
        }
    }

    private void onUploadError(String errorMessage) {
        revealErrorToolbar();
        if (toolbar.get() != null && prevTitle != null && !prevTitle.isEmpty()) {
            toolbar.get().setTitle(prevTitle);
        }
        //Show Error Dialog
        if (!errorDialogShown) {
            LogUtils.i(TAG, "showing dialog");
            errorDialogShown = true;
            DialogUtils.showFrescoDialog(getActivity(), R.string.upload_error, errorMessage,
                    R.string.get_help, ((dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        errorDialogShown = false;
                        ConversationActivity.show(getActivity());
                    }),
                    R.string.ok, (dialogInterface, i) -> {
                        dialogInterface.dismiss();
                        errorDialogShown = false;
                    });
        }

    }

    private void onUploadError() {
        revealErrorToolbar();
        if (toolbar.get() != null && prevTitle != null && !prevTitle.isEmpty()) {
            toolbar.get().setTitle(prevTitle);
        }
    }

    private void hideErrorToolbar() {
        if (toolbar.get() == null || errorToolbar.get() == null) {
            return;
        }

        if (!errorToolbarShown) {
            return;
        }
        errorToolbarShown = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.upload_progress));

            if (errorToolbar.get().isAttachedToWindow()) {
                errorToolbar.get().setVisibility(View.INVISIBLE);
                return;
            }

            float initialRadius = (float) Math.hypot(errorToolbar.get().getHeight(), errorToolbar.get().getWidth());

            Resources r = getActivity().getResources();
            int pos = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, r.getDisplayMetrics());

            Animator anim = ViewAnimationUtils.createCircularReveal(errorToolbar.get(), pos, pos, initialRadius, 0);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    errorToolbar.get().setVisibility(View.INVISIBLE);
                }
            });
            anim.start();
        }
        else {
            errorToolbar.get().setVisibility(View.INVISIBLE);
        }
    }

    private void revealErrorToolbar() {
        if (toolbar.get() == null || errorToolbar.get() == null) {
            return;
        }

        if (errorToolbarShown) {
            return;
        }
        errorToolbarShown = true;

        errorToolbar.get().setVisibility(View.VISIBLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.fresco_red_dark));

            if (!errorToolbar.get().isAttachedToWindow()) {
                return;
            }

            float finalRadius = (float) Math.hypot(errorToolbar.get().getHeight(), errorToolbar.get().getWidth());

            Resources r = getActivity().getResources();
            int pos = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32, r.getDisplayMetrics());

            Animator anim = ViewAnimationUtils.createCircularReveal(errorToolbar.get(), pos, pos, 0, finalRadius);
            anim.addListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    errorToolbar.get().setVisibility(View.VISIBLE);
                }
            });
            anim.start();
        }
    }

    @Bindable
    protected boolean isLoggedIn() {
        return sessionManager != null && sessionManager.isLoggedIn();
    }

    protected View getRoot() {
        return getActivity().getDataBinding().getRoot();
    }
}
