package com.fresconews.fresco.v2.profile;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.AuthManager;
import com.fresconews.fresco.framework.persistence.managers.LocalSettingsManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailActivity;
import com.fresconews.fresco.v2.utils.LogUtils;

import javax.inject.Inject;

import static com.fresconews.fresco.v2.search.SearchActivity.EXTRA_FROM_SEARCH;

public class ProfileActivity extends BaseActivity {
    private static final String TAG = ProfileActivity.class.getSimpleName();

    public static final int EDIT_REQUEST_CODE = 44;

    public static final String FROM_PUSH_NOTIFICATION = "FROM_PUSH_NOTIFICATION";
    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    public static final String EXTRA_FOLLOW_BACK = "EXTRA_FOLLOW_BACK";

    @Inject
    LocalSettingsManager mLocalSettingsManager;

    @Inject
    UserManager mUserManager;

    @Inject
    AuthManager mAuthManager;

    @Inject
    SessionManager sessionManager;

    @Inject
    AnalyticsManager analyticsManager;

    private String userId;
    private ProfileViewModel viewModel;

    public static void start(Context context, String userId) {
        start(context, userId, false, false, false);
    }

    public static void start(Context context, String userId, boolean fromSearch, boolean newTask, boolean clearTop) {
        Intent starter = new Intent(context, ProfileActivity.class);
        if (newTask) {
            starter.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            starter.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (clearTop) {
            starter.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        }
        starter.putExtra(EXTRA_USER_ID, userId);
        starter.putExtra(EXTRA_FROM_SEARCH, fromSearch);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);

        userId = null;
        boolean fromSearch = false;
        boolean follow = false;

        analyticsManager.trackScreen("Profile");

        String scheme = getIntent().getScheme();
        if (getIntent().getData() != null && !TextUtils.isEmpty(scheme) && scheme.startsWith("http")) {
            userId = getIntent().getData().getLastPathSegment();
        }
        else {
            userId = getIntent().getStringExtra(EXTRA_USER_ID);
            fromSearch = getIntent().getBooleanExtra(EXTRA_FROM_SEARCH, false);
            follow = getIntent().getBooleanExtra(EXTRA_FOLLOW_BACK, false);
        }

        if (userId == null && sessionManager.isLoggedIn()) {
            LogUtils.i(TAG, "userId was null. Using default user.");
            userId = sessionManager.getCurrentSession().getUserId();
        }

        analyticsManager.profileScreenOpened();

        viewModel = new ProfileViewModel(this, userId, follow);
        setViewModel(R.layout.activity_profile, viewModel);

        TextView tv = (TextView) findViewById(R.id.editBio);
        if (tv != null) {
            tv.setHorizontallyScrolling(false);
            tv.setLines(7);
        }

        if (getIntent().getBooleanExtra(FROM_PUSH_NOTIFICATION, false)) {
            GalleryDetailActivity.start(this, getIntent().getExtras());
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == EDIT_REQUEST_CODE) {
            if (data != null) {
                Uri uri = data.getData();
                viewModel.setAvatarUrl(uri.toString());
                viewModel.setSelfAvatarUrl(uri.toString());
            }
            viewModel.downloadUser();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        analyticsManager.profileScreenClosed(userId);
        analyticsManager.stopTrackingPost();
    }

    public String getUserId() {
        return userId;
    }
}
