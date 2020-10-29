package com.fresconews.fresco.v2.onboarding;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.v2.home.HomeActivity;
import com.fresconews.fresco.v2.mediabrowser.MediaItemViewModel;
import com.fresconews.fresco.v2.settings.SettingsActivity;
import com.fresconews.fresco.v2.submission.SubmissionActivity;
import com.fresconews.fresco.v2.utils.LogUtils;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

public class OnboardingActivity extends BaseActivity {
    public static final String SUBMISSIONS = "submissions";
    private static final int KILL_ME = 666;
    private boolean goToSettings = false;
    private boolean goToHome= false;

    @Inject
    AnalyticsManager analyticsManager;

    public static void start(Context context) {
        Intent starter = new Intent(context, OnboardingActivity.class);
        context.startActivity(starter);
    }


    public static void start(Context context, String goTo) {
        Intent starter = new Intent(context, OnboardingActivity.class);
        starter.putExtra("goTo", goTo);
        context.startActivity(starter);
    }

//    public static void start(Context context, boolean isFirstRun) {
//        Intent starter = new Intent(context, OnboardingActivity.class);
//        starter.putExtra("isFirstRun", isFirstRun);
//        context.startActivity(starter);
//        //TODO double check this is the only first run, it worked weird for me.
//    }

    public static void start(Context context, ArrayList<MediaItemViewModel> mediaItems) {
        Intent starter = new Intent(context, OnboardingActivity.class);
        starter.putParcelableArrayListExtra(SUBMISSIONS, mediaItems);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ((Fresco2) getApplication()).getFrescoComponent().inject(this);

        ArrayList<MediaItemViewModel> mediaItems = getIntent().getParcelableArrayListExtra(SUBMISSIONS);
        String goTo = getIntent().getStringExtra("goTo");
        if(goTo!=null) {
            if (goTo.equals("SETTINGS")) {
                this.goToSettings = true;
            } else if (goTo.equals("HOME")) {
                this.goToHome = true;
            }
        }

        analyticsManager.beganOnboarding(); // user began onboarding process

        OnBoardingViewModel viewModel = new OnBoardingViewModel(this);
        if(mediaItems!=null){
            setViewModel(R.layout.activity_onboarding, new OnBoardingViewModel(this, mediaItems));
        } else {
            setViewModel(R.layout.activity_onboarding, viewModel);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        analyticsManager.exitedOnboarding();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == KILL_ME ) {
            LogUtils.i("ONBOARDINGACTIVITY", "kill me");
            killMe();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    public void killMe(){
        if(goToSettings){
            goToSettings = false;
            SettingsActivity.startForResult(this);
            return;
        }
        if(goToHome){
            goToHome = false;
            HomeActivity.start(this, true);
            return;
        }
        if(isTaskRoot()){
            HomeActivity.start(this, true);
            return;
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
