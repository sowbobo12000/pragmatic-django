package com.fresconews.fresco.v2.settings;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.View;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.FCMManager;
import com.fresconews.fresco.framework.persistence.managers.PaymentManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import io.card.payment.CardIOActivity;
import io.card.payment.CreditCard;

/**
 * Created by techjini on 21/7/16.
 */
public class SettingsActivity extends BaseActivity {
    private static final String TAG = SettingsActivity.class.getSimpleName();
    private static final int KILL_ME = 666;

    public static final String EXTRA_SHOW_PAYMENT_INFO = "EXTRA_SHOW_PAYMENT_INFO";
    public static final String EXTRA_SHOW_TAX_INFO = "EXTRA_SHOW_TAX_INFO";
    public static final String EXTRA_SHOW_STATE_ID_INFO = "EXTRA_SHOW_STATE_ID_INFO";

    public static final int SCAN_REQUEST_CODE = 138;
    public static final int CAMERA_REQUEST_CODE = 123;

    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @Inject
    SessionManager sessionManager;

    @Inject
    PaymentManager paymentManager;

    @Inject
    UserManager userManager;

    @Inject
    FCMManager fcmManager;

    @Inject
    AnalyticsManager analyticsManager;

    private Uri outputFileUri;
    private SettingsViewModel viewModel;

    public static void start(Context context) {
        Intent starter = new Intent(context, SettingsActivity.class);
        context.startActivity(starter);
    }

    public static void startForResult(Activity activity) {
        Intent starter = new Intent(activity, SettingsActivity.class);
        activity.startActivityForResult(starter, KILL_ME);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);

        viewModel = new SettingsViewModel(this);
        setViewModel(R.layout.activity_settings, viewModel);

        analyticsManager.trackScreen("Settings");

        if (getIntent().getBooleanExtra(EXTRA_SHOW_PAYMENT_INFO, false)) {
            viewModel.getSettingsPaymentViewModel().showPaymentDialog("");
        }
        else if (getIntent().getBooleanExtra(EXTRA_SHOW_TAX_INFO, false)) {
            viewModel.getSettingsPaymentViewModel().showChangeTaxInfoPopup();
        }
        else if (getIntent().getBooleanExtra(EXTRA_SHOW_STATE_ID_INFO, false)) {
            viewModel.getSettingsPaymentViewModel().showChangeTaxInfoPopup();
        }
    }

    @Override
    public void onBackPressed() {
        Intent databackIntent = new Intent();
        setResult(KILL_ME, databackIntent);
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getViewModel().refreshHeader(sessionManager.getCurrentSession());
    }

    public void launchCameraForDocumentId() {
        new RxPermissions(this)
                .request(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .onErrorReturn(throwable -> null)
                .subscribe(granted -> {
                    if (granted != null && granted) { // Always true pre-M
                        outputFileUri = launchCameraIntent(CAMERA_REQUEST_CODE);
                    }
                });
    }

    private Uri launchCameraIntent(int requestCode) {
        Uri outputFileUri = null;
        String sdCard = Environment.getExternalStorageDirectory().getPath();
        if (TextUtils.isEmpty(sdCard)) {
            return null;
        }
        File picturesDir = new File(sdCard, "Pictures");
        if (!picturesDir.exists()) {
            if (!picturesDir.mkdirs()) {
                return null;
            }
        }
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File imagePath = new File(picturesDir.getPath() + File.separator + "IMG_" + timeStamp + ".jpg");
        try {
            boolean created = imagePath.createNewFile();
            if (created) {
                outputFileUri = Uri.fromFile(imagePath);

                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
                startActivityForResult(cameraIntent, requestCode);
            }
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        return outputFileUri;
    }

    public void onScanPress(View v) {
        Intent scanIntent = new Intent(this, CardIOActivity.class);

        // customize these values to suit your needs.
        scanIntent.putExtra(CardIOActivity.EXTRA_REQUIRE_EXPIRY, true); // default: false
        scanIntent.putExtra(CardIOActivity.EXTRA_RETURN_CARD_IMAGE, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_CONFIRMATION, true);
        scanIntent.putExtra(CardIOActivity.EXTRA_SUPPRESS_MANUAL_ENTRY, true);
        //You could optionally inflate a layout over it. But you can't inflate card.io into a view.
//        scanIntent.putExtra(CardIOActivity.EXTRA_SCAN_OVERLAY_LAYOUT_ID, R.layout.view_add_payment);
        scanIntent.putExtra(CardIOActivity.EXTRA_USE_CARDIO_LOGO, false);
        scanIntent.putExtra(CardIOActivity.EXTRA_USE_PAYPAL_ACTIONBAR_ICON, false);
        scanIntent.putExtra(CardIOActivity.EXTRA_KEEP_APPLICATION_THEME, true); //let's see...

        startActivityForResult(scanIntent, SCAN_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CAMERA_REQUEST_CODE) {
            viewModel.getSettingsPaymentViewModel().isProcessing.set(true);
            viewModel.getSettingsPaymentViewModel().dueBy.set("Processing");

            File selectedImageFile = new File(outputFileUri.getPath());
            viewModel.getSettingsPaymentViewModel().updateDocumentId(selectedImageFile);
        }
        else {
            if (requestCode == SCAN_REQUEST_CODE) {
                if (data != null && data.hasExtra(CardIOActivity.EXTRA_SCAN_RESULT)) {
                    CreditCard scanResult = data.getParcelableExtra(CardIOActivity.EXTRA_SCAN_RESULT);

                    String expirationDate = "";
                    String cvv = "";
                    String redactedCard = scanResult.getRedactedCardNumber();
                    String formattedCard = scanResult.getFormattedCardNumber();

                    // Never log a raw card number. Avoid displaying it, but if necessary use getFormattedCardNumber()
                    String resultDisplayStr = "Card Number: " + scanResult.getRedactedCardNumber() + "\n";
                    resultDisplayStr += "Card Number Formatted: " + scanResult.getFormattedCardNumber() + "\n";
                    resultDisplayStr += "Card Type toString: " + scanResult.getCardType().toString() + "\n";

                    if (scanResult.isExpiryValid()) {
                        resultDisplayStr += "Expiration Date: " + scanResult.expiryMonth + "/" + scanResult.expiryYear + "\n";
                        expirationDate = scanResult.expiryMonth + "/" + scanResult.expiryYear;
                    }

                    if (scanResult.cvv != null) {
                        // Never log or display a CVV
                        resultDisplayStr += "CVV has " + scanResult.cvv.length() + " digits.\n";
                        cvv = scanResult.cvv;
                    }
                    resultDisplayStr += "Card Type: " + scanResult.getCardType();
                    Bitmap cardImage = CardIOActivity.getCapturedCardImage(data);
                    viewModel.getSettingsPaymentViewModel().populateCardInfo(redactedCard, formattedCard, expirationDate, cvv, cardImage);
                    LogUtils.i(TAG, resultDisplayStr);
                }
            }
            else if (requestCode == CAMERA_REQUEST_CODE) {
                viewModel.getSettingsPaymentViewModel().isProcessing.set(false);
                viewModel.getSettingsPaymentViewModel().dueBy.set("");
            }
            else {
                viewModel.getSettingsSocialViewModel().getTwitterAuthClient().onActivityResult(requestCode, resultCode, data);
                viewModel.getSettingsSocialViewModel().onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    private List<String> getPermissionsDenied() {
        List<String> permissionsToAllow = new ArrayList<>();
        for (String permission : CAMERA_PERMISSIONS) {
            if (ActivityCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                permissionsToAllow.add(permission);
            }
        }

        return permissionsToAllow;
    }
}
