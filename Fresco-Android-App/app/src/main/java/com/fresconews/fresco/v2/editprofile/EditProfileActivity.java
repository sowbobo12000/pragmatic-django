package com.fresconews.fresco.v2.editprofile;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.view.inputmethod.EditorInfo;
import android.widget.Scroller;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.network.requests.NetworkSignupRequest;
import com.fresconews.fresco.framework.persistence.managers.AuthManager;
import com.fresconews.fresco.framework.persistence.managers.LocalSettingsManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.v2.home.HomeActivity;
import com.fresconews.fresco.v2.utils.BitmapUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.google.gson.Gson;
import com.kbeanie.multipicker.api.CacheLocation;
import com.kbeanie.multipicker.api.ImagePicker;
import com.kbeanie.multipicker.api.Picker;
import com.kbeanie.multipicker.api.callbacks.ImagePickerCallback;
import com.kbeanie.multipicker.api.entity.ChosenImage;

import java.io.FileNotFoundException;
import java.util.List;

import javax.inject.Inject;

import rx.Observable;

public class EditProfileActivity extends BaseActivity {
    private static final String TAG = EditProfileActivity.class.getSimpleName();

    public static final int PERMISSIONS_STORAGE_REQUEST_CODE = 54;
    private static final int KILL_ME = 666;
    private static final int BIO_NUM_LINES = 7;
    private static final double MAX_SIZE_PICTURE_MB = 4.8;

    public static final String EXTRA_USER_ID = "EXTRA_USER_ID";
    public static final String EXTRA_NETWORK_REQUEST = "EXTRA_NETWORK_REQUEST";
    public static final String EXTRA_FULL_NAME = "EXTRA_FULL_NAME";
    public static final String EXTRA_LOCATION = "EXTRA_LOCATION";
    public static final String EXTRA_AVATAR = "EXTRA_AVATAR";
    public static final String EXTRA_NOTIFICATION_RADIUS = "EXTRA_NOTIFICATION_RADIUS";
    public static final String SUBMISSIONS = "submissions";

    @Inject
    LocalSettingsManager localSettingsManager;

    @Inject
    UserManager userManager;

    @Inject
    AuthManager authManager;

    @Inject
    SessionManager sessionManager;

    private ImagePicker imagePicker;
    private EditProfileViewModel viewModel;

    public static void start(Activity activity, String userId, int requestCode) {
        Intent starter = new Intent(activity, EditProfileActivity.class);
        starter.putExtra(EXTRA_USER_ID, userId);
        activity.startActivityForResult(starter, requestCode);
    }

    public static void start(Activity activity, NetworkSignupRequest request, String fullName, String location, String avatar, double radius) {
        String requestString = new Gson().toJson(request, NetworkSignupRequest.class);

        Intent starter = new Intent(activity, EditProfileActivity.class);
        starter.putExtra(EXTRA_NETWORK_REQUEST, requestString);
        starter.putExtra(EXTRA_LOCATION, location);
        starter.putExtra(EXTRA_FULL_NAME, fullName);
        starter.putExtra(EXTRA_AVATAR, avatar);
        starter.putExtra(EXTRA_NOTIFICATION_RADIUS, radius);
        activity.startActivityForResult(starter, KILL_ME);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);

        viewModel = new EditProfileViewModel(this);

        setViewModel(R.layout.activity_edit_profile, viewModel);

        //It's gross but it's necessary. No way to bind it from XML.
        TextInputLayout nameLayout = (TextInputLayout) findViewById(R.id.name_layout);
        TextInputEditText nameText = (TextInputEditText) findViewById(R.id.editName);
        nameText.setOnFocusChangeListener((view, hasFocus) -> {
            nameLayout.setCounterEnabled(hasFocus);
        });

        TextInputLayout locationLayout = (TextInputLayout) findViewById(R.id.location_layout);
        TextInputEditText locationText = (TextInputEditText) findViewById(R.id.editLocation);
        locationText.setOnFocusChangeListener((view, hasFocus) -> {
            locationLayout.setCounterEnabled(hasFocus);
        });

        TextInputLayout bioLayout = (TextInputLayout) findViewById(R.id.bio_layout);
        TextInputEditText bioText = (TextInputEditText) findViewById(R.id.editBio);
        bioText.setScroller(new Scroller(this.getApplicationContext()));
        bioText.setMaxLines(BIO_NUM_LINES);
        bioText.setVerticalScrollBarEnabled(true);
        bioText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        bioText.setHorizontallyScrolling(false);
        bioText.setLines(BIO_NUM_LINES);
        bioText.setOnFocusChangeListener((view, hasFocus) -> {
            bioLayout.setCounterEnabled(hasFocus);
        });
    }

    @Override
    public void onBackPressed() {
        if (viewModel != null && viewModel.isSignupUser()) {
            viewModel.signup.call(null);
        }
        else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        getViewModel().refreshHeader(sessionManager.getCurrentSession());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Picker.PICK_IMAGE_DEVICE && resultCode == Activity.RESULT_OK && data != null) {
            try {
                Cursor cursor = getContentResolver().query(data.getData(), null, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    long size = cursor.getLong(cursor.getColumnIndex(OpenableColumns.SIZE));
                    cursor.close();
                    if (size > Math.round(MAX_SIZE_PICTURE_MB * 1024 * 1024)) {
                        Bitmap smallImage = BitmapUtils.decodeUri(this, data.getData(), 2100); //Resize
                        data.setData(BitmapUtils.bitmapToUriConverter(smallImage));
                    }
                }
            }
            catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            if (imagePicker == null) {
                SnackbarUtil.dismissableSnackbar(getDataBinding().getRoot(), R.string.error_image_pick);
            }
            else {
                imagePicker.submit(data);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_STORAGE_REQUEST_CODE) {
            boolean granted = true;
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    granted = false;
                }
            }
            if (granted) {
                pickImage()
                        .onErrorReturn(throwable -> null)
                        .subscribe(url -> {
                            if(url != null) {
                                viewModel.setAvatarUrl(url);
                            }
                        });
            }
        }
    }

    public Observable<String> pickImage() {
        return Observable.create(subscriber -> {
            imagePicker = new ImagePicker(this);
            imagePicker.shouldGenerateThumbnails(false);
            imagePicker.shouldGenerateMetadata(false);
            imagePicker.setImagePickerCallback(new ImagePickerCallback() {
                @Override
                public void onImagesChosen(List<ChosenImage> list) {
                    if (list.size() == 0) {
                        subscriber.onCompleted();
                        return;
                    }
                    subscriber.onNext(list.get(0).getQueryUri());
                    subscriber.onCompleted();
                }

                @Override
                public void onError(String err) {
                    LogUtils.e(TAG, err);
                    subscriber.onError(new Throwable(err));
                }
            });
            imagePicker.setCacheLocation(CacheLocation.EXTERNAL_STORAGE_APP_DIR);
            imagePicker.pickImage();
        });
    }

    public void killMe(){
        if(isTaskRoot()){
            HomeActivity.start(this, true);
        }
        if(android.os.Build.VERSION.SDK_INT >= 21) {
            setResult(KILL_ME);
            finishAndRemoveTask();
        }
        else {
            setResult(KILL_ME);
            finish();
        }
    }
}
