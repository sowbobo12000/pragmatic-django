package com.fresconews.fresco.v2.follow;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.fresconews.fresco.R;
import com.fresconews.fresco.v2.follow.followers.FollowersFragment;
import com.fresconews.fresco.v2.follow.following.FollowingFragment;

public class FollowPagerAdapter extends FragmentPagerAdapter {
    private String userId;
//    private boolean fromGallery;
    private int state;
    private FollowingFragment followingFragment;
    private FollowersFragment followersFragment;
    private Context context;

    private static final int GALLERY_LIKES_STATE = 11;
    private static final int GALLERY_REPOSTS_STATE = 12;

    public FollowPagerAdapter(Context context, FragmentManager fragmentManager, String userId, int state) {
        super(fragmentManager);
        this.userId = userId;
//        this.fromGallery = fromGallery;
        this.state = state;
        this.context = context;
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            if (followersFragment == null) {
                followersFragment = FollowersFragment.newInstance(userId, state);
                return followersFragment;
            }
            return followersFragment;
        }
        else {
            if (followingFragment == null) {
                followingFragment = FollowingFragment.newInstance(userId, state);
                return followingFragment;
            }
            return followingFragment;
        }
    }

    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) {
            return context.getString(R.string.followers);
        }
        else {
            return context.getString(R.string.following);
        }
    }

    @Override
    public int getCount() {
        return 2;
    }
}
