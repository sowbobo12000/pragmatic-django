package com.fresconews.fresco.v2.home;

import android.content.SharedPreferences;
import android.databinding.Bindable;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Build;
import android.support.design.widget.TabLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.FeedManager;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.persistence.managers.NotificationFeedManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.Session;
import com.fresconews.fresco.v2.login.TOSDialogViewModel;
import com.fresconews.fresco.v2.notificationfeed.NotificationFeedActivity;
import com.fresconews.fresco.v2.notifications.INotificationActivityViewModel;
import com.fresconews.fresco.v2.utils.DialogUtils;
import com.fresconews.fresco.v2.utils.LogUtils;

import javax.inject.Inject;

import io.smooch.ui.ConversationActivity;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class HomeViewModel extends ActivityViewModel<HomeActivity> implements INotificationActivityViewModel {
    private static final String TAG = HomeViewModel.class.getSimpleName();
    private static final int HIGHLIGHTS_TAB = 0;
    private static final int FOLLOWING_TAB = 1;
    private static final String ANALYTICS_PERMISSIONS = "FIRST_RUN_INFO";
    private static final String SHOWED_SUSPENSION = "SHOWED_SUSPENSION";

    @Inject
    SessionManager sessionManager;

    @Inject
    FeedManager feedManager;

    @Inject
    GalleryManager galleryManager;

    @Inject
    NotificationFeedManager notificationFeedManager;

    @Inject
    AnalyticsManager analyticsManager;

    @Inject
    UserManager userManager;

    public BindableView<ViewPager> viewPager = new BindableView<>();
    public BindableView<TabLayout> tabLayout = new BindableView<>();
    private ViewGroup highlightsTab;
    private ViewGroup followingTab;
    private HomePagerAdapter adapter;

    private String avatarUrl;
    private int unreadNotifications;

    private Subscription highlightsSubscription;
    private Subscription followingSubscription;

    private boolean forceRefresh;

    public HomeViewModel(HomeActivity activity, boolean forceRefresh) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        this.forceRefresh = forceRefresh;

        SharedPreferences sharedPreferences = getActivity().getSharedPreferences(ANALYTICS_PERMISSIONS, getActivity().MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        boolean showedSuspension = sharedPreferences.getBoolean(SHOWED_SUSPENSION, false);

        if (!sessionManager.isLoggedIn()) {
            setTitle(getActivity().getString(R.string.highlights));
            //set number of tabs
            if (tabLayout != null && tabLayout.get() != null && adapter != null) {
                tabLayout.get().setVisibility(View.GONE);
                adapter.setLoggedIn(false);
            }
        }
        else {
            setTitle(getActivity().getString(R.string.home));

            if (isLoggedIn()) {
                //Check for privacy policy updates
                userManager.me()
                           .observeOn(AndroidSchedulers.mainThread())
                           .onErrorReturn(throwable -> null)
                           .subscribe(user -> {
                               if (user == null) {
                                   return;
                               }

                               //Show suspend user if necessary
                               if (user.getSuspendedUntil() != null && !showedSuspension) {
                                   LogUtils.i(TAG, "user is suspended");
                                   editor.putBoolean(SHOWED_SUSPENSION, !showedSuspension);
                                   editor.apply();
                                   DialogUtils.showFrescoDialog(getActivity(), R.string.suspended, getActivity().getString(R.string.suspended_message_to_user),
                                           R.string.ok, ((dialogInterface, i) -> {
                                               dialogInterface.dismiss();
                                           }),
                                           R.string.contact_support, (dialogInterface, i) -> {
                                               ConversationActivity.show(getActivity());
                                               dialogInterface.dismiss();
                                           });
                               }
                               else {
                                   LogUtils.i(TAG, "user is not disabled");
                               }

                               if (user.isTermsValid()) {
                                   LogUtils.i(TAG, "User was null or terms were valid");
                                   return;
                               }

                               String bodyText = user.getTermsBodyText();
                               if (bodyText == null) {
                                   return;
                               }

                               bodyText = bodyText.replace('�', '"');

                               final String finalBodyText = bodyText;
                               getActivity().runOnUiThread(() -> {
                                   TOSDialogViewModel tosDialogViewModel = new TOSDialogViewModel(getActivity(), finalBodyText, TOSDialogViewModel.UPDATED_TOS_TYPE, this::determineResponse);
                                   ViewDataBinding dataBinding = DataBindingUtil.inflate(LayoutInflater.from(getActivity()), R.layout.view_tos, null, false);
                                   dataBinding.setVariable(BR.model, tosDialogViewModel);
                                   tosDialogViewModel.show(dataBinding.getRoot());
                               });
                           });

                String userId = sessionManager.getCurrentSession().getUserId();

                if (!analyticsManager.isTrackingUser()) {
                    analyticsManager.trackUser(userId);
                }

                userManager.getUser(userId).onErrorReturn(throwable -> null).subscribe(user -> {
                    if (user != null) {
                        avatarUrl = user.getAvatar();
                        notifyPropertyChanged(BR.avatarUrl);
                    }
                });
            }

        }
        setNavIcon(R.drawable.ic_menu);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (isLoggedIn()) {
            notificationSubscription = notificationFeedManager.pollUnseenNotificationsCount()
                                                              .observeOn(AndroidSchedulers.mainThread())
                                                              .onErrorReturn(throwable -> null)
                                                              .subscribe(integer -> {
                                                                  if (integer != null) {
                                                                      setUnreadNotifications(integer);
                                                                      notifyPropertyChanged(BR.unreadNotifications);
                                                                  }
                                                              });

            if (tabLayout.get() != null) {
                if (tabLayout.get().getSelectedTabPosition() == HIGHLIGHTS_TAB) {
                    pollFollowings();
                }
                else if (tabLayout.get().getSelectedTabPosition() == FOLLOWING_TAB) {
                    pollHighlights();
                }
            }
        }
        else {
            unsubscribeFollowing();
            unsubscribeHighlights();
        }
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }

        super.onBound();

        if (viewPager.get() == null || tabLayout.get() == null) {
            return;
        }

        adapter = new HomePagerAdapter(getActivity().getSupportFragmentManager());

        if (!sessionManager.isLoggedIn()) {
            tabLayout.get().setVisibility(View.GONE);
            adapter.setLoggedIn(false);
        }
        else {
            tabLayout.get().setVisibility(View.VISIBLE);
            adapter.setLoggedIn(true);
        }

        viewPager.get().setAdapter(adapter);
        tabLayout.get().setupWithViewPager(viewPager.get());

        highlightsTab = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.view_custom_home_tab, tabLayout.get(), false);
        followingTab = (ViewGroup) LayoutInflater.from(getActivity()).inflate(R.layout.view_custom_home_tab, tabLayout.get(), false);

        ((TextView) highlightsTab.findViewById(R.id.tab_text_view)).setText(getString(R.string.highlights));
        if (tabLayout.get().getTabAt(HIGHLIGHTS_TAB) != null) {
            tabLayout.get().getTabAt(HIGHLIGHTS_TAB).setCustomView(highlightsTab);
        }
        ((TextView) followingTab.findViewById(R.id.tab_text_view)).setText(getString(R.string.following));
        if (tabLayout.get().getTabAt(FOLLOWING_TAB) != null) {
            tabLayout.get().getTabAt(FOLLOWING_TAB).setCustomView(followingTab);
        }

        viewPager.get().addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                if (position == 0) {
                    analyticsManager.trackScreen("Home", "highlights");
                }
                else {
                    analyticsManager.trackScreen("Home", "following");
                }
                selectTab(position);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        selectTab(HIGHLIGHTS_TAB);
    }

    @Override
    public void refreshHeader(Session session) {
        super.refreshHeader(session);
        refreshNotifications();
    }

    @Override
    public void onPause() {
        super.onPause();

        unsubscribeFollowing();
        unsubscribeHighlights();
    }

    private void refreshNotifications() {
        if (sessionManager.isLoggedIn()) {
            notificationFeedManager.getUnseenNotificationsCount()
                                   .observeOn(AndroidSchedulers.mainThread())
                                   .onErrorReturn(throwable -> null)
                                   .subscribe(integer -> {
                                       if (integer == null) {
                                           setUnreadNotifications(0);
                                       }
                                       else {
                                           setUnreadNotifications(integer);
                                       }
                                       notifyPropertyChanged(BR.unreadNotifications);
                                   });
        }
    }

    private void pollHighlights() {
        if (isLoggedIn()) {
            highlightsSubscription = galleryManager.pollHighlights()
                                                   .observeOn(AndroidSchedulers.mainThread())
                                                   .onErrorReturn(throwable -> null)
                                                   .subscribe(aBoolean -> {
                                                       if (aBoolean != null && !aBoolean) {
                                                           highlightsTab.findViewById(R.id.notification_image_view).setVisibility(View.VISIBLE);
                                                       }
                                                   });
            unsubscribeFollowing();
            followingTab.findViewById(R.id.notification_image_view).setVisibility(View.GONE);
        }
    }

    private void pollFollowings() {
        Session session = sessionManager.getCurrentSession();
        if (session != null && isLoggedIn()) {
            followingSubscription = feedManager.pollFollowing(session.getUserId())
                                               .observeOn(AndroidSchedulers.mainThread())
                                               .onErrorReturn(throwable -> null)
                                               .subscribe(aBoolean -> {
                                                   if (aBoolean != null && !aBoolean) {
                                                       followingTab.findViewById(R.id.notification_image_view).setVisibility(View.VISIBLE);
                                                   }
                                               });
            unsubscribeHighlights();
            highlightsTab.findViewById(R.id.notification_image_view).setVisibility(View.GONE);
        }
    }

    private void unsubscribeFollowing() {
        if (followingSubscription != null && !followingSubscription.isUnsubscribed()) {
            followingSubscription.unsubscribe();
        }
    }

    private void unsubscribeHighlights() {
        if (highlightsSubscription != null && !highlightsSubscription.isUnsubscribed()) {
            highlightsSubscription.unsubscribe();
        }
    }

    private void selectTab(int position) {
        if (isLoggedIn()) {
            if (position == HIGHLIGHTS_TAB) {
                ((TextView) highlightsTab.findViewById(R.id.tab_text_view)).setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                ((TextView) followingTab.findViewById(R.id.tab_text_view)).setTextColor(ContextCompat.getColor(getActivity(), R.color.white_27));
                if (highlightsTab.findViewById(R.id.notification_image_view).getVisibility() == View.VISIBLE && adapter.getRegisteredFragment(position) != null) {
                    adapter.getRegisteredFragment(position).reload();
                }
                pollFollowings();
            }
            else if (position == FOLLOWING_TAB) {
                ((TextView) highlightsTab.findViewById(R.id.tab_text_view)).setTextColor(ContextCompat.getColor(getActivity(), R.color.white_27));
                ((TextView) followingTab.findViewById(R.id.tab_text_view)).setTextColor(ContextCompat.getColor(getActivity(), R.color.white));
                if ((followingTab.findViewById(R.id.notification_image_view).getVisibility() == View.VISIBLE && adapter.getRegisteredFragment(position) != null) || forceRefresh) {
                    adapter.getRegisteredFragment(position).reload();
                    forceRefresh = false;
                }
                pollHighlights();
            }
        }
    }

    private void determineResponse() {
        LogUtils.i(TAG, "user responded");
    }

    @Bindable
    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String url) {
        avatarUrl = url;
        notifyPropertyChanged(BR.avatarUrl);
    }

    @Bindable
    public int getUnreadNotifications() {
        return unreadNotifications;
    }

    public void setUnreadNotifications(int unreadNotifications) {
        this.unreadNotifications = unreadNotifications;
    }

    @Bindable
    public boolean isLoggedIn() {
        return super.isLoggedIn();
    }

    public Action1<View> profilePictureClicked = view -> {
        NotificationFeedActivity.start(getActivity());
    };

    @Override
    public void onNotificationReceived() {
        refreshNotifications();
    }
}
