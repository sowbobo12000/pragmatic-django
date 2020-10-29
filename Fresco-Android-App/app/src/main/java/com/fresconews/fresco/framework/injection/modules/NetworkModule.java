package com.fresconews.fresco.framework.injection.modules;

import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.fresconews.fresco.BuildConfig;
import com.fresconews.fresco.framework.network.EndpointHelper;
import com.fresconews.fresco.framework.network.FrescoAuthInterceptor;
import com.fresconews.fresco.framework.network.FrescoMixedListDeserializer;
import com.fresconews.fresco.framework.network.FrescoTokenAuthenticator;
import com.fresconews.fresco.framework.network.responses.NetworkFrescoObject;
import com.fresconews.fresco.framework.network.services.AssignmentService;
import com.fresconews.fresco.framework.network.services.AuthService;
import com.fresconews.fresco.framework.network.services.FeedService;
import com.fresconews.fresco.framework.network.services.GalleryService;
import com.fresconews.fresco.framework.network.services.NotificationFeedService;
import com.fresconews.fresco.framework.network.services.PaymentService;
import com.fresconews.fresco.framework.network.services.PostService;
import com.fresconews.fresco.framework.network.services.SearchService;
import com.fresconews.fresco.framework.network.services.StoryService;
import com.fresconews.fresco.framework.network.services.UserService;
import com.fresconews.fresco.framework.persistence.managers.LocalSettingsManager;
import com.fresconews.fresco.framework.persistence.managers.SessionManager;
import com.github.filosganga.geogson.gson.GeometryAdapterFactory;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;

@Module
public class NetworkModule {
    @Provides
    @Singleton
    public OkHttpClient provideOkHttpClient(FrescoAuthInterceptor authInterceptor, FrescoTokenAuthenticator tokenAuthenticator, LocalSettingsManager localSettingsManager) {
        OkHttpClient.Builder clientBuilder = new OkHttpClient.Builder();
        if (BuildConfig.DEBUG && localSettingsManager.printAPILogs()) {
            HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
            logging.setLevel(HttpLoggingInterceptor.Level.BODY);

            // Adds logging to each request, outputting all information
            clientBuilder.addInterceptor(logging);
        }
        clientBuilder.addNetworkInterceptor(authInterceptor);
        clientBuilder.authenticator(tokenAuthenticator);
        clientBuilder.addNetworkInterceptor(new StethoInterceptor());

        return clientBuilder.build();
    }

    @Provides
    @Singleton
    public Gson provideGson() {
        Type FrescoMixedListType = new TypeToken<List<NetworkFrescoObject>>() {
        }.getType();

        return new GsonBuilder()
                .registerTypeAdapterFactory(new GeometryAdapterFactory())
                .registerTypeAdapter(FrescoMixedListType, new FrescoMixedListDeserializer())
                .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ")
                .create();
    }

    @Provides
    @Singleton
    public Retrofit provideRetrofit(OkHttpClient client, Gson gson) {
        return new Retrofit.Builder()
                .baseUrl(EndpointHelper.currentEndpoint.baseUrl)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .client(client)
                .build();
    }

    @Provides
    @Singleton
    public FrescoAuthInterceptor provideFrescoAuthInterceptor(SessionManager sessionManager) {
        return new FrescoAuthInterceptor(sessionManager);
    }

    @Provides
    @Singleton
    public FrescoTokenAuthenticator provideFrescoTokenAuthenticator(SessionManager sessionManager) {
        return new FrescoTokenAuthenticator(sessionManager);
    }

    @Provides
    @Singleton
    public GalleryService provideGalleryService(Retrofit retrofit) {
        return retrofit.create(GalleryService.class);
    }

    @Provides
    @Singleton
    public SearchService provideSearchService(Retrofit retrofit) {
        return retrofit.create(SearchService.class);
    }

    @Provides
    @Singleton
    public PaymentService providePaymentService(Retrofit retrofit) {
        return retrofit.create(PaymentService.class);
    }

    @Provides
    @Singleton
    public AuthService provideAuthService(Retrofit retrofit) {
        return retrofit.create(AuthService.class);
    }

    @Provides
    @Singleton
    public PostService providePostService(Retrofit retrofit) {
        return retrofit.create(PostService.class);
    }

    @Provides
    @Singleton
    public UserService provideUserService(Retrofit retrofit) {
        return retrofit.create(UserService.class);
    }

    @Provides
    @Singleton
    public StoryService provideStoryService(Retrofit retrofit) {
        return retrofit.create(StoryService.class);
    }

    @Provides
    @Singleton
    public FeedService provideFeedService(Retrofit retrofit) {
        return retrofit.create(FeedService.class);
    }

    @Provides
    @Singleton
    public AssignmentService provideAssignmentService(Retrofit retrofit) {
        return retrofit.create(AssignmentService.class);
    }

    @Provides
    @Singleton
    public NotificationFeedService provideNotificationFeedService(Retrofit retrofit) {
        return retrofit.create(NotificationFeedService.class);
    }
}
