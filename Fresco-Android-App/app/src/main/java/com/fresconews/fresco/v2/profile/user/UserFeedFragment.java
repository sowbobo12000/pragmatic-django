package com.fresconews.fresco.v2.profile.user;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseFragment;

/**
 * Created by ryan on 8/11/2016.
 */
public class UserFeedFragment extends BaseFragment {

    public static final String USER_ID = "userId";

    public static UserFeedFragment newInstance(String userId) {
        UserFeedFragment fragment = new UserFeedFragment();
        Bundle args = new Bundle(1);
        args.putString(USER_ID, userId);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        String userId = getArguments().getString(USER_ID);
        UserFeedViewModel viewModel = new UserFeedViewModel(this, userId);
        return setViewModel(R.layout.fragment_user_feed, viewModel, container);
    }
}
