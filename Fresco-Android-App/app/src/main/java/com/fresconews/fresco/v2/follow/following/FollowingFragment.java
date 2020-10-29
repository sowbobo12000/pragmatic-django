package com.fresconews.fresco.v2.follow.following;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseFragment;

/**
 * Created by ryan on 8/16/2016.
 */
public class FollowingFragment extends BaseFragment {

    private FollowingViewModel viewModel;
    private String userId;
    private int state;
    private static final int GALLERY_LIKES_STATE = 11;
    private static final int GALLERY_REPOSTS_STATE = 12;

    public static FollowingFragment newInstance(String userId, int state) {
        FollowingFragment fragment = new FollowingFragment();
        fragment.userId = userId;
        fragment.state = state;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new FollowingViewModel(this, userId, state);
        return setViewModel(R.layout.fragment_following_user, viewModel, container);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (viewModel != null) {
            viewModel.refresh.call(null);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        viewModel.detachDataSource();
    }
}
