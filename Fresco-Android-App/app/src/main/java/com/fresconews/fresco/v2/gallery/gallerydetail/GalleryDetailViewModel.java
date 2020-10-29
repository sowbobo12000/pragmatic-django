package com.fresconews.fresco.v2.gallery.gallerydetail;

import android.content.Intent;
import android.databinding.Bindable;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.transition.Transition;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.fresconews.fresco.BR;
import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.R;
import com.fresconews.fresco.framework.databinding.bindingTypes.BindableView;
import com.fresconews.fresco.framework.mvvm.ActivityViewModel;
import com.fresconews.fresco.framework.mvvm.datasources.ArticleViewModelDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.IDataSource;
import com.fresconews.fresco.framework.mvvm.datasources.ListDataSource;
import com.fresconews.fresco.framework.mvvm.viewmodels.ArticleViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.CommentViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.UserSearchViewModel;
import com.fresconews.fresco.framework.network.EndpointHelper;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;
import com.fresconews.fresco.framework.persistence.models.Comment;
import com.fresconews.fresco.framework.persistence.models.Gallery;
import com.fresconews.fresco.framework.persistence.models.Post;
import com.fresconews.fresco.framework.persistence.models.User;
import com.fresconews.fresco.framework.recyclerview.RecyclerViewBindingAdapter;
import com.fresconews.fresco.v2.follow.FollowActivity;
import com.fresconews.fresco.v2.gallery.PostPagerAdapter;
import com.fresconews.fresco.v2.onboarding.OnboardingActivity;
import com.fresconews.fresco.v2.report.ReportBottomSheetDialogFragment;
import com.fresconews.fresco.v2.report.ReportBottomSheetDialogViewModel;
import com.fresconews.fresco.v2.report.ReportGalleryDialogViewModel;
import com.fresconews.fresco.v2.report.ReportUserDialogViewModel;
import com.fresconews.fresco.v2.utils.DialogUtils;
import com.fresconews.fresco.v2.utils.LogUtils;
import com.fresconews.fresco.v2.utils.NetworkUtils;
import com.fresconews.fresco.v2.utils.ResizeUtils;
import com.fresconews.fresco.v2.utils.SnackbarUtil;
import com.google.gson.Gson;
import com.viewpagerindicator.CirclePageIndicator;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Years;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;
import java.util.List;

import javax.inject.Inject;

