package com.fresconews.fresco.framework.base;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.databinding.OnRebindCallback;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.network.ConnectionReceiver;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UploadManager;
import com.fresconews.fresco.framework.persistence.models.UploadStatusMessage;
import com.fresconews.fresco.framework.rx.RxBus;
import com.fresconews.fresco.messages.ActivePostChangedMessage;
import com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailViewModel;
import com.fresconews.fresco.v2.notifications.FcmListenerService;
import com.fresconews.fresco.v2.notifications.INotificationActivityViewModel;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.trello.rxlifecycle.components.support.RxAppCompatActivity;

import javax.inject.Inject;

import rx.Subscription;

import static com.fresconews.fresco.framework.persistence.managers.NotificationIntentManager.FROM_PUSH;
import static com.fresconews.fresco.framework.persistence.managers.NotificationIntentManager.OBJECT_ID;
import static com.fresconews.fresco.framework.persistence.managers.NotificationIntentManager.OBJECT_TYPE;
import static com.fresconews.fresco.framework.persistence.managers.NotificationIntentManager.PUSH_KEY;

public abstract class BaseActivity extends RxAppCompatActivity {
    private static final String TAG = BaseActivity.class.getSimpleName();

    public static final int PERMISSIONS_CAMERA_REQUEST_CODE = 11;

    @Inject
    UploadManager uploadManager;

    @Inject
    SessionManager sessionManager;

    @Inject
    AnalyticsManager analyticsManager;

    private ActivityViewModel viewModel;
    private ViewDataBinding dataBinding;

    private Subscription uploadStatusSubscription;
    private boolean isReceiverRegistered;

    private ConnectionReceiver connectionReceiver = new ConnectionReceiver();
    private BroadcastReceiver notificationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (viewModel != null && viewModel instanceof INotificationActivityViewModel) {
                ((INotificationActivityViewModel) viewModel).onNotificationReceived();
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null && intent.getBooleanExtra(FROM_PUSH, false)) {
            String type = intent.getStringExtra(PUSH_KEY);
            String id = intent.getStringExtra(OBJECT_ID);
            String object = intent.getStringExtra(OBJECT_TYPE);
            analyticsManager.notificationOpened(type, id, object);

            LogUtils.i(TAG, "base activity - " + type);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getViewModel().onResume();
        getViewModel().refreshHeader(sessionManager.getCurrentSession());

        uploadStatusSubscription = RxBus.getInstance().register(UploadStatusMessage.class, viewModel::handleUploadStatusMessage);

        UploadStatusMessage currentUploadStatus = uploadManager.getCurrentUploadStatus();

        if (currentUploadStatus.getUploadStatus() == UploadStatusMessage.UPLOADING) {
            LogUtils.i(TAG, "Current Status: Uploading: " + currentUploadStatus.getProgress());
        }
        else if (currentUploadStatus.getUploadStatus() == UploadStatusMessage.DONE) {
            LogUtils.i(TAG, "Current Status: Done: " + currentUploadStatus.getProgress());
        }
        else {
            LogUtils.i(TAG, "Current Status: Error: " + currentUploadStatus.getProgress());
        }

        viewModel.handleUploadStatusMessage(currentUploadStatus);

        if (!isReceiverRegistered) {
            LocalBroadcastManager.getInstance(this).registerReceiver(notificationReceiver,
                    new IntentFilter(FcmListenerService.FCM_INTENT_FILTER));
            LocalBroadcastManager.getInstance(this).registerReceiver(connectionReceiver,
                    new IntentFilter(ConnectionReceiver.CONNECTION_RECEIVER_INTENT_FILTER));
            isReceiverRegistered = true;
        }

        if (getViewModel() instanceof GalleryDetailViewModel) {
            Fresco2.setActivePostIdInViewAnalytics(Fresco2.activePostIdInView, null, false, true);
        }
        else {
            Fresco2.setActivePostIdInViewAnalytics(Fresco2.activePostIdInView, null, true, true);
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        getViewModel().onPause();
        if (uploadStatusSubscription != null) {
            uploadStatusSubscription.unsubscribe();
        }

        RxBus.getInstance().post(new ActivePostChangedMessage("stop"));
        LocalBroadcastManager.getInstance(this).unregisterReceiver(connectionReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(notificationReceiver);
        isReceiverRegistered = false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.onUnBound();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_CAMERA_REQUEST_CODE) {
            boolean granted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                }
            }
            if (granted) {
                viewModel.openCamera();
            }
        }
    }

    protected void setViewModel(@LayoutRes int layoutId, ActivityViewModel viewModel) {
        this.viewModel = viewModel;
        dataBinding = DataBindingUtil.inflate(getLayoutInflater(), layoutId, null, false);
        dataBinding.setVariable(BR.model, viewModel);

        setContentView(dataBinding.getRoot());
        dataBinding.addOnRebindCallback(new OnRebindCallback() {
            @Override
            public void onBound(ViewDataBinding binding) {
                BaseActivity.this.viewModel.onBound();
            }
        });
    }

    public ActivityViewModel getViewModel() {
        return viewModel;
    }

    public ViewDataBinding getDataBinding() {
        return dataBinding;
    }
}
