package com.fresconews.fresco.framework.mvvm.datasources;

import android.app.Activity;
import android.support.v7.widget.RecyclerView;

import com.fresconews.fresco.framework.mvvm.viewmodels.ArticleViewModel;
import com.fresconews.fresco.framework.persistence.models.Article;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ryan on 6/28/2016.
 */
public class ArticleViewModelDataSource implements IDataSource<ArticleViewModel> {
    private IDataSource<Article> dataSource;
    private WeakReference<Activity> activity;

    public ArticleViewModelDataSource(Activity activity, IDataSource<Article> dataSource) {
        this.dataSource = dataSource;
        this.activity = new WeakReference<>(activity);
    }

    @Override
    public int getItemCount() {
        return dataSource.getItemCount();
    }

    @Override
    public ArticleViewModel get(int position) {
        return new ArticleViewModel(activity.get(), dataSource.get(position));
    }

    @Override
    public void onBind(RecyclerView.Adapter adapter, int position) {
        if (dataSource != null) {
            dataSource.onBind(adapter, position);
        }
    }

    @Override
    public void onDetached() {
        if (dataSource != null) {
            dataSource.onDetached();
        }
    }

    @Override
    public List<ArticleViewModel> list() {
        List<ArticleViewModel> viewModels = new ArrayList<>();
        if (dataSource != null) {
            for (int i = 0; i < dataSource.getItemCount(); i++) {
                viewModels.add(get(i));
            }
        }
        return viewModels;
    }
}
