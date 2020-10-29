package com.fresconews.fresco.framework.injection.modules;

import android.content.Context;

import com.fresconews.fresco.framework.network.services.AssignmentService;
import com.fresconews.fresco.framework.network.services.AuthService;
import com.fresconews.fresco.framework.network.services.FeedService;
import com.fresconews.fresco.framework.network.services.GalleryService;
import com.fresconews.fresco.framework.network.services.NotificationFeedService;
import com.fresconews.fresco.framework.network.services.PaymentService;
import com.fresconews.fresco.framework.network.services.SearchService;
import com.fresconews.fresco.framework.network.services.StoryService;
import com.fresconews.fresco.framework.network.services.UserService;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.managers.AssignmentManager;
import com.fresconews.fresco.framework.persistence.managers.AuthManager;
import com.fresconews.fresco.framework.persistence.managers.FeedManager;
import com.fresconews.fresco.framework.persistence.managers.FrescoLocationManager;
import com.fresconews.fresco.framework.persistence.managers.FCMManager;
import com.fresconews.fresco.framework.persistence.managers.GalleryManager;
import com.fresconews.fresco.framework.persistence.managers.LocalSettingsManager;
import com.fresconews.fresco.framework.persistence.managers.NotificationFeedManager;
import com.fresconews.fresco.framework.persistence.managers.NotificationIntentManager;
import com.fresconews.fresco.framework.persistence.managers.PaymentManager;
import com.fresconews.fresco.framework.persistence.managers.PostManager;
import com.fresconews.fresco.framework.persistence.managers.SearchManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.fresconews.fresco.framework.persistence.managers.StoryManager;
import com.fresconews.fresco.framework.persistence.managers.UploadManager;
import com.fresconews.fresco.framework.persistence.managers.UserManager;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ManagerModule {
    @Provides
    @Singleton
    public GalleryManager provideGalleryManager(GalleryService galleryService, FeedManager feedManager, SessionManager sessionManager, Context context) {
        return new GalleryManager(galleryService, feedManager, sessionManager, context);
    }

    @Provides
    @Singleton
    public PostManager providePostManager() {
        return new PostManager();
    }

    @Provides
    @Singleton
    public SessionManager provideSessionManager(Context context) {
        return new SessionManager(context);
    }

    @Provides
    @Singleton
    public SearchManager provideSearchManager(SearchService searchService, Context context) {
        return new SearchManager(searchService, context);
    }

    @Provides
    @Singleton
    public PaymentManager providePaymentManager(PaymentService paymentService, Context context) {
        return new PaymentManager(paymentService, context);
    }

    @Provides
    @Singleton
    public AnalyticsManager provideAnalyticsManager(Context context, UserManager userManager) {
        return new AnalyticsManager(context, userManager);
    }

    @Provides
    @Singleton
    public LocalSettingsManager provideLocalSettingsManager(Context context) {
        return new LocalSettingsManager(context);
    }

    @Provides
    @Singleton
    public AuthManager provideAuthManager(AuthService authService, SessionManager sessionManager, UserManager userManager, FCMManager FCMManager) {
        return new AuthManager(authService, sessionManager, userManager, FCMManager);
    }

    @Provides
    @Singleton
    public UserManager provideUserManager(UserService userService, Context context) {
        return new UserManager(userService, context);
    }

    @Provides
    @Singleton
    public StoryManager provideStoryManager(StoryService storyService, FeedManager feedManager, Context context) {
        return new StoryManager(storyService, feedManager, context);
    }

    @Provides
    @Singleton
    public FCMManager provideGCMManager(Context context) {
        return new FCMManager(context);
    }

    @Provides
    @Singleton
    public UploadManager provideUploadManager(Context context) {
        return new UploadManager(context);
    }

    @Provides
    @Singleton
    public AssignmentManager provideAssignmentManager(AssignmentService assignmentService) {
        return new AssignmentManager(assignmentService);
    }

    @Provides
    @Singleton
    public FeedManager providesFeedManager(FeedService feedService, SessionManager sessionManager, Context context) {
        return new FeedManager(feedService, sessionManager, context);
    }

    @Provides
    @Singleton
    public NotificationFeedManager providesNotificationFeedManager(NotificationFeedService notificationFeedService, SessionManager sessionManager, Context context) {
        return new NotificationFeedManager(notificationFeedService, sessionManager, context);
    }

    @Provides
    @Singleton
    public NotificationIntentManager providesNotificationIntentManager(AnalyticsManager analyticsManager, AssignmentManager assignmentManager, FrescoLocationManager frescoLocationManager, UserManager userManager, PaymentManager paymentManager) {
        return new NotificationIntentManager(analyticsManager, assignmentManager, frescoLocationManager, userManager, paymentManager);
    }

    @Provides
    @Singleton
    public FrescoLocationManager providesFrescoLocationManager() {
        return new FrescoLocationManager();
    }
}
