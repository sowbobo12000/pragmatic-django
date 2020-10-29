package com.fresconews.fresco.v2.follow.followers;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseFragment;

public class FollowersFragment extends BaseFragment {

    private FollowersViewModel viewModel;
    private String userId;
//    private boolean fromGallery;

    private int state;
    private static final int GALLERY_LIKES_STATE = 11;
    private static final int GALLERY_REPOSTS_STATE = 12;

    public static FollowersFragment newInstance(String userId, int state) {
        FollowersFragment fragment = new FollowersFragment();
        fragment.userId = userId;
        fragment.state = state;
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        viewModel = new FollowersViewModel(this, userId, state);
        return setViewModel(R.layout.fragment_followers, viewModel, container);
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
        Log.i("Followers Fragment", "I was paused");
        viewModel.detachDataSource();
    }
}