import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class GalleryDetailViewModel extends ActivityViewModel<GalleryDetailActivity> {
    private static final String TAG = GalleryDetailViewModel.class.getSimpleName();
    private static final int GALLERY_LIKES_STATE = 11;
    private static final int GALLERY_REPOSTS_STATE = 12;
    private static final int FORMAT_POSTED_BY = 17;
    private static final int FORMAT_UPDATED_AT = 19;

    @Inject
    GalleryManager galleryManager;

    @Inject
    SessionManager sessionManager;

    @Inject
    UserManager userManager;

    public BindableView<AppBarLayout> appBarLayout = new BindableView<>();
    public BindableView<NestedScrollView> mainScrollView = new BindableView<>();
    public BindableView<ViewPager> viewPager = new BindableView<>();
    public BindableView<CirclePageIndicator> circleIndicator = new BindableView<>();
    public BindableView<CollapsingToolbarLayout> collapsingToolbar = new BindableView<>();
    public BindableView<RecyclerView> articleList = new BindableView<>();
    public BindableView<ViewGroup> headerView = new BindableView<>();

    private ReportBottomSheetDialogFragment reportFragment;
    private GalleryDetailCommentsViewModel commentsViewModel;
    private GalleryDetailStatusViewModel statusViewModel;
    private UserSearchViewModel postedByViewModel;
    Comment postedByComment;
    private Gallery gallery;
    private boolean galleryLoaded;
    private boolean owner;
    private int currentPostPosition;
    private String currentPost;
    private String galleryId;

    GalleryDetailViewModel(GalleryDetailActivity activity, String galleryId, String currentCommentId, String currentPost, int currentPostPosition) {
        super(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

        setNavIcon(R.drawable.ic_navigation_arrow_back_white);

        this.currentPostPosition = currentPostPosition;
        this.currentPost = currentPost;

        this.galleryId = galleryId;

        commentsViewModel = new GalleryDetailCommentsViewModel(activity, currentCommentId);
        statusViewModel = new GalleryDetailStatusViewModel(activity);

        galleryManager.getOrDownloadGallery(galleryId)
                      .subscribeOn(Schedulers.io())
                      .observeOn(AndroidSchedulers.mainThread())
                      .onErrorReturn(throwable -> null)
                      .subscribe(gallery -> {
                          if (gallery != null) {
                              this.gallery = gallery;
                              notifyPropertyChanged(BR.caption);
                              notifyPropertyChanged(BR.updatedAt);
                              notifyPropertyChanged(BR.postedAt);
                              notifyPropertyChanged(BR.articleCount);
                              notifyPropertyChanged(BR.commentCount);
                              notifyPropertyChanged(BR.addCommentButtonText);
                              setPosts();
                              commentsViewModel.setGallery(gallery);
                              commentsViewModel.setComments();
                              if (gallery.getOriginalOwnerId() != null && sessionManager.getCurrentSession() != null) {
                                  setOwner(sessionManager.isLoggedIn() && gallery.getOriginalOwnerId().equals(sessionManager.getCurrentSession().getUserId()));
                                  setPostedByView(gallery.getOriginalOwnerId());
                              }
                              else {
                                  setPostedByView(gallery.getCuratorId());
                              }
                              if (isOwner()) {
                                  statusViewModel.setStatus(gallery.getRating());
                                  statusViewModel.loadPurchases(gallery.getId());
                              }
                              updateLikes();
                              updateReposts();
                              setGalleryLoaded(true);
                          }
                      });
    }

    @Override
    public void onBound() {
        if (hasBeenBound) {
            return;
        }

        super.onBound();
        setPosts();

        commentsViewModel.onBound();
        statusViewModel.onBound();

        commentsViewModel.setAppBarLayout(appBarLayout.get());
        commentsViewModel.setScrollView(mainScrollView.get());
        commentsViewModel.setComments();

        collapsingToolbar.get().setExpandedTitleColor(Color.argb(0x00, 0xff, 0xff, 0xff));
    }

    public Action1<View> goBack = view -> {
        ActivityCompat.finishAfterTransition(getActivity());
    };

    private int findPostPosition(List<Post> posts, String id) {
        if (posts != null) {
            int pos = 0;
            for (Post post : posts) {
                if (post.getId().equals(id)) {
                    return pos;
                }
                pos++;
            }
        }
        return currentPostPosition;
    }

    private void setPostedByView(String postedById) {
        if (postedById == null) {
            //make view disappear
            //actually i can't
        }
        userManager.downloadUser(postedById)
                   .observeOn(AndroidSchedulers.mainThread())
                   .onErrorReturn(throwable -> null)
                   .subscribe(user -> {
                       if (user == null || postedById == null) {
                           //make view invisibile
                           return;
                       }
                       user.save();
                       postedByViewModel = new UserSearchViewModel(getActivity(), user);
                       notifyPropertyChanged(BR.postedByViewModel);
                       LogUtils.i(TAG, "set posted by is following - " + Boolean.toString(user.isFollowing()));
                   });

    }

    private void setPosts() {
        if (viewPager.get() == null || circleIndicator.get() == null || headerView.get() == null) {
            return;
        }

        if (gallery == null) {
            return;
        }

        List<Post> posts = galleryManager.getPostsForGallery(gallery.getId());

        Point size = ResizeUtils.calculateAndSetViewPagerHeight(posts);
        ViewGroup.LayoutParams params = headerView.get().getLayoutParams();
        params.width = size.x;
        params.height = size.y;
        headerView.get().setLayoutParams(params);

        PostPagerAdapter adapter = new PostPagerAdapter(getActivity(), posts);
        viewPager.get().setAdapter(adapter);
        circleIndicator.get().setViewPager(viewPager.get());
        int postPosition = findPostPosition(posts, currentPost);

        viewPager.get().setCurrentItem(postPosition, false);

        if (!posts.isEmpty()) {
            viewPager.get().setOffscreenPageLimit(8);
            viewPager.get().clearOnPageChangeListeners();

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                if (getActivity() != null && getActivity().getWindow() != null) {
                    getActivity().getWindow().getSharedElementEnterTransition().addListener(new Transition.TransitionListener() {
                        @Override
                        public void onTransitionStart(Transition transition) {

                        }

                        @Override
                        public void onTransitionEnd(Transition transition) {
                            viewPager.get().post(() -> Fresco2.setActivePostIdInViewAnalytics(posts.get(postPosition).getId(), galleryId, false));
                        }

                        @Override
                        public void onTransitionCancel(Transition transition) {

                        }

                        @Override
                        public void onTransitionPause(Transition transition) {
                        }

                        @Override
                        public void onTransitionResume(Transition transition) {
                        }
                    });
                }
            }
            else {
                viewPager.get().post(() -> Fresco2.setActivePostIdInViewAnalytics(posts.get(postPosition).getId(), galleryId, false));
            }

            viewPager.get().addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
                @Override
                public void onPageSelected(int position) {
                    super.onPageSelected(position);
                    if (position >= posts.size()) {
                        return;
                    }

                    viewPager.get().post(() -> Fresco2.setActivePostIdInViewAnalytics(posts.get(position).getId(), galleryId, false));
                }

                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    super.onPageScrolled(position, positionOffset, positionOffsetPixels);
                }
            });
        }
        // needed because ViewPagerIndicator does not respect the android:gravity xml parameter
        // and we want the indicator to be right aligned
        int indicatorRadius = Fresco2.getContext()
                                     .getResources()
                                     .getDimensionPixelSize(R.dimen.page_indicator_radius);
        circleIndicator.get().getLayoutParams().width = adapter.getCount() * indicatorRadius * 3;

        // Article Data Source
        IDataSource<ArticleViewModel> articleDataSource = new ArticleViewModelDataSource(getActivity(), new ListDataSource<>(gallery.loadArticles()));
        RecyclerViewBindingAdapter<ArticleViewModel> articleAdapter = new RecyclerViewBindingAdapter<>(R.layout.item_article, articleDataSource);

        articleList.get().setLayoutManager(new LinearLayoutManager(getActivity()));
        articleList.get().setAdapter(articleAdapter);
    }

    @Bindable
    public String getCaption() {
        if (gallery == null) {
            return "";
        }
        return gallery.getCaption();
    }

    @Bindable
    public String getUpdatedAt() {
        return getFormatTimeAt(FORMAT_UPDATED_AT);

    }

    @Bindable
    public String getPostedAt() {
        return getFormatTimeAt(FORMAT_POSTED_BY);

    }

    private String getFormatTimeAt(int formatType) {
        if (gallery == null) {
            return "";
        }
        int days = 0;
        int years = 0;
        DateTime postedAt = null;
        if (formatType == FORMAT_UPDATED_AT) {
            days = Days.daysBetween(new DateTime(gallery.getUpdatedAt()), new DateTime()).getDays();
            years = Years.yearsBetween(new DateTime(gallery.getUpdatedAt()), new DateTime()).getYears();
            postedAt = new DateTime(gallery.getUpdatedAt());
        }
        else if (formatType == FORMAT_POSTED_BY) {
            days = Days.daysBetween(new DateTime(gallery.getHighlightedAt()), new DateTime()).getDays();
            years = Years.yearsBetween(new DateTime(gallery.getHighlightedAt()), new DateTime()).getYears();
            postedAt = new DateTime(gallery.getHighlightedAt());
        }

        DateTimeFormatter fmt;
        if (!DateFormat.is24HourFormat(getActivity())) {
            fmt = DateTimeFormat.forPattern("hh:mm aa");
        }
        else {
            fmt = DateTimeFormat.forPattern("HH:mm");
        }
        String time = fmt.print(postedAt);

        String stringYear = "";
        String stringMonth = "";
        String stringDay = "";

        if (formatType == FORMAT_UPDATED_AT) {
            stringYear = (String) android.text.format.DateFormat.format("yyyy", gallery.getUpdatedAt()); //2013
            stringMonth = (String) android.text.format.DateFormat.format("MMM", gallery.getUpdatedAt()); //Jun
            stringDay = (String) android.text.format.DateFormat.format("dd", gallery.getUpdatedAt()); //20

        }
        else if (formatType == FORMAT_POSTED_BY) {
            stringYear = (String) android.text.format.DateFormat.format("yyyy", gallery.getCreatedAt()); //2013
            stringMonth = (String) android.text.format.DateFormat.format("MMM", gallery.getCreatedAt()); //Jun
            stringDay = (String) android.text.format.DateFormat.format("dd", gallery.getCreatedAt()); //20
        }

        if (formatType == FORMAT_UPDATED_AT) {
            if (years > 0) {
                return getString(R.string.updated_at_years, stringMonth, stringDay, stringYear, time);
            }
            if (days > 1) {
                return getString(R.string.updated_at, stringMonth, stringDay, time);
            }
            else if (days == 1) {
                return getString(R.string.updated_at_yesterday, time);
            }
            else {
                return getString(R.string.updated_at_today, time);
            }
        }
        else if (formatType == FORMAT_POSTED_BY) {
            if (years > 0) {
                return getString(R.string.posted_by_years, stringMonth, stringDay, stringYear, time);
            }
            if (days > 1) {
                return getString(R.string.posted_by, stringMonth, stringDay, time);
            }
            else if (days == 1) {
                return getString(R.string.posted_by_yesterday, time);
            }
            else {
                return getString(R.string.posted_by_today, time);
            }
        }
        return "";
    }

    @Bindable
    public int getArticleCount() {
        if (gallery == null) {
            return 0;
        }
        return gallery.loadArticles().size();
    }

    @Bindable
    public int getLikes() {
        if (gallery == null) {
            return 0;
        }
        return gallery.getLikes();
    }

    @Bindable
    public int getReposts() {
        if (gallery == null) {
            return 0;
        }
        return gallery.getReposts();
    }

    @Bindable
    public boolean isLiked() {
        if (gallery == null) {
            return false;
        }
        return gallery.isLiked();
    }

    @Bindable
    public boolean isReposted() {
        if (gallery == null) {
            return false;
        }

        return gallery.isReposted();
    }

    @Bindable
    public GalleryDetailCommentsViewModel getCommentsViewModel() {
        return commentsViewModel;
    }

    @Bindable
    public GalleryDetailStatusViewModel getStatusViewModel() {
        return statusViewModel;
    }

    @Bindable
    public UserSearchViewModel getPostedByViewModel() {
        return postedByViewModel;
    }

    @Bindable
    public boolean isSignedIn() {
        return sessionManager.isLoggedIn();
    }

    @Bindable
    public boolean isGalleryLoaded() {
        return galleryLoaded;
    }

    public void setGalleryLoaded(boolean galleryLoaded) {
        this.galleryLoaded = galleryLoaded;
        notifyPropertyChanged(BR.galleryLoaded);
    }

    @Bindable
    public boolean isOwner() {
        return owner;
    }

    public void setOwner(boolean owner) {
        this.owner = owner;
        notifyPropertyChanged(BR.owner);
    }

    public String getGalleryOwnerId() {
        if (gallery == null) {
            return "";
        }
        if (gallery.getOriginalOwnerId() != null) {
            return gallery.getOriginalOwnerId();
        }
        return gallery.getOwnerId();
    }

    public Date getHighlightedAt() {
        if (gallery == null) {
            return null;
        }
        return gallery.getHighlightedAt();
    }

    private void updateReposts() {
        notifyPropertyChanged(BR.reposts);
        notifyPropertyChanged(BR.reposted);
    }

    private void updateLikes() {
        notifyPropertyChanged(BR.likes);
        notifyPropertyChanged(BR.liked);
    }

    public Action1<View> share = view -> {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, EndpointHelper.currentEndpoint.frescoWebsite + "gallery/" + gallery.getId());
        getActivity().startActivity(Intent.createChooser(intent, "Share Gallery"));
    };

    public Action1<View> repost = view -> {
        if (!sessionManager.isLoggedIn()) {
            OnboardingActivity.start(getActivity());
            return;
        }
        //Can't repost your own gallery
        if (sessionManager.getCurrentSession().getUserId().equals(gallery.getOwnerId())) {
            return;
        }
        if (isReposted()) {
            galleryManager.unrepost(gallery).subscribe(success -> updateReposts());
        }
        else {
            galleryManager.repost(gallery).subscribe(success -> updateReposts());
        }
        updateReposts();
    };

    public Action1<View> like = view -> {
        if (!sessionManager.isLoggedIn()) {
            OnboardingActivity.start(getActivity());
            return;
        }
        if (isLiked()) {
            galleryManager.unlike(gallery).subscribe(success -> updateLikes());
        }
        else {
            galleryManager.like(gallery).subscribe(success -> updateLikes());
        }
        updateLikes();
    };

    public Action1<View> showLikes = view -> {
        FollowActivity.start(getActivity(), galleryId, GALLERY_LIKES_STATE);
    };
    public Action1<View> showReposts = view -> {
        FollowActivity.start(getActivity(), galleryId, GALLERY_REPOSTS_STATE);
    };

    public Action1<View> showOptions = view -> {
        if (view == null) {
            return;
        }
        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_check_internet);
            return;
        }

        reportFragment = ReportBottomSheetDialogFragment.newInstance(gallery, null, new ReportBottomSheetDialogViewModel.OnReportAndBlockListener() {
            @Override
            public void onReportGallery() {
                ReportGalleryDialogViewModel reportGalleryDialogViewModel = new ReportGalleryDialogViewModel(getActivity(), (reason, message) -> {
                    galleryManager.report(galleryId, reason, message)
                                  .subscribeOn(Schedulers.io())
                                  .observeOn(AndroidSchedulers.mainThread())
                                  .onErrorReturn(throwable -> null)
                                  .subscribe(networkReport -> {
                                      if (networkReport == null) {
                                          SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_reporting_gallery);
                                          return;
                                      }
                                      Log.e(TAG, new Gson().toJson(networkReport));
                                      DialogUtils.showFrescoDialog(getActivity(), R.string.report_sent, R.string.report_sent_message, R.string.youre_welcome,
                                              (dialogInterface, i) -> dialogInterface.dismiss());

                                  });
                    reportFragment.dismiss();
                });
                reportGalleryDialogViewModel.show(R.layout.view_report_gallery);
            }

            @Override
            public void onToggleFollow(boolean following) {
                reportFragment.dismiss();
            }

            @Override
            public void onReportUser() {
                ReportUserDialogViewModel reportUserDialogViewModel = new ReportUserDialogViewModel(getActivity(), (reason, message) -> {
                    userManager.report(gallery.getOriginalOwnerId(), reason, message)
                               .subscribeOn(Schedulers.io())
                               .observeOn(AndroidSchedulers.mainThread())
                               .onErrorReturn(throwable -> null)
                               .subscribe(networkReport -> {
                                   if (networkReport == null) {
                                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_reporting_user);
                                       return;
                                   }
                                   Log.e(TAG, new Gson().toJson(networkReport));
                                   userManager.getUser(gallery.getOriginalOwnerId())
                                              .onErrorReturn(throwable -> null)
                                              .subscribe(user -> {
                                                  if (user == null || user.isBlocking()) {
                                                      DialogUtils.showFrescoDialog(getActivity(), R.string.report_sent, R.string.report_sent_message, R.string.youre_welcome,
                                                              (dialogInterface, i) -> dialogInterface.dismiss());
                                                  }
                                                  else {
                                                      DialogUtils.showFrescoDialog(getActivity(), R.string.report_sent, getActivity().getString(R.string.report_sent_user_message, user.getUsername()),
                                                              R.string.block_user_static, ((dialogInterface, i) -> {
                                                                  onToggleBlockUser(true);
                                                                  dialogInterface.dismiss();
                                                              }),
                                                              R.string.close, (dialogInterface, i) -> dialogInterface.dismiss());
                                                  }
                                              });

                               });
                    reportFragment.dismiss();
                });
                reportUserDialogViewModel.show(R.layout.view_report_user);
                reportUserDialogViewModel.setReportTitle(getString(R.string.report_user, userManager.getUsername(gallery.getOriginalOwnerId())));
            }

            @Override
            public void onToggleBlockUser(boolean blocking) {
                if (blocking) {
                    userManager.block(gallery.getOriginalOwnerId())
                               .onErrorReturn(throwable -> null)
                               .subscribe(networkSuccessResult -> {
                                   if (networkSuccessResult != null && !TextUtils.isEmpty(networkSuccessResult.getSuccess())) {
                                       reportFragment.refreshBlockText(getActivity(), gallery.getOriginalOwnerId());
                                       userManager.getUser(gallery.getOriginalOwnerId())
                                                  .onErrorReturn(throwable -> null)
                                                  .subscribe(user -> {
                                                      user.setBlocking(true);
                                                      user.save();//todo what about                                        reportFragment.refreshBlockText(getActivity(), user.getId());
                                                      //like what if i block a user on their gallery and they commented on their own gallery? :thinking face:
                                                      //Show dialog letting user know consequencecs of blocking.
                                                      if (user.isBlocking()) {
                                                          DialogUtils.showFrescoDialog(getActivity(), R.string.blocked_text, getActivity().getString(R.string.blocked_dialog_message, user.getUsername()),
                                                                  R.string.ok, ((dialogInterface, i) -> {
                                                                      dialogInterface.dismiss();
                                                                  }),
                                                                  R.string.undo, (dialogInterface, i) -> {
                                                                      onToggleBlockUser(false); //unblock user
                                                                      dialogInterface.dismiss();
                                                                  });
                                                      }
                                                  });
                                   }
                                   else {
                                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_blocking_user);
                                   }
                               });
                }
                else {
                    userManager.unblock(gallery.getOriginalOwnerId())
                               .onErrorReturn(throwable -> null)
                               .subscribe(networkSuccessResult -> {
                                   if (networkSuccessResult != null && !TextUtils.isEmpty(networkSuccessResult.getSuccess())) {
                                       reportFragment.refreshBlockText(getActivity(), gallery.getOriginalOwnerId());
                                       userManager.getUser(gallery.getOriginalOwnerId())
                                                  .onErrorReturn(throwable -> null)
                                                  .subscribe(user -> {
                                                      if(user != null) {
                                                          user.setBlocking(false);
                                                          user.save();
                                                      }
                                                  });
                                   }
                                   else {
                                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_unblocking_user);
                                   }
                               });
                }
                reportFragment.dismiss();
            }

            @Override
            public void onDeleteComment() {
                reportFragment.dismiss();
            }
        });
        reportFragment.show(getActivity().getSupportFragmentManager(), TAG);
    };

    public void showCommentOptions(String userId, String commentToDeleteId, boolean myGallery, boolean myComment) {
        String userMe = sessionManager.getCurrentSession().getUserId(); // of current user
        Boolean itsMyGallery = userMe.equals(getGalleryOwnerId());
        userManager.getOrDownloadUser(userId)
                   .observeOn(AndroidSchedulers.mainThread())
                   .onErrorReturn(throwable -> null)
                   .subscribe(user -> {
                       if (user != null) {
                           showCommentOptions(user, commentToDeleteId, myGallery, myComment);
                       }
                       else {
                           if (!NetworkUtils.isNetworkAvailable(getActivity())) {
                               SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_check_internet);
                           }
                           else {
                               //unable to retreive comment options? kind of an edge case, really only happens if no internet.
                           }
                       }
                   });
    }

    private void showCommentOptions(User user, String commentToDeleteId, boolean myGallery, boolean myComment) {
        if (user == null || commentToDeleteId == null) {
            return;
        }
        if (!NetworkUtils.isNetworkAvailable(getActivity())) {
            SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_check_internet);
        }

        reportFragment = ReportBottomSheetDialogFragment.newInstance(null, user, true, myGallery, myComment, new ReportBottomSheetDialogViewModel.OnReportAndBlockListener() {
            @Override
            public void onReportGallery() {
                reportFragment.dismiss();
            }

            @Override
            public void onToggleFollow(boolean following) {
                //this shouldn't pop up
                reportFragment.dismiss();
            }

            @Override
            public void onReportUser() {
                ReportUserDialogViewModel reportUserDialogViewModel = new ReportUserDialogViewModel(getActivity(), (reason, message) -> {
                    userManager.report(user.getId(), reason, message)
                               .subscribeOn(Schedulers.io())
                               .observeOn(AndroidSchedulers.mainThread())
                               .onErrorReturn(throwable -> null)
                               .subscribe(networkReport -> {
                                   if (networkReport == null) {
                                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_reporting_user);
                                       return;
                                   }
                                   Log.e(TAG, new Gson().toJson(networkReport));
                                   if (user.isBlocking()) {
                                       DialogUtils.showFrescoDialog(getActivity(), R.string.report_sent, R.string.report_sent_message, R.string.youre_welcome,
                                               (dialogInterface, i) -> dialogInterface.dismiss());
                                   }
                                   else {
                                       DialogUtils.showFrescoDialog(getActivity(), R.string.report_sent, getActivity().getString(R.string.report_sent_user_message, user.getUsername()),
                                               R.string.block_user_static, ((dialogInterface, i) -> {
                                                   onToggleBlockUser(true);
                                                   dialogInterface.dismiss();
                                               }),
                                               R.string.close, (dialogInterface, i) -> dialogInterface.dismiss());
                                   }

                               });
                    reportFragment.dismiss();
                });
                reportUserDialogViewModel.show(R.layout.view_report_user);
                reportUserDialogViewModel.setReportTitle(getString(R.string.report_user, user.getUsername()));
            }

            @Override
            public void onToggleBlockUser(boolean blocking) {
                if (blocking) {
                    userManager.block(user.getId())
                               .observeOn(AndroidSchedulers.mainThread())
                               .subscribeOn(Schedulers.io())
                               .onErrorReturn(throwable -> null)
                               .subscribe(networkSuccessResult -> {
                                   if (networkSuccessResult != null && !TextUtils.isEmpty(networkSuccessResult.getSuccess())) {
                                       user.setBlocking(true);
                                       user.save();
                                       reportFragment.refreshBlockText(getActivity(), user.getId());
                                       //Show dialog letting user know consequencecs of blocking.
                                       if (user.isBlocking()) {
                                           DialogUtils.showFrescoDialog(getActivity(), R.string.blocked_text, getActivity().getString(R.string.blocked_dialog_message, user.getUsername()),
                                                   R.string.ok, ((dialogInterface, i) -> {
                                                       dialogInterface.dismiss();
                                                   }),
                                                   R.string.undo, (dialogInterface, i) -> {
                                                       onToggleBlockUser(false); //unblock user
                                                       dialogInterface.dismiss();
                                                   });
                                       }
                                   }
                                   else {
                                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_blocking_user);
                                   }
                               });
                }
                else {
                    userManager.unblock(user.getId())
                               .onErrorReturn(throwable -> null)
                               .subscribe(networkSuccessResult -> {
                                   if (networkSuccessResult != null && !TextUtils.isEmpty(networkSuccessResult.getSuccess())) {
                                       user.setBlocking(false);
                                       user.save();
                                       reportFragment.refreshBlockText(getActivity(), user.getId());
                                   }
                                   else {
                                       SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_unblocking_user);
                                   }
                               });
                }
                reportFragment.dismiss();
            }

            @Override
            public void onDeleteComment() {
                Comment commentToDelete = galleryManager.getComment(commentToDeleteId);
                if (commentToDelete != null) {
                    commentToDelete.delete();
                    galleryManager.deleteComment(gallery.getId(), commentToDeleteId)
                                  .onErrorReturn(throwable -> {
                                      SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_deleting_comment);
                                      Log.e(TAG, throwable.getMessage());
                                      commentToDelete.save();
                                      return null;
                                  })
                                  .observeOn(AndroidSchedulers.mainThread())
                                  .subscribeOn(Schedulers.io())
                                  .subscribe(networkSuccessResult -> {
                                      if (networkSuccessResult == null) {
                                          SnackbarUtil.dismissableSnackbar(getRoot(), R.string.error_deleting_comment);
                                          commentToDelete.save();
                                      }
                                      else {
                                          getCommentsViewModel().deleteCommentViewHandler();
                                      }
                                      getCommentsViewModel().updateAdapter();
                                  });
                }
                else {
                    LogUtils.i(TAG, "comment was null");
                }
                reportFragment.dismiss();

            }
        });
        reportFragment.show(getActivity().getSupportFragmentManager(), TAG);
    }

}
