package com.fresconews.fresco.v2.profile;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.fresconews.fresco.v2.profile.likes.LikesFeedFragment;
import com.fresconews.fresco.v2.profile.user.UserFeedFragment;

public class ProfilePagerAdapter extends FragmentPagerAdapter {

    private String mUserId;

    private UserFeedFragment userFeedFragment;
    private LikesFeedFragment likesFeedFragment;

    public ProfilePagerAdapter(FragmentManager fragmentManager, String userId) {
        super(fragmentManager);
        mUserId = userId;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            userFeedFragment = UserFeedFragment.newInstance(mUserId);
            return userFeedFragment;
        }
        else {
            likesFeedFragment = LikesFeedFragment.newInstance(mUserId);
            return likesFeedFragment;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return "FEED";
        }
        else {
            return "LIKES";
        }
    }

    @Override
    public int getCount() {
        return 2;
    }

    public UserFeedFragment getUserFeedFragment() {
        return userFeedFragment;
    }

    public LikesFeedFragment getLikesFeedFragment() {
        return likesFeedFragment;
    }
}
