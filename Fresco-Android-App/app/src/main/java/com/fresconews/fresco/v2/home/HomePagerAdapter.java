package com.fresconews.fresco.v2.home;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.util.SparseArray;
import android.view.ViewGroup;

import com.fresconews.fresco.v2.home.following.FollowingFeedFragment;
import com.fresconews.fresco.v2.home.highlights.HighlightsFragment;
import com.fresconews.fresco.v2.utils.LogUtils;

/**
 * Created by ryan on 8/11/2016.
 */
public class HomePagerAdapter extends FragmentPagerAdapter {

    private static final int NUM_TABS_LOGGED_OUT = 1;
    private static final int NUM_TABS_LOGGED_IN = 2;

    private boolean loggedIn = false;

    private SparseArray<HomeFragment> registeredFragments;

    public HomePagerAdapter(FragmentManager fragmentManager) {
        super(fragmentManager);

        registeredFragments = new SparseArray<>();
    }

    @Override
    public Fragment getItem(int position) {
        if (position == 0) {
            return new HighlightsFragment();
        }
        else {
            return new FollowingFeedFragment();
        }
    }

    @Override
    public int getCount() {
        return loggedIn ? NUM_TABS_LOGGED_IN : NUM_TABS_LOGGED_OUT; //todo logged in could change at any point hombre
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        HomeFragment fragment = (HomeFragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public HomeFragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }

    public void setLoggedIn(boolean loggedIn) {
        this.loggedIn = loggedIn;
    }
}
