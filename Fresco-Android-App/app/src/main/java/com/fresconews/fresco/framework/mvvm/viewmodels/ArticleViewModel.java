package com.fresconews.fresco.framework.mvvm.viewmodels;

import android.app.Activity;
import android.content.Intent;
import android.databinding.Bindable;
import android.net.Uri;
import android.view.View;

import com.fresconews.fresco.Fresco2;
import com.fresconews.fresco.framework.mvvm.ItemViewModel;
import com.fresconews.fresco.framework.persistence.managers.AnalyticsManager;
import com.fresconews.fresco.framework.persistence.models.Article;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import rx.functions.Action1;

public class ArticleViewModel extends ItemViewModel<Article> {
    private static final String TAG = ArticleViewModel.class.getSimpleName();

    private WeakReference<Activity> activity;

    @Inject
    AnalyticsManager analyticsManager;

    public ArticleViewModel(Activity activity, Article item) {
        super(item);
        this.activity = new WeakReference<>(activity);
        ((Fresco2) activity.getApplication()).getFrescoComponent().inject(this);

    }

    @Bindable
    public String getFavicon() {
        return getItem().getFavicon();
    }

    @Bindable
    public Uri getFaviconUri() {
        return Uri.parse(getItem().getFavicon());
    }

    @Bindable
    public String getTitle() {
        return getItem().getTitle();
    }

    @Bindable
    public String getArticleUrl() {
        return getItem().getLink();
    }

    public Action1<View> viewArticle = view -> {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(getItem().getLink().trim()));
        if(getItem().getLink()!=null){
            analyticsManager.articleOpened(getItem().getId(), getItem().getLink());

        }else {
            analyticsManager.articleOpened(getItem().getId());
        }

        activity.get().startActivity(intent);
    };
}
