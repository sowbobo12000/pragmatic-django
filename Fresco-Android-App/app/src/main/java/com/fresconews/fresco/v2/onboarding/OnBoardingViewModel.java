package com.fresconews.fresco.v2.onboarding;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.v2.login.LoginActivity;
import com.fresconews.fresco.v2.mediabrowser.MediaItemViewModel;
import com.fresconews.fresco.v2.signup.SignupActivity;
import com.fresconews.fresco.v2.utils.ViewListPagerAdapter;
import com.viewpagerindicator.CirclePageIndicator;

import java.util.ArrayList;
import java.util.Arrays;

import javax.inject.Inject;

import rx.functions.Action1;

public class OnBoardingViewModel extends ActivityViewModel<OnboardingActivity> {
    private static final String TAG = OnBoardingViewModel.class.getSimpleName();
    private static final int KILL_ME = 666;


    @Inject
    AnalyticsManager analyticsManager;

    public BindableView<ViewPager> viewPager = new BindableView<>();
    public BindableView<CirclePageIndicator> circleIndicator = new BindableView<>();

    private ArrayList<MediaItemViewModel> mediaItems;

    public OnBoardingViewModel(OnboardingActivity activity) {
        super(activity);
        ((Fresco2) getActivity().getApplication()).getFrescoComponent().inject(this);
    }

    public OnBoardingViewModel(OnboardingActivity activity, ArrayList<MediaItemViewModel> mediaItems) {
        super(activity);
        ((Fresco2) getActivity().getApplication()).getFrescoComponent().inject(this);

        this.mediaItems = mediaItems;
    }

    @Override
    @SuppressLint("InflateParams")
    public void onBound() {
        super.onBound();
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View onboardingPage1 = inflater.inflate(R.layout.onboarding_page1, null);
        View onboardingPage2 = inflater.inflate(R.layout.onboarding_page2, null);
        View onboardingPage3 = inflater.inflate(R.layout.onboarding_page3, null);

        viewPager.get().setAdapter(new ViewListPagerAdapter(Arrays.asList(
                onboardingPage1,
                onboardingPage2,
                onboardingPage3
        )));

        circleIndicator.get().setViewPager(viewPager.get());
    }

    public Action1<View> login = view -> {
        if (mediaItems == null) {
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            getActivity().startActivityForResult(intent, KILL_ME);
        }
        else {
            LoginActivity.start(getActivity(), mediaItems);
        }
    };

    public Action1<View> signup = view -> {
        analyticsManager.completedOnboarding();
        if (mediaItems == null) {
            Intent intent = new Intent(getActivity(), SignupActivity.class);
            getActivity().startActivityForResult(intent, KILL_ME);
        }
        else {
            SignupActivity.start(getActivity(), mediaItems);
        }
    };

    public Action1<View> goBack = view -> {
        analyticsManager.exitedOnboarding();
        ActivityCompat.finishAfterTransition(getActivity());
    };

}
