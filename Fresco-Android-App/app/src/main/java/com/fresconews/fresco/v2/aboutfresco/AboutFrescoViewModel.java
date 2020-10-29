package com.fresconews.fresco.v2.aboutfresco;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.databinding.Bindable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;

import com.fresconews.fresco.BuildConfig;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.v2.utils.NetworkUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import rx.functions.Action1;

/**
 * Created by Khalid P on 8/31/2016.
 */
public class AboutFrescoViewModel extends ActivityViewModel<AboutFrescoActivity> {
    private String TAG = AboutFrescoViewModel.class.getName();

    private final static String FRESCO_URL = "https://www.fresconews.com/";
    private final static String TERMS_URL = "https://www.fresconews.com/terms";
    private final static String PRIVACY_URL = "https://www.fresconews.com/privacy";
    private final static String FRESCO_TWITTER_URL = "https://twitter.com/fresconews/";
    private final static String FRESCO_FACEBOOK_URL = "https://www.facebook.com/fresconews/";
    private final static String FRESCO_INSTAGRAM_URL = "https://www.instagram.com/fresconews/";

    public AboutFrescoViewModel(AboutFrescoActivity activity) {
        super(activity);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getActivity().getWindow().setStatusBarColor(ContextCompat.getColor(getActivity(), R.color.black_10));
        }
    }

    @Bindable
    public String getCredits() {
        return getString(R.string.team_names);
    }

    @Bindable
    public String getReleaseDate() {
        SimpleDateFormat dtf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        return dtf.format(BuildConfig.BUILD_TIME);
    }

    @Bindable
    public String getVersionNumber() {
        return getString(R.string.version_number, BuildConfig.VERSION_NAME);
    }

    public Action1<View> goToFrescoWeb = view -> {
        if (NetworkUtils.isNetworkAvailable(getActivity())) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FRESCO_URL));
            getActivity().startActivity(browserIntent);
        }
        else {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_check_internet);
        }
    };

    public Action1<View> goToTermsOfServices = view -> {
        if (NetworkUtils.isNetworkAvailable(getActivity())) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(TERMS_URL));
            getActivity().startActivity(browserIntent);
        }
        else {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_check_internet);
        }
    };

    public Action1<View> goToPrivacyPolicies = view -> {
        if (NetworkUtils.isNetworkAvailable(getActivity())) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(PRIVACY_URL));
            getActivity().startActivity(browserIntent);
        }
        else {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_check_internet);
        }
    };

    public Action1<View> onTwitterClicked = view -> {
        if (NetworkUtils.isNetworkAvailable(getActivity())) {
            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(FRESCO_TWITTER_URL));
            getActivity().startActivity(browserIntent);
        }
        else {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_check_internet);
        }
    };

    public Action1<View> onFaceBookClicked = view -> {
        Uri uri = Uri.parse(FRESCO_FACEBOOK_URL);
        PackageManager pm = getActivity().getPackageManager();
        try {
            ApplicationInfo applicationInfo = pm.getApplicationInfo("com.facebook.katana", 0);
            if (applicationInfo.enabled) {
                // http://stackoverflow.com/a/24547437/1048340
                uri = Uri.parse("fb://facewebmodal/f?href=" + FRESCO_FACEBOOK_URL);
                Intent facebookIntent = new Intent(Intent.ACTION_VIEW, uri);
                getActivity().startActivity(facebookIntent);
            }
        }
        catch (PackageManager.NameNotFoundException ignored) {
            if (NetworkUtils.isNetworkAvailable(getActivity())) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
                getActivity().startActivity(browserIntent);
            }
            else {
                SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_check_internet);
            }
        }

    };

    public Action1<View> onInstagramClicked = view -> {
        String url = FRESCO_INSTAGRAM_URL;
        PackageManager pm = getActivity().getPackageManager();
        try {
            if (pm.getPackageInfo("com.instagram.android", 0) != null) {
                if (url.endsWith("/")) {
                    url = url.substring(0, url.length() - 1);
                }
                final String username = url.substring(url.lastIndexOf("/") + 1);
                // http://stackoverflow.com/questions/21505941/intent-to-open-instagram-user-profile-on-android
                Intent instagramIntent = new Intent(Intent.ACTION_VIEW);
                instagramIntent.setData(Uri.parse("http://instagram.com/_u/" + username));
                instagramIntent.setPackage("com.instagram.android");
                getActivity().startActivity(instagramIntent);
            }
        }
        catch (PackageManager.NameNotFoundException ignored) {
            if (NetworkUtils.isNetworkAvailable(getActivity())) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                getActivity().startActivity(browserIntent);
            }
            else {
                SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_check_internet);
            }
        }

    };
}
