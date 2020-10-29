package com.fresconews.fresco.framework.injection.components;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.framework.base.BaseActivity;
import com.fresconews.fresco.framework.injection.helpers.InjectionTarget;
import com.fresconews.fresco.framework.injection.modules.AppModule;
import com.fresconews.fresco.framework.injection.modules.ManagerModule;
import com.fresconews.fresco.framework.injection.modules.NetworkModule;
import com.fresconews.fresco.framework.mvvm.viewmodels.ArticleViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.CommentViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.GalleryViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.NotificationFeedItemViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.StoriesPreviewItemModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.UserAutoCompleteViewModel;
import com.fresconews.fresco.framework.mvvm.viewmodels.UserSearchViewModel;
import com.fresconews.fresco.framework.network.FrescoAuthInterceptor;
import com.fresconews.fresco.framework.network.FrescoTokenAuthenticator;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.FrescoUploadService2;
import com.fresconews.fresco.framework.persistence.managers.NotificationIntentManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.v2.assignments.AssignmentMapActivity;
import com.fresconews.fresco.v2.assignments.AssignmentMapViewModel;
import com.fresconews.fresco.v2.assignments.GlobalAssignmentViewModel;
import com.fresconews.fresco.v2.backgroundservice.FrescoLocationService;
import com.fresconews.fresco.v2.backgroundservice.OnClearFromRecentService;
import com.fresconews.fresco.v2.camera.CameraActivity;
import com.fresconews.fresco.v2.dev.DevOptionsViewModel;
import com.fresconews.fresco.v2.editprofile.EditProfileActivity;
import com.fresconews.fresco.v2.editprofile.EditProfileViewModel;
import com.fresconews.fresco.v2.follow.FollowViewModel;
import com.fresconews.fresco.v2.follow.followers.FollowersViewModel;
import com.fresconews.fresco.v2.follow.following.FollowingViewModel;
import com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailActivity;
import com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailCommentsViewModel;
import com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailViewModel;
import com.fresconews.fresco.v2.gallery.gallerydetail.GalleryDetailStatusViewModel;
import com.fresconews.fresco.v2.gallery.gallerylist.GalleryListActivity;
import com.fresconews.fresco.v2.gallery.gallerylist.GalleryListViewModel;
import com.fresconews.fresco.v2.home.HomeActivity;
import com.fresconews.fresco.v2.home.HomeViewModel;
import com.fresconews.fresco.v2.home.following.FollowingFeedViewModel;
import com.fresconews.fresco.v2.home.highlights.HighlightsFragment;
import com.fresconews.fresco.v2.home.highlights.HighlightsViewModel;
import com.fresconews.fresco.v2.login.LoginActivity;
import com.fresconews.fresco.v2.login.LoginViewModel;
import com.fresconews.fresco.v2.login.MigrateUserDialogViewModel;
import com.fresconews.fresco.v2.login.TOSDialogViewModel;
import com.fresconews.fresco.v2.mediabrowser.MediaBrowserViewModel;
import com.fresconews.fresco.v2.navdrawer.HeaderViewModel;
import com.fresconews.fresco.v2.notificationfeed.NotificationFeedViewModel;
import com.fresconews.fresco.v2.notifications.FcmListenerService;
import com.fresconews.fresco.v2.onboarding.OnBoardingViewModel;
import com.fresconews.fresco.v2.onboarding.OnboardingActivity;
import com.fresconews.fresco.v2.profile.ProfileActivity;
import com.fresconews.fresco.v2.profile.ProfileViewModel;
import com.fresconews.fresco.v2.profile.likes.LikesFeedViewModel;
import com.fresconews.fresco.v2.profile.user.UserFeedViewModel;
import com.fresconews.fresco.v2.report.ReportBottomSheetDialogViewModel;
import com.fresconews.fresco.v2.search.SearchViewModel;
import com.fresconews.fresco.v2.search.UserSeeAllViewModel;
import com.fresconews.fresco.v2.settings.BaseSettingsViewModel;
import com.fresconews.fresco.v2.settings.SettingsActivity;
import com.fresconews.fresco.v2.settings.SettingsPaymentViewModel;
import com.fresconews.fresco.v2.settings.SettingsViewModel;
import com.fresconews.fresco.v2.settings.dialogs.ChangeEmailDialogViewModel;
import com.fresconews.fresco.v2.settings.dialogs.ChangePasswordDialogViewModel;
import com.fresconews.fresco.v2.settings.dialogs.ChangeUserNameDialogViewModel;
import com.fresconews.fresco.v2.settings.dialogs.MapRadiusDialogViewModel;
import com.fresconews.fresco.v2.settings.dialogs.TaxInfoDialogViewModel;
import com.fresconews.fresco.v2.signup.SignupActivity;
import com.fresconews.fresco.v2.signup.SignupViewModel;
import com.fresconews.fresco.v2.storiespreview.StoriesPreviewActivity;
import com.fresconews.fresco.v2.storiespreview.StoriesPreviewViewModel;
import com.fresconews.fresco.v2.storygallery.StoryGalleryActivity;
import com.fresconews.fresco.v2.storygallery.StoryGalleryViewModel;
import com.fresconews.fresco.v2.submission.SubmissionActivity;
import com.fresconews.fresco.v2.submission.SubmissionViewModel;
import com.fresconews.fresco.v2.views.FrescoPostVideoView;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {AppModule.class, NetworkModule.class, ManagerModule.class})
public interface FrescoComponent {
    // Activities
    void inject(BaseActivity activity);
    void inject(OnboardingActivity activity);
    void inject(SignupActivity activity);
    void inject(LoginActivity activity);
    void inject(HomeActivity activity);
    void inject(StoriesPreviewActivity activity);
    void inject(EditProfileActivity editProfileActivity);
    void inject(StoryGalleryActivity activity);
    void inject(ProfileActivity profileActivity);
    void inject(SettingsActivity activity);
    void inject(CameraActivity cameraActivity);
    void inject(SubmissionActivity submissionActivity);
    void inject(GalleryDetailActivity galleryActivity);
    void inject(AssignmentMapActivity assignmentMapActivity);
    void inject(GalleryListActivity galleryListActivity);

