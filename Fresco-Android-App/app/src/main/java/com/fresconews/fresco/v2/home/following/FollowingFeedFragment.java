package com.fresconews.fresco.v2.home.following;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.base.BaseFragment;
import com.fresconews.fresco.v2.home.HomeFragment;

/**
 * Created by ryan on 8/11/2016.
 */
public class FollowingFeedFragment extends BaseFragment implements HomeFragment {
    private static final String TAG = FollowingFeedFragment.class.getSimpleName();

    private FollowingFeedViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModel = new FollowingFeedViewModel(this);
        return setViewModel(R.layout.fragment_following, viewModel, container);
    }

    @Override
    public void reload() {
        if (viewModel != null) {
            viewModel.reload(null);
        }
    }
}
