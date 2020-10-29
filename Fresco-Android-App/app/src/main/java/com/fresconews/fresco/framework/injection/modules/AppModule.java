package com.fresconews.fresco.framework.injection.modules;

import android.app.Application;
import android.content.Context;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class AppModule
{
    private Application mApplication;

    private Context mContext;

    public AppModule(Application application) {
        mApplication = application;
        mContext = mApplication.getBaseContext();
    }

    @Provides
    @Singleton
    public Application provideApplication() {
        return mApplication;
    }

    @Provides
    @Singleton
    public Context provideContext() {
        return mContext;
    }
}
