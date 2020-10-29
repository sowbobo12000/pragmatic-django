package com.fresconews.fresco.v2.follow;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.v2.follow.followers.FollowersFragment;
import com.fresconews.fresco.v2.follow.followers.FollowersViewModel;
import com.fresconews.fresco.v2.follow.following.FollowingFragment;
import com.fresconews.fresco.v2.follow.following.FollowingViewModel;

import javax.inject.Inject;

public class FollowViewModel extends ActivityViewModel<FollowActivity> {
    private static final int FOLLOWERS_PAGE = 0;
    private static final int FOLLOWING_PAGE = 1;
    private static final int GALLERY_LIKES_STATE = 11;
    private static final int GALLERY_REPOSTS_STATE = 12;

    @Inject
    UserManager userManager;

    @Inject
    GalleryManager galleryManager;

    public BindableView<ViewPager> viewPager = new BindableView<>();
    public BindableView<TabLayout> tabLayout = new BindableView<>();

    private String userId;
    private int state;

    public FollowViewModel(FollowActivity activity, String userId, int state) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        this.state = state;
        this.userId = userId;

        setNavIcon(R.drawable.ic_navigation_arrow_back_white);
    }

    @Override
    public void onBound() {
        if (hasBeenBound || viewPager.get() == null) {
            return;
        }
        super.onBound();

        FollowPagerAdapter adapter = new FollowPagerAdapter(getActivity(), getActivity().getSupportFragmentManager(), userId, state);
        viewPager.get().setAdapter(adapter);
        if (state == GALLERY_LIKES_STATE) {
            viewPager.get().setCurrentItem(0);
        }
        else if (state == GALLERY_REPOSTS_STATE) {
            viewPager.get().setCurrentItem(1);
        }
        viewPager.get().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == FOLLOWERS_PAGE) {
                    FollowersFragment followersFragment = (FollowersFragment) adapter.getItem(position);
                    FollowersViewModel followersViewModel = (FollowersViewModel) followersFragment.getViewModel();
                    if (followersFragment.getViewModel() == null) {
                        return;
                    }
                    followersViewModel.refresh.call(null);
                }
                else if (position == FOLLOWING_PAGE) {
                    FollowingFragment followingFragment = (FollowingFragment) adapter.getItem(position);
                    FollowingViewModel followingViewModel = (FollowingViewModel) followingFragment.getViewModel();
                    if (followingFragment.getViewModel() == null) {
                        return;
                    }
                    followingViewModel.refresh.call(null);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        tabLayout.get().setupWithViewPager(viewPager.get());
        if (state == GALLERY_LIKES_STATE || state == GALLERY_REPOSTS_STATE) {
            galleryManager.getGallery(userId)
                          .onErrorReturn(throwable -> null)
                          .subscribe(gallery -> {
                              setTitle("Gallery");
                              TabLayout.Tab followersTab = tabLayout.get().getTabAt(0);
                              TabLayout.Tab followingTab = tabLayout.get().getTabAt(1);
                              if (followersTab != null && gallery != null) {
                                  followersTab.setText(getString(R.string.likes_count, gallery.getLikes()));
                              }
                              if (followingTab != null && gallery != null) {
                                  followingTab.setText(getString(R.string.reposts_count, gallery.getReposts()));
                              }
                          });
        }
        else {
            userManager.getUser(userId)
                       .onErrorReturn(throwable -> null)
                       .subscribe(user -> {
                           if (user != null) {
                               setTitle(user.getFullName());
                           }
                           TabLayout.Tab followersTab = tabLayout.get().getTabAt(0);
                           TabLayout.Tab followingTab = tabLayout.get().getTabAt(1);
                           if (followersTab != null && user != null) {
                               followersTab.setText(getString(R.string.followers_count, user.getFollowedCount()));
                           }
                           if (followingTab != null && user != null) {
                               followingTab.setText(getString(R.string.following_count, user.getFollowingCount()));
                           }
                       });
        }

    }
}