    // Activity View Models
    void inject(HomeViewModel viewModel);
    void inject(LoginViewModel viewModel);
    void inject(SignupViewModel viewModel);
    void inject(GalleryDetailViewModel viewModel);
    void inject(StoriesPreviewViewModel viewModel);
    void inject(AssignmentMapViewModel viewModel);
    void inject(StoryGalleryViewModel viewModel);
    void inject(MediaBrowserViewModel viewModel);
    void inject(SubmissionViewModel viewModel);
    void inject(NotificationFeedViewModel viewModel);
    void inject(ProfileViewModel viewModel);
    void inject(EditProfileViewModel viewModel);
    void inject(SearchViewModel searchViewModel);
    void inject(SettingsViewModel viewModel);
    void inject(SettingsPaymentViewModel viewModel);
    void inject(GlobalAssignmentViewModel viewModel);
    void inject(GalleryListViewModel viewModel);
    void inject(UserSeeAllViewModel viewModel);
    void inject(FollowViewModel viewModel);
    void inject(OnBoardingViewModel viewModel);
    void inject(DevOptionsViewModel viewModel);

    // Fragments
    void inject(HighlightsFragment highlightsFragment);

    // Fragment View Models
    void inject(HighlightsViewModel highlightsViewModel);
    void inject(FollowingFeedViewModel followingFeedViewModel);
    void inject(UserFeedViewModel userFeedViewModel);
    void inject(LikesFeedViewModel likesFeedViewModel);
    void inject(FollowersViewModel followersViewModel);
    void inject(FollowingViewModel followingViewModel);

    // Item View Models
    void inject(GalleryViewModel viewModel);
    void inject(NotificationFeedItemViewModel viewModel);
    void inject(StoriesPreviewItemModel viewModel);
    void inject(UserSearchViewModel viewModel);
    void inject(UserAutoCompleteViewModel viewModel);
    void inject(CommentViewModel viewModel);
    void inject(ArticleViewModel viewModel);

    // Dialog View Models
    void inject(MigrateUserDialogViewModel viewModel);
    void inject(TOSDialogViewModel viewModel);
    void inject(TaxInfoDialogViewModel viewModel);
    void inject(ChangeUserNameDialogViewModel viewModel);
    void inject(ChangePasswordDialogViewModel viewModel);
    void inject(ChangeEmailDialogViewModel viewModel);
    void inject(MapRadiusDialogViewModel viewModel);

    // Regular View Models
    void inject(HeaderViewModel viewModel);
    void inject(BaseSettingsViewModel viewModel);
    void inject(GalleryDetailCommentsViewModel viewModel);
    void inject(GalleryDetailStatusViewModel viewModel);
    void inject(ReportBottomSheetDialogViewModel viewModel);
    void inject(FrescoPostVideoView viewModel);

    // Services
    void inject(FcmListenerService service);
    void inject(FrescoUploadService2 service);
    void inject(OnClearFromRecentService service);
    void inject(FrescoLocationService service);

    // Other
    void inject(InjectionTarget mInjection);
    void inject(Fresco2 fresco2);
    void inject(FrescoTokenAuthenticator frescoTokenAuthenticator);
    void inject(FrescoAuthInterceptor frescoAuthInterceptor);
    void inject(NotificationIntentManager notificationIntentManager);
}
